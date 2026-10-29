package com.tamdan.repository;

import com.tamdan.entities.PolicyDelivery;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyDeliveryRepository extends JpaRepository<PolicyDelivery, Long> {
    Optional<PolicyDelivery> findByPolicyIdAndChildDeviceId(Long policyId, Long deviceId);
    List<PolicyDelivery> findAllByPolicyIdAndPolicyChildProfileFamilyId(Long policyId, Long familyId);
    List<PolicyDelivery> findAllByChildDeviceIdAndPolicyChildProfileFamilyId(Long deviceId, Long familyId);
}
