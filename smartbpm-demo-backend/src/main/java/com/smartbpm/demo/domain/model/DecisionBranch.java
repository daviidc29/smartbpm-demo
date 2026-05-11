package com.smartbpm.demo.domain.model;

public record DecisionBranch(
        String id,
        String condition,
        String targetRef) {
}
