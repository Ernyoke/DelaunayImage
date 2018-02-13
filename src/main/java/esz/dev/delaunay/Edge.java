package esz.dev.delaunay;

import org.opencv.core.Point;

public class Edge {

    private Point a;
    private Point b;

    private static final double d = 0.01;

    public Edge(Point a, Point b) {
        this.a = a;
        this.b = b;
    }

    public Point getA() {
        return a;
    }

    public Point getB() {
        return b;
    }

    public double getLength() {
        return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
    }

    public boolean containsVertex(Point vertex) {
        return ((Math.abs(this.a.x - vertex.x) < d) && (Math.abs(this.a.y - vertex.y) < d)) ||
                ((Math.abs(this.b.x - vertex.x) < d) && (Math.abs(this.b.y - vertex.y) < d));
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Edge) {
            Edge otherEdge = (Edge) other;
            return ((Math.abs(this.a.x - otherEdge.getA().x) < d && Math.abs(this.a.y - otherEdge.getA().y) < d) && (Math.abs(this.b.x - otherEdge.getB().x) < d && Math.abs(this.b.y - otherEdge.getB().y) < d)) ||
                    ((Math.abs(this.b.x - otherEdge.getA().x) < d && Math.abs(this.b.y - otherEdge.getA().y) < d) && (Math.abs(this.a.x - otherEdge.getB().x) < d && Math.abs(this.a.y - otherEdge.getB().y) < d));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * (int)(a.x + a.y + b.x + b.y);
    }
}
