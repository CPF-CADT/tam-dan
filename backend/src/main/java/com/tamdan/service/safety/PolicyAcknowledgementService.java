package com.tamdan.service.safety;

import com.tamdan.service.device.ChildDeviceRepositoryFacade;
import com.tamdan.dto.SafetyDto.DevicePolicyDeliveryResponse;
import com.tamdan.dto.SafetyDto.PolicyAcknowledgementRequest;
import com.tamdan.entities.PolicyDelivery;
import com.tamdan.entities.SafetyPolicy;
import com.tamdan.exception.ConflictException;
import com.tamdan.exception.ResourceNotFoundException;
import com.tamdan.repository.PolicyDeliveryRepository;
import com.tamdan.repository.SafetyPolicyRepository;
import com.tamdan.security.AuthenticatedDeviceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PolicyAcknowledgementService {
    private final PolicyDeliveryRepository deliveryRepository;
    private final SafetyPolicyRepository policyRepository;
    private final ChildDeviceRepositoryFacade deviceFacade;

    @Transactional
    public DevicePolicyDeliveryResponse acknowledge(AuthenticatedDeviceContext device, Long policyId, PolicyAcknowledgementRequest request) {
        deviceFacade.requireActive(device);
        PolicyDelivery delivery = deliveryRepository.findByPolicyIdAndChildDeviceId(policyId, device.deviceId()).orElseThrow(() -> new ResourceNotFoundException("Policy delivery was not found"));
        if (!delivery.getPolicy().getChildProfile().getId().equals(device.childProfileId()) || !delivery.getPolicy().getChildProfile().getFamily().getId().equals(device.familyId())) {
            throw new ResourceNotFoundException("Policy delivery was not found");
        }
        if (request.policyVersion() != delivery.getPolicyVersion()) throw new ConflictException("Policy acknowledgement version is stale");
        delivery.acknowledge(request.policyVersion(), request.status(), request.reportedAt(), request.failureReason());
        refreshPolicyState(delivery.getPolicy());
        return new DevicePolicyDeliveryResponse(device.deviceId(), delivery.getState(), delivery.getAcknowledgedVersion(), delivery.getReportedAt(), delivery.getFailureReason());
    }

    private void refreshPolicyState(SafetyPolicy policy) {
        var deliveries = deliveryRepository.findAllByPolicyIdAndPolicyChildProfileFamilyId(policy.getId(), policy.getChildProfile().getFamily().getId());
        if (!deliveries.isEmpty() && deliveries.stream().allMatch(d -> d.getState() == PolicyDelivery.State.ACTIVE && policy.getVersion().equals(d.getAcknowledgedVersion()))) policy.activate();
        else if (deliveries.stream().anyMatch(d -> d.getState() == PolicyDelivery.State.UNSUPPORTED)) policy.markUnsupported();
        else if (deliveries.stream().anyMatch(d -> d.getState() == PolicyDelivery.State.FAILED)) policy.markFailed();
        else policy.markPendingDelivery();
        policyRepository.save(policy);
    }
}
