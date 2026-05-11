package com.smartbpm.demo.domain.model;

import java.util.ArrayList;
import java.util.List;

public record ValidationReport(
        boolean valid,
        List<ValidationIssue> issues) {

    public ValidationReport {
        issues = issues == null ? new ArrayList<>() : issues;
    }
}
