package esz.dev.delaunay.bowyerWatson;

import esz.dev.delaunay.DelaunayRunner;
import org.opencv.core.Point;
import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.List;

public class BowyerWatsonRunner implements DelaunayRunner {
    @Override
    public List<List<Point>> run(List<Point> edgePoints, Size imageSize, boolean deleteBorder) {
        List<Point> points = new ArrayList<>(edgePoints);
        List<Point> superTriangle = createInitialSuperTriangle(imageSize);
        List<List<Point>> triangles = BowyerWatson.bowyerWatson(List.of(
                new Triangle(superTriangle.get(0), superTriangle.get(1), superTriangle.get(2)),
                new Triangle(superTriangle.get(0), superTriangle.get(2), superTriangle.get(3))), points);
        if (deleteBorder) {
            return triangles.stream().filter(vertices -> vertices.stream().noneMatch(superTriangle::contains)).toList();
        }
        return triangles;
    }

    private List<Point> createInitialSuperTriangle(Size size) {
        Point a = new Point(0.0, 0.0);
        Point b = new Point(size.height - 1, 0.0);
        Point c = new Point(size.height - 1, size.width - 1);
        Point d = new Point(0.0, size.width - 1);
        return List.of(a, b, c, d);
    }
}
