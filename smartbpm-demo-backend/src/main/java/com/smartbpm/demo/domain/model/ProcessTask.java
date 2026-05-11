package com.smartbpm.demo.domain.model;

public record ProcessTask(
        String id,
        String name,
        String role,
        int order,
        boolean automated) {
}
