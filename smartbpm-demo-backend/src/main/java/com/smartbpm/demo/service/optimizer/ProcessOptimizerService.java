package com.smartbpm.demo.service.optimizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbpm.demo.domain.model.*;
import com.smartbpm.demo.service.ai.AiGateway;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProcessOptimizerService {

    private final AiGateway aiGateway;
    private final ObjectMapper objectMapper;

    public ProcessOptimizerService(AiGateway aiGateway, ObjectMapper objectMapper) {
        this.aiGateway = aiGateway;
        this.objectMapper = objectMapper;
    }

    public OptimizationResult optimize(String narrative, IntermediateProcess source) {
        try {
            String originalJson = objectMapper.writeValueAsString(source);
            String prompt = """
                    You are a BPMN Optimization Expert.
                    Your task is to optimize the provided BPMN Intermediate JSON process.
                    
                    Optimization Criteria:
                    - Identify and reduce elements that do not add value (unnecessary handoffs, duplicated activities).
                    - Eliminate multiple validations on the same information; unify tasks if possible.
                    - Remove unnecessary wait times for approvals or signatures.
                    - Prevent manual errors by automating tasks (use "Sistema" role) and proposing digital forms/centralized data.
                    - Fix disorganized sequences, run tasks in parallel if possible, remove excessive dependencies.
                    - Add auto-approval paths for low-risk or simple paths if applicable.
                    - Fix unconnected components and ensure the graph is a SINGLE CONTINUOUS CHAIN from startEvent to endEvent.
                    - CRITICAL: NO HANGING NODES. Every task and decision MUST have an outgoing flow that leads to the endEvent. NO isolated elements.
                    - CRITICAL: The endEvent MUST have at least one incoming flow. Every task MUST have exactly ONE incoming flow and exactly ONE outgoing flow.
                    - CRITICAL: If you need to branch or merge flows, you MUST use a Gateway. NO task should have multiple incoming or outgoing sequence flows.
                    - CRITICAL: Merging gateways (joins) MUST have between 2 and 3 incoming flows. Diverging gateways (splits) MUST have between 2 and 3 outgoing branches.
                    - ONLY output JSON. DO NOT output markdown code blocks.
                    
                    Return EXACTLY a JSON object with two keys:
                    1. "optimizedProcess": the complete new IntermediateProcess JSON.
                       (Constraints: startEvent and endEvent MUST be objects with id and name. The labels MUST be in Spanish).
                    2. "changes": an array of objects describing the optimizations, each with "title", "before", "after", and "reason".
                    
                    Original Narrative:
                    %s
                    
                    Original JSON:
                    %s
                    """.formatted(narrative, originalJson);

            String responseJson = aiGateway.generateStructuredJson(prompt);
            return objectMapper.readValue(responseJson, OptimizationResult.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to run AI optimization", e);
        }
    }
}

