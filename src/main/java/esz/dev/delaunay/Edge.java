package esz.dev.delaunay;

import org.opencv.core.Point;

public record Edge(Point a, Point b) {

    private static final double D = 0.01;

    public double getLength() {
        return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
    }

    public boolean containsVertex(Point vertex) {
        return ((Math.abs(this.a.x - vertex.x) < D) && (Math.abs(this.a.y - vertex.y) < D)) ||
                ((Math.abs(this.b.x - vertex.x) < D) && (Math.abs(this.b.y - vertex.y) < D));
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Edge otherEdge) {
            return ((Math.abs(this.a.x - otherEdge.a().x) < D && Math.abs(this.a.y - otherEdge.a().y) < D) && (Math.abs(this.b.x - otherEdge.b().x) < D && Math.abs(this.b.y - otherEdge.b().y) < D)) ||
                    ((Math.abs(this.b.x - otherEdge.a().x) < D && Math.abs(this.b.y - otherEdge.a().y) < D) && (Math.abs(this.a.x - otherEdge.b().x) < D && Math.abs(this.a.y - otherEdge.b().y) < D));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * (int) (a.x + a.y + b.x + b.y);
    }
}
