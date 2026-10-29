package com.tamdan.service;

import com.tamdan.dto.SafetyDto.ActivityBatchRequest;
import com.tamdan.dto.SafetyDto.EventRequest;
import com.tamdan.dto.SafetyDto.IngestionResultResponse;
import com.tamdan.entities.SafetyEvent;
import com.tamdan.exception.BadRequestException;
import com.tamdan.repository.SafetyEventRepository;
import com.tamdan.security.AuthenticatedDeviceContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class ActivityIngestionService {

    private final SafetyEventRepository safetyEventRepository;
    private final ChildDeviceRepositoryFacade childDeviceRepositoryFacade;
    private final ObjectMapper objectMapper;
    private final AlertService alertService;

    @Transactional
    public IngestionResultResponse ingest(AuthenticatedDeviceContext device, ActivityBatchRequest request, SafetyEvent.Kind kind) {
        childDeviceRepositoryFacade.requireActive(device);
        Instant now = Instant.now();
        int accepted = 0;
        int duplicate = 0;
        for (EventRequest event : request.events()) {
            validateOccurredAt(event.occurredAt(), now);
            if (safetyEventRepository.findByChildDeviceIdAndEventId(device.deviceId(), event.eventId()).isPresent()) {
                duplicate++;
                continue;
            }
            String payload = normalizePayload(event.payload());
            try {
                safetyEventRepository.saveAndFlush(SafetyEvent.create(
                    childDeviceRepositoryFacade.requireEntity(device), event.eventId(), kind, request.schemaVersion(), event.occurredAt(), payload, now
                ));
                if (kind == SafetyEvent.Kind.DOMAIN && payload.replace(" ", "").contains("\"risk\":\"RISK\"")) {
                    alertService.upsertRiskAlert(device.childProfileId(), event.eventId(), now);
                }
                accepted++;
            } catch (DataIntegrityViolationException duplicateRace) {
                duplicate++;
            }
        }
        return new IngestionResultResponse(accepted, duplicate, 0);
    }

    private void validateOccurredAt(Instant occurredAt, Instant now) {
        if (occurredAt.isAfter(now.plus(5, ChronoUnit.MINUTES)) || occurredAt.isBefore(now.minus(31, ChronoUnit.DAYS))) {
            throw new BadRequestException("Event timestamp is outside the accepted window");
        }
    }

    private String normalizePayload(JsonNode payload) {
        if (payload == null || payload.isNull()) return "{}";
        try {
            if (!payload.isObject()) throw new BadRequestException("Event payload must be a JSON object");
            String serialized = objectMapper.writeValueAsString(payload);
            if (serialized.length() > 10000) throw new BadRequestException("Event payload exceeds the maximum size");
            return serialized;
        } catch (BadRequestException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BadRequestException("Event payload must be valid JSON");
        }
    }
}
