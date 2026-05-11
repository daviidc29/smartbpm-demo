package com.smartbpm.demo.service.ai;

import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String build(String narrative, String ragText) {
        return """
                Generate a controlled intermediate JSON for a BPMN process.
                Constraints:
                - Do NOT output BPMN XML.
                - Output JSON only.
                - Fields required:
                  processName, roles, startEvent, endEvent, tasks, decisions, sequenceFlows, assumptions, warnings.
                - A task must contain: id, name, role, order, automated.
                - A decision must contain: id, name, order, type, mergeId, branches.
                - A branch must contain: id, condition, targetRef.
                - A sequence flow must contain: id, sourceRef, targetRef, conditionLabel.
                - startEvent and endEvent must be objects containing: id, name.
                - Prefer clean verb + object labels.
                - The labels (processName, task names, decision names, branches) MUST be in Spanish.
                - Keep the graph connected.
                - CRITICAL: The process MUST be a SINGLE CONTINUOUS CHAIN from startEvent to endEvent.
                - CRITICAL: NO HANGING NODES. Every task MUST have an outgoing flow that eventually leads to the endEvent.
                - CRITICAL: The endEvent MUST have at least one incoming flow. Every task MUST have exactly ONE incoming flow and exactly ONE outgoing flow.
                - CRITICAL: If you need to branch or merge flows, you MUST use a Gateway. NO task should have multiple incoming or outgoing sequence flows.
                - CRITICAL: Merging gateways (joins) MUST have between 2 and 3 incoming flows. Diverging gateways (splits) MUST have between 2 and 3 outgoing branches.
                - CRITICAL: If the narrative is linear, generate a purely linear process. Only create decisions if the text explicitly describes a condition or branch (e.g. "if X, then Y").

                Narrative:
                %s

                RAG Context:
                %s
                """.formatted(narrative, ragText);
    }
}
