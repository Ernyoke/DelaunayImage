package delaunay;

import esz.dev.delaunay.bowyerWatson.Edge;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opencv.core.Point;

public class EdgeTest {
    @Test
    public void equalEdges() {
        Edge a = new Edge(new Point(1.0, 5.0), new Point(7.0, 10.0));
        Edge b = new Edge(new Point(1.0, 5.0), new Point(7.0, 10.0));
        Assertions.assertEquals(a.equals(b), true);
    }

    @Test
    public void equalFlippedEdges() {
        Edge a = new Edge(new Point(1.0, 5.0), new Point(7.0, 10.0));
        Edge b = new Edge(new Point(7.0, 10.0), new Point(1.0, 5.0));
        Assertions.assertEquals(a.equals(b), true);
    }
}
