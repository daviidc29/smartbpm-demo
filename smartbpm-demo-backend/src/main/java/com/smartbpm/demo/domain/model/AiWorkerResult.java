package com.smartbpm.demo.domain.model;

public record AiWorkerResult(
        String ragContextText,
        String finalPrompt,
        String llmRawResponse,
        String intermediateJson,
        IntermediateProcess intermediateProcess) {
}
