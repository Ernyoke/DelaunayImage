package esz.dev.delaunay.delaunator;

public class DEdge {

    public DPoint a;
    public DPoint b;

    public DEdge(DPoint a, DPoint b) {
        boolean swap = 0 < a.compareTo(b);
        this.a = swap ? b : a;
        this.b = swap ? a : b;
    }

    public DTriangle A;
    public DTriangle B;

    public DTriangle[] getWing() {
        if (A != null && B != null) {
            return new DTriangle[]{A, B};
        }

        return new DTriangle[]{A};
    }

    public void wing(DTriangle t) {
        if (this.A == null) {
            this.A = t;
        } else if (this.B == null) {
            this.B = t;
        } else {
            System.err.println("[ERR] error state in edge's wing triangle ...");
        }
    }

    @Override
    public String toString() {
        return "e[" + a + " - " + b + "]";
    }

    Integer hash = null;

    @Override
    public int hashCode() {
        if (hash != null) {
            return hash;
        }
        return hash = hash(a, b);
    }

    public static int hash(DPoint a, DPoint b) {
        int ahash = a.hashCode();
        int bhash = b.hashCode();
        return ahash * 31 + bhash;
    }

    public boolean equals(DPoint a, DPoint b) {
        if (this.a.equals(a) && this.b.equals(b)) {
            return true;
        }
        if (this.a.equals(b) && this.b.equals(a)) {
            return true;
        }
        return false;
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
        DEdge A = this;
        DEdge B = (DEdge) obj;

        return (A.a.equals(B.a) && A.b.equals(B.b)) || (A.a.equals(B.b) && A.b.equals(B.a));
    }
}
