package com.smartbpm.demo.application.clients;

import com.smartbpm.demo.domain.model.AiWorkerResult;

public interface AiWorkerClient {
    AiWorkerResult generate(String narrative);
}
