package com.smartbpm.demo.service.rag;

public record RagDocument(
        String name,
        String content,
        double score) {
}
