package com.smartbpm.demo.api.dto;

import com.smartbpm.demo.domain.model.IntermediateProcess;
import com.smartbpm.demo.domain.model.OptimizationChange;
import com.smartbpm.demo.domain.model.TraceEventView;
import com.smartbpm.demo.domain.model.ValidationReport;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ProcessViewResponse(
        String processId,
        String status,
        String originalText,
        String ragContextText,
        String finalPrompt,
        String llmRawResponse,
        String intermediateJson,
        IntermediateProcess intermediateProcess,
        String bpmnXml,
        ValidationReport validationReport,
        String optimizedIntermediateJson,
        IntermediateProcess optimizedProcess,
        String optimizedBpmnXml,
        ValidationReport optimizedValidationReport,
        List<OptimizationChange> optimizationChanges,
        Map<String, String> artifactKeys,
        List<TraceEventView> traceEvents,
        Instant createdAt,
        Instant updatedAt) {
}
