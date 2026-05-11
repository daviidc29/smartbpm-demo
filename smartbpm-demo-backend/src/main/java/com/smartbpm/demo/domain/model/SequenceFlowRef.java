package com.smartbpm.demo.domain.model;

public record SequenceFlowRef(
        String id,
        String sourceRef,
        String targetRef,
        String conditionLabel) {
}
