package esz.dev.delaunay.bowyerWatson;

import org.opencv.core.Point;
import org.opencv.core.Size;

import java.util.*;
import java.util.stream.Collectors;

public class Delaunay {

    // https://en.wikipedia.org/wiki/Bowyer%E2%80%93Watson_algorithm
    public List<List<Point>> bowyerWatson(List<Point> edgePoints, Size imageSize, boolean deleteBorder) {
        List<Triangle> triangles = new ArrayList<>(createInitialSuperTriangle(imageSize));

        for (Point point : edgePoints) {
            Map<Boolean, List<Triangle>> partitionedTriangles = triangles.stream()
                    .collect(Collectors.partitioningBy(triangle -> triangle.getCircumCircle().isInside(point),
                            Collectors.toCollection(ArrayList<Triangle>::new)));

            List<Triangle> badTriangles = partitionedTriangles.get(true);
            Set<Edge> polygon = new HashSet<>();
            for (Triangle triangle : badTriangles) {
                for (Edge edge : triangle.getEdges()) {
                    boolean sharedEdge = false;
                    for (Triangle otherTriangle : badTriangles) {
                        if (triangle != otherTriangle && otherTriangle.containsEdge(edge)) {
                            sharedEdge = true;
                            break;
                        }
                    }
                    if (!sharedEdge) {
                        polygon.add(edge);
                    }
                }
            }

            List<Triangle> goodTriangles = partitionedTriangles.get(false);
            for (Edge edge : polygon) {
                goodTriangles.add(new Triangle(edge.a(), edge.b(), point));
            }

            triangles = goodTriangles;
        }

        if (deleteBorder) {
            List<Triangle> superTriangle = createInitialSuperTriangle(imageSize);

            return triangles.stream().filter(triangle -> {
                for (Triangle st : superTriangle) {
                    for (Edge edge : triangle.getEdges()) {
                        if (st.containsVertex(edge.a()) || st.containsVertex(edge.b())) {
                            return false;
                        }
                    }
                }
                return true;
            }).map(triangle -> Arrays.asList(triangle.getVertices())).toList();
        }

        return triangles.stream().map(triangle -> Arrays.asList(triangle.getVertices())).toList();
    }

    private List<Triangle> createInitialSuperTriangle(Size size) {
        Point a = new Point(0.0, 0.0);
        Point b = new Point(size.height - 1, 0.0);
        Point c = new Point(size.height - 1, size.width - 1);
        Point d = new Point(0.0, size.width - 1);
        return List.of(new Triangle(a, b, c), new Triangle(a, c, d));
    }
}
