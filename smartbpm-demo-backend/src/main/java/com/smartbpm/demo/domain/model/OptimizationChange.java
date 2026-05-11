package com.smartbpm.demo.domain.model;

public record OptimizationChange(
        String title,
        String before,
        String after,
        String reason) {
}
