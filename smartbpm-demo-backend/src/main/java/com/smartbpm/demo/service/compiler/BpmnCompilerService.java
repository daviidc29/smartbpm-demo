package com.smartbpm.demo.service.compiler;

import com.smartbpm.demo.domain.model.DecisionBranch;
import com.smartbpm.demo.domain.model.IntermediateProcess;
import com.smartbpm.demo.domain.model.ProcessDecision;
import com.smartbpm.demo.domain.model.ProcessTask;
import com.smartbpm.demo.domain.model.SequenceFlowRef;
import com.smartbpm.demo.domain.model.TransformResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;

@Service
public class BpmnCompilerService {

    private final LayoutCalculator layoutCalculator = new LayoutCalculator();

    public TransformResult compile(IntermediateProcess process) {
        LayoutResult layout = layoutCalculator.calculate(process);
        String processId = sanitizeId(process.processName(), "process_main");
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<bpmn:definitions xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ")
                .append("xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" ")
                .append("xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" ")
                .append("xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" ")
                .append("xmlns:di=\"http://www.omg.org/spec/DD/20100524/DI\" ")
                .append("id=\"Definitions_1\" targetNamespace=\"http://smartbpm.demo\">\n");

        xml.append("<bpmn:process id=\"").append(processId).append("\" name=\"")
                .append(escape(process.processName())).append("\" isExecutable=\"false\">\n");

        xml.append("<bpmn:startEvent id=\"").append(process.startEvent().id()).append("\" name=\"")
                .append(escape(process.startEvent().name())).append("\" />\n");

        List<ProcessDecision> decisions = process.decisions().stream()
                .sorted(Comparator.comparingInt(ProcessDecision::order))
                .toList();

        for (ProcessDecision decision : decisions) {
            xml.append("<bpmn:exclusiveGateway id=\"").append(decision.id()).append("\" name=\"")
                    .append(escape(decision.name())).append("\" />\n");
        }

        for (ProcessTask task : process.tasks().stream().sorted(Comparator.comparingInt(ProcessTask::order)).toList()) {
            String tag = task.automated() ? "serviceTask" : "task";
            xml.append("<bpmn:").append(tag).append(" id=\"").append(task.id()).append("\" name=\"")
                    .append(escape(labelForTask(task))).append("\" />\n");
        }

        for (ProcessDecision decision : decisions) {
            boolean used = process.sequenceFlows().stream()
                    .anyMatch(f -> decision.mergeId().equals(f.targetRef()) || decision.mergeId().equals(f.sourceRef()));
            if (used) {
                xml.append("<bpmn:exclusiveGateway id=\"").append(decision.mergeId()).append("\" name=\"Merge\" gatewayDirection=\"Converging\" />\n");
            }
        }

        xml.append("<bpmn:endEvent id=\"").append(process.endEvent().id()).append("\" name=\"")
                .append(escape(process.endEvent().name())).append("\" />\n");

        for (SequenceFlowRef flow : process.sequenceFlows()) {
            xml.append("<bpmn:sequenceFlow id=\"").append(flow.id()).append("\" sourceRef=\"")
                    .append(flow.sourceRef()).append("\" targetRef=\"").append(flow.targetRef()).append("\"");
            if (StringUtils.hasText(flow.conditionLabel())) {
                xml.append(" name=\"").append(escape(flow.conditionLabel())).append("\"");
            }
            xml.append(" />\n");
        }

        xml.append("</bpmn:process>\n");

        xml.append("<bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n");
        xml.append("<bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"").append(processId).append("\">\n");

        for (LayoutResult.ShapeBounds bounds : layout.shapes().values()) {
            xml.append("<bpmndi:BPMNShape id=\"Shape_").append(bounds.id()).append("\" bpmnElement=\"").append(bounds.id()).append("\">\n");
            xml.append("<dc:Bounds x=\"").append(format(bounds.x())).append("\" y=\"").append(format(bounds.y()))
                    .append("\" width=\"").append(format(bounds.width())).append("\" height=\"").append(format(bounds.height())).append("\" />\n");
            xml.append("</bpmndi:BPMNShape>\n");
        }

        for (SequenceFlowRef flow : process.sequenceFlows()) {
            List<LayoutResult.Point> points = layout.edges().get(flow.id());
            if (points == null || points.isEmpty()) {
                continue;
            }
            xml.append("<bpmndi:BPMNEdge id=\"Edge_").append(flow.id()).append("\" bpmnElement=\"").append(flow.id()).append("\">\n");
            for (LayoutResult.Point point : points) {
                xml.append("<di:waypoint x=\"").append(format(point.x())).append("\" y=\"").append(format(point.y())).append("\" />\n");
            }
            xml.append("</bpmndi:BPMNEdge>\n");
        }

        xml.append("</bpmndi:BPMNPlane>\n");
        xml.append("</bpmndi:BPMNDiagram>\n");
        xml.append("</bpmn:definitions>\n");
        return new TransformResult(xml.toString());
    }

    public LayoutResult layout(IntermediateProcess process) {
        return layoutCalculator.calculate(process);
    }

    private String labelForTask(ProcessTask task) {
        if (StringUtils.hasText(task.role())) {
            return "[" + task.role() + "] " + task.name();
        }
        return task.name();
    }

    private String sanitizeId(String value, String defaultValue) {
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        return value.toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");
    }

    private String escape(String value) {
        return value == null ? "" : value
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String format(double value) {
        return String.format(java.util.Locale.US, "%.2f", value);
    }
}
