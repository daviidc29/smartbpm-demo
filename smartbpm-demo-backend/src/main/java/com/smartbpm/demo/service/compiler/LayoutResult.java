package com.smartbpm.demo.service.compiler;

import java.util.List;
import java.util.Map;

public record LayoutResult(
        Map<String, ShapeBounds> shapes,
        Map<String, List<Point>> edges) {

    public record ShapeBounds(String id, double x, double y, double width, double height) {
        public double left() { return x; }
        public double right() { return x + width; }
        public double centerX() { return x + width / 2.0; }
        public double centerY() { return y + height / 2.0; }
    }

    public record Point(double x, double y) {
    }
}
