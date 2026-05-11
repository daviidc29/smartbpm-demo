package com.smartbpm.demo.service.compiler;

import com.smartbpm.demo.domain.model.IntermediateProcess;
import com.smartbpm.demo.domain.model.ProcessDecision;
import com.smartbpm.demo.domain.model.ProcessTask;
import com.smartbpm.demo.domain.model.SequenceFlowRef;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LayoutCalculator {

    private static final double START_X = 80;
    private static final double COLUMN_WIDTH = 180;
    private static final double BASE_Y = 180;
    private static final double ROW_HEIGHT = 110;
    private static final double TASK_WIDTH = 120;
    private static final double TASK_HEIGHT = 80;
    private static final double EVENT_SIZE = 36;
    private static final double GATEWAY_SIZE = 50;

    public LayoutResult calculate(IntermediateProcess process) {
        Map<String, LayoutResult.ShapeBounds> shapes = new HashMap<>();
        Map<String, List<LayoutResult.Point>> edges = new HashMap<>();

        Map<String, Integer> roleIndex = new HashMap<>();
        int roleCounter = 0;
        for (String role : process.roles()) {
            roleIndex.put(role, roleCounter++);
        }
        roleIndex.putIfAbsent("Sistema", roleCounter);
        roleIndex.putIfAbsent("SYSTEM", roleCounter);
        final int defaultRoleIndex = roleCounter;

        shapes.put(process.startEvent().id(), new LayoutResult.ShapeBounds(
                process.startEvent().id(), START_X, BASE_Y, EVENT_SIZE, EVENT_SIZE));

        List<ProcessTask> orderedTasks = process.tasks().stream()
                .sorted(Comparator.comparingInt(ProcessTask::order))
                .toList();

        Map<Integer, Integer> collisionCounter = new HashMap<>();
        for (ProcessTask task : orderedTasks) {
            double x = START_X + (task.order() * COLUMN_WIDTH);
            int bucket = roleIndex.getOrDefault(task.role(), defaultRoleIndex);
            int collisions = collisionCounter.getOrDefault(task.order() * 100 + bucket, 0);
            collisionCounter.put(task.order() * 100 + bucket, collisions + 1);
            double y = BASE_Y + (bucket * ROW_HEIGHT) + (collisions * 60);
            shapes.put(task.id(), new LayoutResult.ShapeBounds(task.id(), x, y, TASK_WIDTH, TASK_HEIGHT));
        }

        for (ProcessDecision decision : process.decisions()) {
            double x = START_X + (decision.order() * COLUMN_WIDTH);
            double y = BASE_Y - 70; // Position gateways above the tasks to avoid overlap
            shapes.put(decision.id(), new LayoutResult.ShapeBounds(decision.id(), x, y, GATEWAY_SIZE, GATEWAY_SIZE));
            
            boolean used = process.sequenceFlows().stream()
                    .anyMatch(f -> decision.mergeId().equals(f.targetRef()) || decision.mergeId().equals(f.sourceRef()));
            if (used) {
                shapes.put(decision.mergeId(), new LayoutResult.ShapeBounds(
                        decision.mergeId(), START_X + ((decision.order() + 2) * COLUMN_WIDTH), y, GATEWAY_SIZE, GATEWAY_SIZE));
            }
        }

        int maxOrder = process.tasks().stream().mapToInt(ProcessTask::order).max().orElse(1);
        for (ProcessDecision decision : process.decisions()) {
            maxOrder = Math.max(maxOrder, decision.order() + 2);
        }

        shapes.put(process.endEvent().id(), new LayoutResult.ShapeBounds(
                process.endEvent().id(), START_X + ((maxOrder + 1) * COLUMN_WIDTH), BASE_Y, EVENT_SIZE, EVENT_SIZE));

        for (SequenceFlowRef flow : process.sequenceFlows()) {
            LayoutResult.ShapeBounds source = shapes.get(flow.sourceRef());
            LayoutResult.ShapeBounds target = shapes.get(flow.targetRef());
            if (source == null || target == null) {
                continue;
            }
            List<LayoutResult.Point> waypoints = new ArrayList<>();
            double startX = source.right();
            double startY = source.centerY();
            double endX = target.left();
            double endY = target.centerY();
            double midX = (startX + endX) / 2.0;
            waypoints.add(new LayoutResult.Point(startX, startY));
            if (Math.abs(startY - endY) < 5) {
                waypoints.add(new LayoutResult.Point(endX, endY));
            } else {
                double turningX = startX + 20;
                waypoints.add(new LayoutResult.Point(turningX, startY));
                waypoints.add(new LayoutResult.Point(turningX, endY));
                waypoints.add(new LayoutResult.Point(endX, endY));
            }
            edges.put(flow.id(), waypoints);
        }

        return new LayoutResult(shapes, edges);
    }
}
