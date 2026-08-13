package com.smartbpm.demo.service.optimizer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbpm.demo.domain.model.*;
import com.smartbpm.demo.service.ai.AiGateway;
import com.smartbpm.demo.service.rag.RagRetrievalResult;
import com.smartbpm.demo.service.rag.RagService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProcessOptimizerService {

    private final AiGateway aiGateway;
    private final ObjectMapper objectMapper;
    private final RagService ragService;

    public ProcessOptimizerService(AiGateway aiGateway, ObjectMapper objectMapper, RagService ragService) {
        this.aiGateway = aiGateway;
        this.objectMapper = objectMapper;
        this.ragService = ragService;
    }

    public OptimizationResult optimize(String narrative, IntermediateProcess source) {
        try {
            RagRetrievalResult rag = ragService.retrieve(narrative);
            String originalJson = objectMapper.writeValueAsString(source);
            String prompt = """
                    You are a BPMN Optimization Expert.
                    Your task is to optimize the provided BPMN Intermediate JSON process.
                    
                    Optimization Criteria:
                    1. Separate MVP from phase 2 (e.g. separate core inventory logic from optional AI vision).
                    2. Never automate business decisions involving money or inventory without human confirmation.
                    3. Stock alerts must be automatic, not manual.
                    4. Each exception path (technical failure, invalid data) must have an explicit handling path.
                    5. Each process must end in a measurable event (e.g. "sale registered") for reporting.
                    6. Roles with different permissions must be reflected in lanes (e.g. Tendero vs Cajero).
                    7. Always separate money flows based on who pays whom (revenue vs subscription vs supplier commission).
                    
                    Meta-Rule for AI: Before optimizing, first diagnose:
                    - Identify 3 assumptions.
                    - Identify 2 unhandled exceptions.
                    - Identify 1 main risk.
                    
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
                    (Include your diagnosis in one of the changes).
                    
                    RAG Context for Business Rules:
                    %s
                    
                    Original Narrative:
                    %s
                    
                    Original JSON:
                    %s
                    """.formatted(rag.combinedText(), narrative, originalJson);

            String responseJson = aiGateway.generateStructuredJson(prompt);
            return objectMapper.readValue(responseJson, OptimizationResult.class);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to run AI optimization", e);
        }
    }
}

