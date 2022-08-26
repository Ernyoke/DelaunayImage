package esz.dev.delaunay;

import org.opencv.core.Point;

public record Circle(Point center, double radius) {

    private double distanceFromCenter(Point point) {
        double x = center.x - point.x;
        double y = center.y - point.y;
        return Math.sqrt(x * x + y * y);
    }

    public boolean isInside(Point point) {
        return distanceFromCenter(point) < radius;
    }
}
