package com.smartbpm.demo.application.clients.local;

import com.smartbpm.demo.application.clients.AiWorkerClient;
import com.smartbpm.demo.domain.model.AiWorkerResult;
import com.smartbpm.demo.service.ai.AiWorkerService;

public class LocalAiWorkerClient implements AiWorkerClient {

    private final AiWorkerService aiWorkerService;

    public LocalAiWorkerClient(AiWorkerService aiWorkerService) {
        this.aiWorkerService = aiWorkerService;
    }

    @Override
    public AiWorkerResult generate(String narrative) {
        return aiWorkerService.generate(narrative);
    }
}
