package com.smartbpm.demo.lambda;

import com.smartbpm.demo.domain.model.AiWorkerResult;
import com.smartbpm.demo.service.ai.AiWorkerService;

public class AiWorkerLambdaHandler {

    private final AiWorkerService aiWorkerService;

    public AiWorkerLambdaHandler(AiWorkerService aiWorkerService) {
        this.aiWorkerService = aiWorkerService;
    }

    public AiWorkerResult handleRequest(String narrative) {
        return aiWorkerService.generate(narrative);
    }
}
