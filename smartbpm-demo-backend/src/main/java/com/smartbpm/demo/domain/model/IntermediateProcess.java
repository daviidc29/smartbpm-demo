package com.smartbpm.demo.domain.model;

import java.util.ArrayList;
import java.util.List;

public record IntermediateProcess(
        String processName,
        List<String> roles,
        EventRef startEvent,
        EventRef endEvent,
        List<ProcessTask> tasks,
        List<ProcessDecision> decisions,
        List<SequenceFlowRef> sequenceFlows,
        List<String> assumptions,
        List<String> warnings) {

    public IntermediateProcess {
        roles = roles == null ? new ArrayList<>() : roles;
        tasks = tasks == null ? new ArrayList<>() : tasks;
        decisions = decisions == null ? new ArrayList<>() : decisions;
        sequenceFlows = sequenceFlows == null ? new ArrayList<>() : sequenceFlows;
        assumptions = assumptions == null ? new ArrayList<>() : assumptions;
        warnings = warnings == null ? new ArrayList<>() : warnings;
    }
}
