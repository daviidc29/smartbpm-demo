package com.smartbpm.demo.domain.model;

public record ValidationIssue(
        String layer,
        String severity,
        String message) {
}
