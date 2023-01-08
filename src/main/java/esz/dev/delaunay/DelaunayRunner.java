package esz.dev.delaunay;

import esz.dev.argparse.DelaunayAlgorithm;
import esz.dev.delaunay.bowyerWatson.BowyerWatsonRunner;
import esz.dev.delaunay.delaunator.DelaunatorRunner;
import org.opencv.core.Point;
import org.opencv.core.Size;

import java.util.List;

public interface DelaunayRunner {
    List<List<Point>> run(List<Point> edgePoints, Size imageSize, boolean deleteBorder);

    static DelaunayRunner builder(DelaunayAlgorithm algorithm) {
        return switch (algorithm) {
            case BW -> new BowyerWatsonRunner();
            case DELAUNATOR -> new DelaunatorRunner();
        };
    }
}
