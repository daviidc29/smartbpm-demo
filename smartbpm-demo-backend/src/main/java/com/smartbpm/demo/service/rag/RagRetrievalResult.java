package com.smartbpm.demo.service.rag;

import java.util.List;

public record RagRetrievalResult(
        List<RagDocument> documents,
        String combinedText) {
}
