package esz.dev.delaunay.delaunator;

import esz.dev.delaunay.DelaunayRunner;
import org.opencv.core.Point;
import org.opencv.core.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DelaunatorRunner implements DelaunayRunner {
    @Override
    public List<List<Point>> run(List<Point> edgePoints, Size imageSize, boolean deleteBorder) {
        List<DPoint> points = edgePoints.stream().map(point -> new DPoint(point.x, point.y))
                .collect(Collectors.toCollection(ArrayList::new));
        List<DPoint> superTriangle = createInitialSuperTriangle(imageSize);
        points.addAll(superTriangle);

        List<DTriangle> triangles = new Delaunator(points).getTriangles();

        if (deleteBorder) {
            triangles = triangles.stream().filter(triangle -> superTriangle.stream()
                            .noneMatch(st -> List.of(triangle.a, triangle.b, triangle.c).contains(st)))
                    .toList();
        }

        return triangles.stream().map(triangle -> List.of(
                new Point(triangle.a.x, triangle.a.y),
                new Point(triangle.b.x, triangle.b.y),
                new Point(triangle.c.x, triangle.c.y)
        )).toList();
    }

    private List<DPoint> createInitialSuperTriangle(Size size) {
        DPoint a = new DPoint(0.0, 0.0);
        DPoint b = new DPoint(size.height - 1, 0.0);
        DPoint c = new DPoint(size.height - 1, size.width - 1);
        DPoint d = new DPoint(0.0, size.width - 1);
        return List.of(a, b, c, d);
    }
}
