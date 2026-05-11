package com.smartbpm.demo.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "process_event")
public class ProcessEventEntity {

    @Id
    @GeneratedValue
    private UUID id;

    private String processId;
    private String stage;
    private String status;
    private String message;
    private Instant createdAt;

    public UUID getId() {
        return id;
    }

    public String getProcessId() {
        return processId;
    }

    public ProcessEventEntity setProcessId(String processId) {
        this.processId = processId;
        return this;
    }

    public String getStage() {
        return stage;
    }

    public ProcessEventEntity setStage(String stage) {
        this.stage = stage;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public ProcessEventEntity setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ProcessEventEntity setMessage(String message) {
        this.message = message;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public ProcessEventEntity setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
