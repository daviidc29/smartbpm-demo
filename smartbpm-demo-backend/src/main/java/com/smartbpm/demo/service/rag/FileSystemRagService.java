package com.smartbpm.demo.service.rag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    /** Files whose name starts with one of these prefixes are always included in the RAG context. */
    private static final Set<String> ALWAYS_INCLUDE_PREFIXES = Set.of("01-", "02-");

    /** Common Spanish stop words to filter out during tokenization so they don't inflate scores. */
    private static final Set<String> STOP_WORDS = Set.of(
            "que", "del", "los", "las", "una", "uno", "con", "para", "por", "como",
            "más", "mas", "pero", "sus", "les", "ser", "este", "esta", "estos", "estas",
            "ese", "esa", "esos", "esas", "otro", "otra", "otros", "otras",
            "todo", "toda", "todos", "todas", "cada", "muy", "desde", "hasta",
            "entre", "sobre", "durante", "antes", "después", "donde", "cuando",
            "también", "tiene", "solo", "sólo", "hay", "sin",
            "proceso", "sistema", "final", "debe", "puede", "hacer"
    );

    /** Bonus multiplier applied when a narrative token matches a keyword in the JSON document. */
    private static final double KEYWORD_BONUS = 3.0;

    private final List<RagDocument> catalog;
    private final ObjectMapper objectMapper;

    public FileSystemRagService() {
        this.objectMapper = new ObjectMapper();
        this.catalog = loadDocuments();
    }

    @Override
    public RagRetrievalResult retrieve(String narrative) {
        Set<String> tokens = tokenize(narrative);

        // Separate always-included base documents from the rest
        List<RagDocument> baseDocs = new ArrayList<>();
        List<RagDocument> candidateDocs = new ArrayList<>();

        for (RagDocument doc : catalog) {
            if (isBaseDocument(doc.name())) {
                baseDocs.add(new RagDocument(doc.name(), doc.content(), 1.0));
            } else {
                double s = score(doc, tokens);
                candidateDocs.add(new RagDocument(doc.name(), doc.content(), s));
            }
        }

        // Rank candidate docs and pick the top 3
        List<RagDocument> topCandidates = candidateDocs.stream()
                .sorted(Comparator.comparingDouble(RagDocument::score).reversed())
                .limit(3)
                .filter(doc -> doc.score() > 0.0)
                .toList();

        // Combine: base documents first, then top matched processes
        List<RagDocument> ranked = new ArrayList<>(baseDocs);
        ranked.addAll(topCandidates);

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

    /**
     * Scores a document against the user's narrative tokens.
     * For JSON process documents, extracted keywords get a bonus multiplier so that
     * domain-specific terms like "venta", "stock", "reorden" rank higher than generic content matches.
     */
    private double score(RagDocument doc, Set<String> tokens) {
        if (tokens.isEmpty()) {
            return 0.0;
        }

        String normalized = doc.content().toLowerCase(Locale.ROOT);
        long contentHits = tokens.stream().filter(normalized::contains).count();
        double baseScore = (double) contentHits / tokens.size();

        // Extract keywords from JSON documents for bonus scoring
        Set<String> keywords = extractKeywords(doc.content());
        if (!keywords.isEmpty()) {
            long keywordHits = tokens.stream()
                    .filter(token -> keywords.stream().anyMatch(kw -> kw.contains(token) || token.contains(kw)))
                    .count();
            double keywordScore = (double) keywordHits / tokens.size() * KEYWORD_BONUS;
            return baseScore + keywordScore;
        }

        return baseScore;
    }

    /**
     * Attempts to extract the "keywords" array from a JSON document.
     * Returns an empty set for non-JSON or documents without keywords.
     */
    private Set<String> extractKeywords(String content) {
        try {
            JsonNode root = objectMapper.readTree(content);
            JsonNode keywordsNode = root.get("keywords");
            if (keywordsNode != null && keywordsNode.isArray()) {
                Set<String> keywords = new java.util.HashSet<>();
                for (JsonNode kw : keywordsNode) {
                    keywords.add(kw.asText().toLowerCase(Locale.ROOT));
                }
                return keywords;
            }
        } catch (Exception ignored) {
            // Not a JSON document or no keywords field — fine, return empty
        }
        return Set.of();
    }

    private boolean isBaseDocument(String filename) {
        if (filename == null) return false;
        return ALWAYS_INCLUDE_PREFIXES.stream().anyMatch(filename::startsWith);
    }

    private Set<String> tokenize(String text) {
        return Arrays.stream(text.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+"))
                .filter(token -> token.length() > 2)
                .filter(token -> !STOP_WORDS.contains(token))
                .collect(Collectors.toSet());
    }
}
