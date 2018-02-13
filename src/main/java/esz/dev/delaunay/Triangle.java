package esz.dev.delaunay;

import org.opencv.core.Point;

public class Triangle {

    private Edge ab;
    private Edge bc;
    private Edge ca;
    private Circle circumCircle;

    public Triangle(Point a, Point b, Point c) {
        this.ab = new Edge(a, b);
        this.bc = new Edge(b, c);
        this.ca = new Edge(c, a);

        calculateCircumCircle(a, b, c);
    }

    // https://en.wikipedia.org/wiki/Circumscribed_circle#Triangles
    private void calculateCircumCircle(Point a, Point b, Point c) {
        double d = 2 * (a.x * (b.y - c.y) + b.x * (c.y - a.y) + c.x * (a.y - b.y));
        double centerX = 1.0 / d * ((a.x * a.x + a.y * a.y) * (b.y - c.y) + (b.x * b.x + b.y * b.y) * (c.y - a.y) + (c.x * c.x + c.y * c.y) * (a.y - b.y));
        double centerY = 1.0 / d * ((a.x * a.x + a.y * a.y) * (c.x - b.x) + (b.x * b.x + b.y * b.y) * (a.x - c.x) + (c.x * c.x + c.y * c.y) * (b.x - a.x));

        Point bSign = new Point(b.x - a.x, b.y - a.y);
        Point cSign = new Point(c.x - a.x, c.y - a.y);
        double dSign = 2 * (bSign.x * cSign.y - bSign.y * cSign.x);
        double centerXSign = 1.0 / dSign * (cSign.y * (bSign.x * bSign.x + bSign.y * bSign.y) - bSign.y * (cSign.x * cSign.x + cSign.y * cSign.y));
        double centerYSign = 1.0 / dSign * (bSign.x * (cSign.x * cSign.x + cSign.y * cSign.y) - cSign.x * (bSign.x * bSign.x + bSign.y * bSign.y));

        double r = Math.sqrt(centerXSign * centerXSign + centerYSign * centerYSign);
        this.circumCircle = new Circle(new Point(centerX, centerY), r);
    }

    public Circle getCircumCircle() {
        return circumCircle;
    }

    public Edge[] getEdges() {
        return new Edge[] {ab, bc, ca};
    }

    public boolean containsEdge(Edge edge) {
        return ab.equals(edge) || bc.equals(edge) || ca.equals(edge);
    }

    public boolean containsVertex(Point vertex) {
        return ab.containsVertex(vertex) || bc.containsVertex(vertex) || ca.containsVertex(vertex);
    }

    public Point[] getVertices() {
        return new Point[] {ab.getA(), bc.getA(), ca.getA()};
    }
}
