package com.smartbpm.demo.service.validation;

import com.smartbpm.demo.domain.model.IntermediateProcess;
import com.smartbpm.demo.domain.model.ProcessDecision;
import com.smartbpm.demo.domain.model.ProcessTask;
import com.smartbpm.demo.domain.model.SequenceFlowRef;
import com.smartbpm.demo.domain.model.ValidationIssue;
import com.smartbpm.demo.domain.model.ValidationReport;
import com.smartbpm.demo.service.compiler.BpmnCompilerService;
import com.smartbpm.demo.service.compiler.LayoutResult;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProcessValidationService {

    private final BpmnCompilerService compilerService;

    public ProcessValidationService(BpmnCompilerService compilerService) {
        this.compilerService = compilerService;
    }

    public ValidationReport validate(String narrative, IntermediateProcess process, String xml) {
        List<ValidationIssue> issues = new ArrayList<>();
        validateIntermediate(process, issues);
        validateConsistency(process, issues);
        validateXml(xml, issues);
        validateSemantics(narrative, process, issues);
        validateVisual(process, issues);
        boolean valid = issues.stream().noneMatch(issue -> "ERROR".equalsIgnoreCase(issue.severity()));
        return new ValidationReport(valid, issues);
    }

    private void validateIntermediate(IntermediateProcess process, List<ValidationIssue> issues) {
        if (process == null) {
            issues.add(new ValidationIssue("ESTRUCTURA", "ERROR", "El proceso intermedio es nulo."));
            return;
        }
        if (process.startEvent() == null) {
            issues.add(new ValidationIssue("ESTRUCTURA", "ERROR", "Se requiere un evento de inicio."));
        }
        if (process.endEvent() == null) {
            issues.add(new ValidationIssue("ESTRUCTURA", "ERROR", "Se requiere un evento de fin."));
        }
        if (process.tasks().isEmpty()) {
            issues.add(new ValidationIssue("ESTRUCTURA", "ERROR", "Se requiere al menos una tarea."));
        }
        Set<String> ids = new HashSet<>();
        List<String> collectedIds = new ArrayList<>();
        if (process.startEvent() != null) collectedIds.add(process.startEvent().id());
        if (process.endEvent() != null) collectedIds.add(process.endEvent().id());
        process.tasks().forEach(task -> collectedIds.add(task.id()));
        process.decisions().forEach(decision -> {
            collectedIds.add(decision.id());
            collectedIds.add(decision.mergeId());
        });
        for (String id : collectedIds) {
            if (!ids.add(id)) {
                issues.add(new ValidationIssue("ESTRUCTURA", "ERROR", "ID duplicado detectado: " + id));
            }
        }
    }

    private void validateConsistency(IntermediateProcess process, List<ValidationIssue> issues) {
        Set<String> validIds = new HashSet<>();
        validIds.add(process.startEvent().id());
        validIds.add(process.endEvent().id());
        process.tasks().forEach(task -> validIds.add(task.id()));
        process.decisions().forEach(decision -> {
            validIds.add(decision.id());
            validIds.add(decision.mergeId());
        });

        Map<String, Integer> incoming = new HashMap<>();
        Map<String, Integer> outgoing = new HashMap<>();

        for (SequenceFlowRef flow : process.sequenceFlows()) {
            if (!validIds.contains(flow.sourceRef()) || !validIds.contains(flow.targetRef())) {
                issues.add(new ValidationIssue("CONSISTENCIA", "ERROR",
                        "Referencia de flujo inválida: " + flow.id()));
            }
            outgoing.merge(flow.sourceRef(), 1, Integer::sum);
            incoming.merge(flow.targetRef(), 1, Integer::sum);
        }

        if (incoming.getOrDefault(process.startEvent().id(), 0) > 0) {
            issues.add(new ValidationIssue("CONSISTENCIA", "ERROR", "El evento de inicio no debe tener flujos de entrada."));
        }
        if (outgoing.getOrDefault(process.endEvent().id(), 0) > 0) {
            issues.add(new ValidationIssue("CONSISTENCIA", "ERROR", "El evento de fin no debe tener flujos de salida."));
        }

        for (ProcessTask task : process.tasks()) {
            if (incoming.getOrDefault(task.id(), 0) == 0) {
                issues.add(new ValidationIssue("CONSISTENCIA", "ERROR", "Tarea sin flujo de entrada: " + task.name()));
            }
            if (outgoing.getOrDefault(task.id(), 0) == 0) {
                issues.add(new ValidationIssue("CONSISTENCIA", "ERROR", "Tarea sin flujo de salida: " + task.name()));
            }
        }

        for (ProcessDecision decision : process.decisions()) {
            if (decision.branches() == null || decision.branches().size() < 2) {
                issues.add(new ValidationIssue("CONSISTENCIA", "ERROR", "La decisión debe tener al menos dos ramas: " + decision.name()));
            }
            Set<String> branchTargets = decision.branches().stream().map(branch -> branch.targetRef()).collect(Collectors.toSet());
            for (var branch : decision.branches()) {
                if (!validIds.contains(branch.targetRef())) {
                    issues.add(new ValidationIssue("CONSISTENCIA", "ERROR", "La rama de decisión apunta a un destino desconocido: " + branch.targetRef()));
                }
            }
            if (branchTargets.size() < 2) {
                issues.add(new ValidationIssue("CONSISTENCIA", "ADVERTENCIA", "Las ramas de decisión convergen al mismo destino, lo cual puede ser redundante."));
            }
        }
    }

    private void validateXml(String xml, List<ValidationIssue> issues) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            if (document.getDocumentElement() == null) {
                issues.add(new ValidationIssue("XML", "ERROR", "Documento BPMN XML inválido."));
            }
        } catch (Exception e) {
            issues.add(new ValidationIssue("XML", "ERROR", "Fallo al parsear XML: " + e.getMessage()));
        }
    }

    private void validateSemantics(String narrative, IntermediateProcess process, List<ValidationIssue> issues) {
        Set<String> narrativeTokens = tokenize(narrative);
        Set<String> taskTokens = process.tasks().stream()
                .flatMap(task -> tokenize(task.name()).stream())
                .collect(Collectors.toSet());

        long overlap = narrativeTokens.stream().filter(taskTokens::contains).count();
        if (overlap < 2) {
            issues.add(new ValidationIssue("SEMÁNTICA", "ADVERTENCIA",
                    "Baja coincidencia entre la narrativa y las etiquetas de tareas generadas."));
        }

        if (narrative.toLowerCase(Locale.ROOT).matches(".*(aproba|autori|valid|revis|firma).*") &&
                process.tasks().stream().noneMatch(task -> task.name().toLowerCase(Locale.ROOT).matches(".*(aproba|autori|valid|revis|firma).*")) &&
                process.decisions().stream().noneMatch(decision -> decision.name().toLowerCase(Locale.ROOT).matches(".*(aproba|autori|valid|revis|firma).*"))) {
            issues.add(new ValidationIssue("SEMÁNTICA", "ADVERTENCIA",
                    "La narrativa sugiere una aprobación/validación pero el modelo no la nombra explícitamente."));
        }
    }

    private void validateVisual(IntermediateProcess process, List<ValidationIssue> issues) {
        LayoutResult layout = compilerService.layout(process);
        List<LayoutResult.ShapeBounds> shapes = new ArrayList<>(layout.shapes().values());

        for (int i = 0; i < shapes.size(); i++) {
            for (int j = i + 1; j < shapes.size(); j++) {
                LayoutResult.ShapeBounds a = shapes.get(i);
                LayoutResult.ShapeBounds b = shapes.get(j);
                // Use a margin of 5 pixels to avoid reporting tight but valid layouts as overlaps
                double margin = 5.0;
                boolean overlap = (a.left() + margin) < (b.right() - margin)
                        && (a.right() - margin) > (b.left() + margin)
                        && (a.y() + margin) < (b.y() + b.height() - margin)
                        && (a.y() + a.height() - margin) > (b.y() + margin);
                if (overlap) {
                    issues.add(new ValidationIssue("VISUAL", "ADVERTENCIA",
                            "Solapamiento visual detectado entre " + a.id() + " y " + b.id()));
                }
            }
        }

        if (process.tasks().size() + process.decisions().size() > 12) {
            issues.add(new ValidationIssue("VISUAL", "ADVERTENCIA",
                    "El diagrama puede estar muy denso; considera dividir el proceso o usar carriles."));
        }
    }

    private Set<String> tokenize(String value) {
        return Arrays.stream(value.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+"))
                .filter(token -> token.length() > 3)
                .collect(Collectors.toSet());
    }
}
