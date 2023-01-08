package esz.dev.delaunay;

import org.opencv.core.Point;
import org.opencv.core.Size;

import java.util.List;

public interface DelaunayRunner {
    List<List<Point>> run(List<Point> edgePoints, Size imageSize, boolean deleteBorder);
}
