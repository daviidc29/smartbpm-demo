package com.smartbpm.demo.service.rag;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FileSystemRagService implements RagService {

    private final List<RagDocument> catalog;

    public FileSystemRagService() {
        this.catalog = loadDocuments();
    }

    @Override
    public RagRetrievalResult retrieve(String narrative) {
        Set<String> tokens = tokenize(narrative);
        List<RagDocument> ranked = catalog.stream()
                .map(doc -> new RagDocument(doc.name(), doc.content(), score(doc.content(), tokens)))
                .sorted(Comparator.comparingDouble(RagDocument::score).reversed())
                .limit(3)
                .toList();

        String combined = ranked.stream()
                .map(doc -> "## " + doc.name() + "\n" + doc.content())
                .collect(Collectors.joining("\n\n"));
        return new RagRetrievalResult(ranked, combined);
    }

    private List<RagDocument> loadDocuments() {
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources("classpath*:rag/*");
            List<RagDocument> docs = new ArrayList<>();
            for (Resource resource : resources) {
                String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                docs.add(new RagDocument(resource.getFilename(), content, 0.0));
            }
            return docs;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load RAG resources", e);
        }
    }

    private double score(String content, Set<String> tokens) {
        if (tokens.isEmpty()) {
            return 0.0;
        }
        String normalized = content.toLowerCase(Locale.ROOT);
        long hits = tokens.stream().filter(normalized::contains).count();
        return (double) hits / tokens.size();
    }

    private Set<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+"))
                .filter(token -> token.length() > 2)
                .collect(Collectors.toSet());
    }
}
