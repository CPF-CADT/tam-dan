package com.tamdan.service.safety;

import com.tamdan.service.device.ChildDeviceRepositoryFacade;
import com.tamdan.service.family.FamilyService;
import com.tamdan.dto.SafetyDto.DevicePolicyDeliveryResponse;
import com.tamdan.dto.SafetyDto.PolicyDeliveryResponse;
import com.tamdan.dto.SafetyDto.PolicyRequest;
import com.tamdan.dto.SafetyDto.PolicyResponse;
import com.tamdan.entities.ChildDevice;
import com.tamdan.entities.ChildProfile;
import com.tamdan.entities.SafetyPolicy;
import com.tamdan.exception.ConflictException;
import com.tamdan.exception.ResourceNotFoundException;
import com.tamdan.repository.ChildDeviceRepository;
import com.tamdan.repository.ChildProfileRepository;
import com.tamdan.repository.PolicyDeliveryRepository;
import com.tamdan.repository.SafetyPolicyRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PolicyService {
    private final FamilyService familyService;
    private final ChildProfileRepository childProfileRepository;
    private final ChildDeviceRepository childDeviceRepository;
    private final SafetyPolicyRepository policyRepository;
    private final PolicyDeliveryRepository deliveryRepository;
    private final ChildDeviceRepositoryFacade childDeviceRepositoryFacade;

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<PolicyResponse> list(Long parentId, Long childId, int page, int pageSize) {
        Long familyId = familyService.requireFamilyForParent(parentId).getId();
        return policyRepository.findAllByChildProfileIdAndChildProfileFamilyId(childId, familyId, PageRequest.of(page, pageSize)).map(this::toResponse);
    }

    @Transactional
    public PolicyResponse create(Long parentId, Long childId, PolicyRequest request) {
        var child = requireChild(parentId, childId);
        SafetyPolicy policy = policyRepository.save(SafetyPolicy.create(child, request.type(), request.target().trim(), Instant.now()));
        createDeliveries(policy);
        policy.markPendingDelivery();
        return toResponse(policy);
    }

    @Transactional
    public PolicyResponse update(Long parentId, Long childId, Long policyId, PolicyRequest request) {
        SafetyPolicy policy = requirePolicy(parentId, childId, policyId);
        if (policy.getState() == SafetyPolicy.State.REMOVED) throw new ConflictException("Removed policy cannot be updated");
        policy.update(request.type(), request.target().trim());
        deliveryRepository.deleteAll(deliveryRepository.findAllByPolicyIdAndPolicyChildProfileFamilyId(policy.getId(), policy.getChildProfile().getFamily().getId()));
        createDeliveries(policy);
        policy.markPendingDelivery();
        return toResponse(policy);
    }

    @Transactional
    public void remove(Long parentId, Long childId, Long policyId) { requirePolicy(parentId, childId, policyId).remove(); }

    @Transactional
    public PolicyResponse pause(Long parentId, Long childId, Long policyId) {
        SafetyPolicy policy = requirePolicy(parentId, childId, policyId);
        policy.pause();
        return toResponse(policy);
    }

    @Transactional(readOnly = true)
    public PolicyDeliveryResponse delivery(Long parentId, Long childId, Long policyId) {
        SafetyPolicy policy = requirePolicy(parentId, childId, policyId);
        var deliveries = deliveryRepository.findAllByPolicyIdAndPolicyChildProfileFamilyId(policyId, policy.getChildProfile().getFamily().getId());
        String state = deliveries.isEmpty() ? "PENDING" : deliveries.stream().allMatch(d -> d.getState() == com.tamdan.entities.PolicyDelivery.State.ACTIVE) ? "ACKNOWLEDGED" : deliveries.stream().anyMatch(d -> d.getState() == com.tamdan.entities.PolicyDelivery.State.FAILED) ? "FAILED" : deliveries.stream().anyMatch(d -> d.getState() == com.tamdan.entities.PolicyDelivery.State.UNSUPPORTED) ? "UNSUPPORTED" : "PARTIAL";
        return new PolicyDeliveryResponse(policy.getId(), policy.getVersion(), state, deliveries.stream().map(d -> new DevicePolicyDeliveryResponse(d.getChildDevice().getId(), d.getState(), d.getAcknowledgedVersion(), d.getReportedAt(), d.getFailureReason())).toList());
    }

    @Transactional(readOnly = true)
    public java.util.List<PolicyResponse> download(com.tamdan.security.AuthenticatedDeviceContext device) {
        childDeviceRepositoryFacade.requireActive(device);
        return deliveryRepository.findAllByChildDeviceIdAndPolicyChildProfileFamilyId(device.deviceId(), device.familyId()).stream()
            .filter(d -> d.getPolicy().getState() != SafetyPolicy.State.REMOVED && d.getPolicy().getState() != SafetyPolicy.State.PAUSED)
            .map(d -> toResponse(d.getPolicy())).toList();
    }

    private void createDeliveries(SafetyPolicy policy) {
        for (ChildDevice device : childDeviceRepository.findAllByChildProfileId(policy.getChildProfile().getId())) {
            if (Boolean.TRUE.equals(device.getActive()) && device.getRevokedAt() == null) deliveryRepository.save(com.tamdan.entities.PolicyDelivery.pending(policy, device));
        }
    }

    private ChildProfile requireChild(Long parentId, Long childId) {
        Long familyId = familyService.requireFamilyForParent(parentId).getId();
        return childProfileRepository.findByIdAndFamilyIdAndActiveTrue(childId, familyId).orElseThrow(() -> new ResourceNotFoundException("Child was not found"));
    }

    private SafetyPolicy requirePolicy(Long parentId, Long childId, Long policyId) {
        Long familyId = familyService.requireFamilyForParent(parentId).getId();
        return policyRepository.findByIdAndChildProfileIdAndChildProfileFamilyId(policyId, childId, familyId).orElseThrow(() -> new ResourceNotFoundException("Policy was not found"));
    }

    private PolicyResponse toResponse(SafetyPolicy policy) { return new PolicyResponse(policy.getId(), policy.getChildProfile().getId(), policy.getVersion(), policy.getType(), policy.getTarget(), policy.getState(), policy.getUpdatedAt()); }
}
