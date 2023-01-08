package esz.dev.delaunay.bowyerWatson;

import esz.dev.delaunay.DelaunayRunner;
import org.opencv.core.Point;
import org.opencv.core.Size;

import java.util.List;

public class Runner implements DelaunayRunner {
    @Override
    public List<List<Point>> run(List<Point> edgePoints, Size imageSize, boolean deleteBorder) {
        return new Delaunay().bowyerWatson(edgePoints, imageSize, deleteBorder);
    }

//    private List<Triangle> createInitialSuperTriangle(Size size) {
//        Point a = new Point(0.0, 0.0);
//        Point b = new Point(size.height - 1, 0.0);
//        Point c = new Point(size.height - 1, size.width - 1);
//        Point d = new Point(0.0, size.width - 1);
//        return List.of(new Triangle(a, b, c), new Triangle(a, c, d));
//    }
}
