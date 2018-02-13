package esz.dev.delaunay;

import org.opencv.core.Point;

public class Circle {

    private Point center;
    private double radius;

    public Circle(Point center, double radius) {
        this.center = center;
        this.radius = radius;
    }

    public Point getCenter() {
        return center;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
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
