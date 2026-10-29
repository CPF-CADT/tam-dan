package com.tamdan.repository;

import com.tamdan.entities.SafetyEvent;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SafetyEventRepository extends JpaRepository<SafetyEvent, Long> {
    Optional<SafetyEvent> findByChildDeviceIdAndEventId(Long deviceId, String eventId);
    List<SafetyEvent> findAllByChildProfileIdAndOccurredAtGreaterThanEqualAndOccurredAtLessThanEqual(Long childId, Instant from, Instant to);
    List<SafetyEvent> findAllByChildProfileIdAndOccurredAtGreaterThanEqualAndOccurredAtLessThanEqualAndKind(Long childId, Instant from, Instant to, SafetyEvent.Kind kind);
    List<SafetyEvent> findAllByChildDeviceIdAndOccurredAtGreaterThanEqualAndOccurredAtLessThanEqual(Long deviceId, Instant from, Instant to);
}
