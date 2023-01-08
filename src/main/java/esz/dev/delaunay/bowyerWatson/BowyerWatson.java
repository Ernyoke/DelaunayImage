package esz.dev.delaunay.bowyerWatson;

import org.opencv.core.Point;

import java.util.*;
import java.util.stream.Collectors;

public interface BowyerWatson {

    // https://en.wikipedia.org/wiki/Bowyer%E2%80%93Watson_algorithm
    static List<List<Point>> bowyerWatson(List<Triangle> initialTriangles, List<Point> edgePoints) {
        List<Triangle> triangles = new ArrayList<>(initialTriangles);

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

        return triangles.stream().map(triangle -> Arrays.asList(triangle.getVertices())).toList();
    }
}
