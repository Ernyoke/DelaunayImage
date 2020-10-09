package esz.dev.delaunay;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.opencv.core.Point;

@Getter
@RequiredArgsConstructor
public class Circle {
    private final Point center;
    private final double radius;

    public double getRadius() {
        return radius;
    }

    private double distanceFromCenter(Point point) {
        double x = center.x - point.x;
        double y = center.y - point.y;
        return Math.sqrt(x * x + y * y);
    }

    public boolean isInside(Point point) {
        return distanceFromCenter(point) < radius;
    }
}
