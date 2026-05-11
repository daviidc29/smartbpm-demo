package com.smartbpm.demo.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "process_trace")
public class ProcessTraceEntity {

    @Id
    private String id;

    private String status;

    @Column(columnDefinition = "TEXT")
    private String originalText;

    @Column(columnDefinition = "TEXT")
    private String ragContextText;

    @Column(columnDefinition = "TEXT")
    private String finalPrompt;

    @Column(columnDefinition = "TEXT")
    private String llmRawResponse;

    @Column(columnDefinition = "TEXT")
    private String intermediateJson;

    @Column(columnDefinition = "TEXT")
    private String bpmnXml;

    @Column(columnDefinition = "TEXT")
    private String validationJson;

    @Column(columnDefinition = "TEXT")
    private String optimizedIntermediateJson;

    @Column(columnDefinition = "TEXT")
    private String optimizedBpmnXml;

    @Column(columnDefinition = "TEXT")
    private String optimizedValidationJson;

    @Column(columnDefinition = "TEXT")
    private String optimizationChangesJson;

    @Column(columnDefinition = "TEXT")
    private String artifactKeysJson;

    private Instant createdAt;
    private Instant updatedAt;

    public String getId() {
        return id;
    }

    public ProcessTraceEntity setId(String id) {
        this.id = id;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public ProcessTraceEntity setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getOriginalText() {
        return originalText;
    }

    public ProcessTraceEntity setOriginalText(String originalText) {
        this.originalText = originalText;
        return this;
    }

    public String getRagContextText() {
        return ragContextText;
    }

    public ProcessTraceEntity setRagContextText(String ragContextText) {
        this.ragContextText = ragContextText;
        return this;
    }

    public String getFinalPrompt() {
        return finalPrompt;
    }

    public ProcessTraceEntity setFinalPrompt(String finalPrompt) {
        this.finalPrompt = finalPrompt;
        return this;
    }

    public String getLlmRawResponse() {
        return llmRawResponse;
    }

    public ProcessTraceEntity setLlmRawResponse(String llmRawResponse) {
        this.llmRawResponse = llmRawResponse;
        return this;
    }

    public String getIntermediateJson() {
        return intermediateJson;
    }

    public ProcessTraceEntity setIntermediateJson(String intermediateJson) {
        this.intermediateJson = intermediateJson;
        return this;
    }

    public String getBpmnXml() {
        return bpmnXml;
    }

    public ProcessTraceEntity setBpmnXml(String bpmnXml) {
        this.bpmnXml = bpmnXml;
        return this;
    }

    public String getValidationJson() {
        return validationJson;
    }

    public ProcessTraceEntity setValidationJson(String validationJson) {
        this.validationJson = validationJson;
        return this;
    }

    public String getOptimizedIntermediateJson() {
        return optimizedIntermediateJson;
    }

    public ProcessTraceEntity setOptimizedIntermediateJson(String optimizedIntermediateJson) {
        this.optimizedIntermediateJson = optimizedIntermediateJson;
        return this;
    }

    public String getOptimizedBpmnXml() {
        return optimizedBpmnXml;
    }

    public ProcessTraceEntity setOptimizedBpmnXml(String optimizedBpmnXml) {
        this.optimizedBpmnXml = optimizedBpmnXml;
        return this;
    }

    public String getOptimizedValidationJson() {
        return optimizedValidationJson;
    }

    public ProcessTraceEntity setOptimizedValidationJson(String optimizedValidationJson) {
        this.optimizedValidationJson = optimizedValidationJson;
        return this;
    }

    public String getOptimizationChangesJson() {
        return optimizationChangesJson;
    }

    public ProcessTraceEntity setOptimizationChangesJson(String optimizationChangesJson) {
        this.optimizationChangesJson = optimizationChangesJson;
        return this;
    }

    public String getArtifactKeysJson() {
        return artifactKeysJson;
    }

    public ProcessTraceEntity setArtifactKeysJson(String artifactKeysJson) {
        this.artifactKeysJson = artifactKeysJson;
        return this;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public ProcessTraceEntity setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public ProcessTraceEntity setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }
}
