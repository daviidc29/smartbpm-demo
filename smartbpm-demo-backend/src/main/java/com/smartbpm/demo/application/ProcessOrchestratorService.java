package com.smartbpm.demo.application;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbpm.demo.api.dto.GenerateProcessRequest;
import com.smartbpm.demo.api.dto.ProcessViewResponse;
import com.smartbpm.demo.domain.model.*;
import com.smartbpm.demo.persistence.ProcessTraceEntity;
import com.smartbpm.demo.persistence.ProcessTraceRepository;
import com.smartbpm.demo.service.audit.AuditService;
import com.smartbpm.demo.service.optimizer.ProcessOptimizerService;
import com.smartbpm.demo.service.storage.StorageService;
import com.smartbpm.demo.application.clients.AiWorkerClient;
import com.smartbpm.demo.application.clients.TransformClient;
import com.smartbpm.demo.application.clients.ValidatorClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.*;
import java.util.Base64;

@Service
public class ProcessOrchestratorService {

    private final AiWorkerClient aiWorkerClient;
    private final TransformClient transformClient;
    private final ValidatorClient validatorClient;
    private final ProcessOptimizerService optimizerService;
    private final StorageService storageService;
    private final AuditService auditService;
    private final ProcessTraceRepository processTraceRepository;
    private final ObjectMapper objectMapper;

    public ProcessOrchestratorService(
            AiWorkerClient aiWorkerClient,
            TransformClient transformClient,
            ValidatorClient validatorClient,
            ProcessOptimizerService optimizerService,
            StorageService storageService,
            AuditService auditService,
            ProcessTraceRepository processTraceRepository,
            ObjectMapper objectMapper) {
        this.aiWorkerClient = aiWorkerClient;
        this.transformClient = transformClient;
        this.validatorClient = validatorClient;
        this.optimizerService = optimizerService;
        this.storageService = storageService;
        this.auditService = auditService;
        this.processTraceRepository = processTraceRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ProcessViewResponse generate(GenerateProcessRequest request) {
        String processId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        ProcessTraceEntity entity = new ProcessTraceEntity()
                .setId(processId)
                .setStatus("RECEIVED")
                .setOriginalText(request.narrative())
                .setCreatedAt(now)
                .setUpdatedAt(now);
        processTraceRepository.save(entity);
        auditService.record(processId, "REQUEST", "OK", "Narrative received by orchestrator.");

        AiWorkerResult aiResult = aiWorkerClient.generate(request.narrative());
        auditService.record(processId, "AI_WORKER", "OK", "Intermediate representation produced.");

        TransformResult transformResult = transformClient.transform(aiResult.intermediateProcess());
        auditService.record(processId, "TRANSFORMER", "OK", "Intermediate JSON compiled into BPMN XML.");

        ValidationReport validationReport = validatorClient.validate(request.narrative(), aiResult.intermediateProcess(), transformResult.bpmnXml());
        auditService.record(processId, "VALIDATOR", validationReport.valid() ? "OK" : "ERROR",
                "Validation finished with " + validationReport.issues().size() + " findings.");

        Map<String, String> artifactKeys = new LinkedHashMap<>();
        artifactKeys.put("intermediateJson", storageService.storeText(processId, "json", "process.json", aiResult.intermediateJson()));
        artifactKeys.put("bpmnXml", storageService.storeText(processId, "bpmn", "process.bpmn", transformResult.bpmnXml()));
        artifactKeys.put("prompt", storageService.storeText(processId, "audit", "prompt.txt", aiResult.finalPrompt()));
        artifactKeys.put("ragContext", storageService.storeText(processId, "audit", "rag-context.txt", aiResult.ragContextText()));
        artifactKeys.put("llmRawResponse", storageService.storeText(processId, "audit", "llm-response.json", aiResult.llmRawResponse()));
        auditService.record(processId, "STORAGE", "OK", "Artifacts stored in the configured storage adapter.");

        entity.setStatus(validationReport.valid() ? "GENERATED" : "GENERATED_WITH_WARNINGS")
                .setRagContextText(aiResult.ragContextText())
                .setFinalPrompt(aiResult.finalPrompt())
                .setLlmRawResponse(aiResult.llmRawResponse())
                .setIntermediateJson(aiResult.intermediateJson())
                .setBpmnXml(transformResult.bpmnXml())
                .setValidationJson(writeJson(validationReport))
                .setArtifactKeysJson(writeJson(artifactKeys))
                .setUpdatedAt(Instant.now());
        processTraceRepository.save(entity);
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public ProcessViewResponse get(String processId) {
        ProcessTraceEntity entity = find(processId);
        return toResponse(entity);
    }

    @Transactional
    public ProcessViewResponse updateBpmn(String processId, String bpmnXml) {
        ProcessTraceEntity entity = find(processId);
        entity.setBpmnXml(bpmnXml).setUpdatedAt(Instant.now());
        processTraceRepository.save(entity);
        auditService.record(processId, "MANUAL_EDIT", "OK", "The user manually edited the BPMN diagram.");
        return toResponse(entity);
    }


    @Transactional
    public ProcessViewResponse optimize(String processId) {
        ProcessTraceEntity entity = find(processId);
        IntermediateProcess original = read(entity.getIntermediateJson(), IntermediateProcess.class);

        OptimizationResult optimizationResult = optimizerService.optimize(entity.getOriginalText(), original);
        TransformResult optimizedBpmn = transformClient.transform(optimizationResult.optimizedProcess());
        ValidationReport optimizedValidation = validatorClient.validate(
                entity.getOriginalText(),
                optimizationResult.optimizedProcess(),
                optimizedBpmn.bpmnXml());

        String optimizedJson = writeJson(optimizationResult.optimizedProcess());
        Map<String, String> artifacts = readMap(entity.getArtifactKeysJson());
        artifacts.put("optimizedIntermediateJson", storageService.storeText(processId, "json", "process-optimized.json", optimizedJson));
        artifacts.put("optimizedBpmnXml", storageService.storeText(processId, "bpmn", "process-optimized.bpmn", optimizedBpmn.bpmnXml()));
        entity.setOptimizedIntermediateJson(optimizedJson)
                .setOptimizedBpmnXml(optimizedBpmn.bpmnXml())
                .setOptimizedValidationJson(writeJson(optimizedValidation))
                .setOptimizationChangesJson(writeJson(optimizationResult.changes()))
                .setArtifactKeysJson(writeJson(artifacts))
                .setStatus(optimizedValidation.valid() ? "OPTIMIZED" : "OPTIMIZED_WITH_WARNINGS")
                .setUpdatedAt(Instant.now());
        processTraceRepository.save(entity);

        auditService.record(processId, "OPTIMIZER", "OK",
                "Optimization created " + optimizationResult.changes().size() + " change(s).");
        auditService.record(processId, "STORAGE", "OK",
                "Optimized artifacts stored in the configured storage adapter.");
        return toResponse(entity);
    }

    @Transactional
    public void persistPdf(String processId, String fileName, String base64Pdf) {
        ProcessTraceEntity entity = find(processId);
        Map<String, String> artifacts = readMap(entity.getArtifactKeysJson());
        byte[] content = Base64.getDecoder().decode(base64Pdf);
        String key = storageService.storeBytes(processId, "pdf", fileName, content, "application/pdf");
        artifacts.put("pdf", key);
        entity.setArtifactKeysJson(writeJson(artifacts)).setUpdatedAt(Instant.now());
        processTraceRepository.save(entity);
        auditService.record(processId, "PDF_EXPORT", "OK", "The frontend uploaded the generated PDF for traceability.");
    }


    private ProcessTraceEntity find(String processId) {
        return processTraceRepository.findById(processId)
                .orElseThrow(() -> new NoSuchElementException("Process not found: " + processId));
    }

    private ProcessViewResponse toResponse(ProcessTraceEntity entity) {
        IntermediateProcess intermediateProcess = readOrNull(entity.getIntermediateJson(), IntermediateProcess.class);
        IntermediateProcess optimizedProcess = readOrNull(entity.getOptimizedIntermediateJson(), IntermediateProcess.class);
        ValidationReport validation = readOrNull(entity.getValidationJson(), ValidationReport.class);
        ValidationReport optimizedValidation = readOrNull(entity.getOptimizedValidationJson(), ValidationReport.class);
        List<OptimizationChange> changes = readList(entity.getOptimizationChangesJson(), new TypeReference<List<OptimizationChange>>() {});
        Map<String, String> artifactKeys = readMap(entity.getArtifactKeysJson());

        return new ProcessViewResponse(
                entity.getId(),
                entity.getStatus(),
                entity.getOriginalText(),
                entity.getRagContextText(),
                entity.getFinalPrompt(),
                entity.getLlmRawResponse(),
                entity.getIntermediateJson(),
                intermediateProcess,
                entity.getBpmnXml(),
                validation,
                entity.getOptimizedIntermediateJson(),
                optimizedProcess,
                entity.getOptimizedBpmnXml(),
                optimizedValidation,
                changes,
                artifactKeys,
                auditService.list(entity.getId()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private String writeJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to serialize JSON", e);
        }
    }

    private <T> T read(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to deserialize JSON", e);
        }
    }

    private <T> T readOrNull(String json, Class<T> type) {
        if (json == null || json.isBlank()) {
            return null;
        }
        return read(json, type);
    }

    private <T> List<T> readList(String json, TypeReference<List<T>> typeReference) {
        try {
            if (json == null || json.isBlank()) {
                return List.of();
            }
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to deserialize JSON list", e);
        }
    }

    private Map<String, String> readMap(String json) {
        try {
            if (json == null || json.isBlank()) {
                return new LinkedHashMap<>();
            }
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            throw new IllegalStateException("Unable to deserialize artifact map", e);
        }
    }
}
