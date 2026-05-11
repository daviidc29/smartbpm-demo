package com.smartbpm.demo.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbpm.demo.domain.model.AiWorkerResult;
import com.smartbpm.demo.domain.model.IntermediateProcess;
import com.smartbpm.demo.service.rag.RagRetrievalResult;
import com.smartbpm.demo.service.rag.RagService;
import org.springframework.stereotype.Service;

@Service
public class AiWorkerService {

    private final RagService ragService;
    private final PromptBuilder promptBuilder;
    private final AiGateway aiGateway;
    private final ObjectMapper objectMapper;

    public AiWorkerService(RagService ragService, PromptBuilder promptBuilder, AiGateway aiGateway, ObjectMapper objectMapper) {
        this.ragService = ragService;
        this.promptBuilder = promptBuilder;
        this.aiGateway = aiGateway;
        this.objectMapper = objectMapper;
    }

    public AiWorkerResult generate(String narrative) {
        RagRetrievalResult rag = ragService.retrieve(narrative);
        String prompt = promptBuilder.build(narrative, rag.combinedText());
        String json = aiGateway.generateStructuredJson(prompt);
        try {
            IntermediateProcess process = objectMapper.readValue(json, IntermediateProcess.class);
            return new AiWorkerResult(rag.combinedText(), prompt, json, json, process);
        } catch (Exception e) {
            throw new IllegalStateException("The AI output is not valid against the intermediate schema", e);
        }
    }
}
