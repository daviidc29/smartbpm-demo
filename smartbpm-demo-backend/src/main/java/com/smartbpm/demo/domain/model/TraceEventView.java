package com.smartbpm.demo.domain.model;

import java.time.Instant;

public record TraceEventView(
        String stage,
        String status,
        String message,
        Instant createdAt) {
}
