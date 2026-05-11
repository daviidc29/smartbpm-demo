package com.smartbpm.demo.service.audit;

import com.smartbpm.demo.domain.model.TraceEventView;
import com.smartbpm.demo.persistence.ProcessEventEntity;
import com.smartbpm.demo.persistence.ProcessEventRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class AuditService {

    private final ProcessEventRepository processEventRepository;

    public AuditService(ProcessEventRepository processEventRepository) {
        this.processEventRepository = processEventRepository;
    }

    public void record(String processId, String stage, String status, String message) {
        ProcessEventEntity entity = new ProcessEventEntity()
                .setProcessId(processId)
                .setStage(stage)
                .setStatus(status)
                .setMessage(message)
                .setCreatedAt(Instant.now());
        processEventRepository.save(entity);
    }

    public List<TraceEventView> list(String processId) {
        return processEventRepository.findByProcessIdOrderByCreatedAtAsc(processId).stream()
                .map(event -> new TraceEventView(event.getStage(), event.getStatus(), event.getMessage(), event.getCreatedAt()))
                .toList();
    }
}
