package delaunay;

import esz.dev.delaunay.Circle;
import esz.dev.delaunay.Edge;
import esz.dev.delaunay.Triangle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opencv.core.Point;

public class TriangleTest {
    @Test
    public void circumCricleTest() {
        Triangle triangle = new Triangle(new Point(10.0, 10.0), new Point(60, 60), new Point(110, 10));
        Edge[] edges = triangle.getEdges();
        Assertions.assertEquals(edges[0].getLength(), 70.71, 0.01);
        Assertions.assertEquals(edges[1].getLength(), 70.71, 0.01);
        Assertions.assertEquals(edges[2].getLength(), 100.0, 0.01);
        Circle circle = triangle.getCircumCircle();
        double radius = circle.getRadius();
        Assertions.assertEquals(50.0, radius, 0.01);
    }
}
