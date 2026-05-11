package com.smartbpm.demo.domain.model;

import java.util.ArrayList;
import java.util.List;

public record OptimizationResult(
        IntermediateProcess optimizedProcess,
        List<OptimizationChange> changes) {

    public OptimizationResult {
        changes = changes == null ? new ArrayList<>() : changes;
    }
}
