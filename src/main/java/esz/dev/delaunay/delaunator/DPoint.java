package esz.dev.delaunay.delaunator;

public class DPoint implements Comparable<DPoint> {

    public double x;
    public double y;
    private Integer hash = null;

    public DPoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double x() {
        return x;
    }

    public double y() {
        return y;
    }

    @Override
    public int compareTo(DPoint p) {
        return this.x != p.x ? Double.compare(this.x, p.x) : Double.compare(this.y, p.y);
    }

    @Override
    public String toString() {
        return "p[" + x + ", " + y + "]";
    }

    @Override
    public int hashCode() {
        if (hash != null) {
            return hash;
        }
        return hash = hash(x, y);
    }

    public static int hash(double x, double y) {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        return prime * result + (int) (temp ^ (temp >>> 32));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        DPoint a = this;
        DPoint b = (DPoint) obj;
        return a.x == b.x && a.y == b.y;
    }
}
