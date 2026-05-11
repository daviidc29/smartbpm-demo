package com.smartbpm.demo.domain.model;

import java.util.List;

public record ProcessDecision(
        String id,
        String name,
        int order,
        String type,
        String mergeId,
        List<DecisionBranch> branches) {
}
