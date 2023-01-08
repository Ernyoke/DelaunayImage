package esz.dev.delaunay.delaunator;

import java.awt.geom.Point2D;
import java.util.*;

/**
 * This code is java port of delaunator. Delaunator triangulation algorithm is
 * incredibly fast and robust library for point of 2D clouds. For more
 * information, go to the original site https://github.com/mapbox/delaunator.
 *
 * @author 14mhz@hanmail.net
 * @author zookim@waveware.co.kr
 */
public class Delaunator {

    private static final double EPSILON = Math.pow(2, -52);
    private final int[] EDGE_STACK = new int[512];

    public int[] triangles;
    public int[] halfedges;
    public DPoint[] points;

    private int hashSize;
    private int[] hullPrev;
    private int[] hullTria;

    private double cx;
    private double cy;

    private int trianglesLen;
    private double[] coords;
    private int hullStart;
    public int[] hull;

    public Delaunator(List<DPoint> points) {
        this(unique(points));
    }

    public Delaunator(DPoint[] points) {
        if (points.length < 3) {
            System.err.println("Need at least 3 points");
            return;
        }

        points = unique(Arrays.asList(points));

        this.points = points;
        this.coords = new double[points.length * 2];

        for (int i = 0; i < points.length; i++) {
            DPoint p = points[i];
            coords[2 * i] = p.x;
            coords[2 * i + 1] = p.y;
        }

        int n = coords.length >> 1;
        int maxTriangles = 2 * n - 5;

        triangles = new int[maxTriangles * 3];

        halfedges = new int[maxTriangles * 3];
        hashSize = (int) Math.ceil(Math.sqrt(n));

        hullPrev = new int[n];
        int[] hullNext = new int[n];
        hullTria = new int[n];
        int[] hullHash = new int[hashSize];

        int[] ids = new int[n];

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for (int i = 0; i < n; i++) {
            double x = coords[2 * i];
            double y = coords[2 * i + 1];
            if (x < minX) {
                minX = x;
            }
            if (y < minY) {
                minY = y;
            }
            if (x > maxX) {
                maxX = x;
            }
            if (y > maxY) {
                maxY = y;
            }
            ids[i] = i;
        }

        double cx = (minX + maxX) / 2;
        double cy = (minY + maxY) / 2;

        double minDist = Double.MAX_VALUE;
        int i0 = 0;
        int i1 = 0;
        int i2 = 0;

        for (int i = 0; i < n; i++) {// pick a seed point close to the center
            double d = dist(cx, cy, coords[2 * i], coords[2 * i + 1]);
            if (d < minDist) {
                i0 = i;
                minDist = d;
            }
        }
        double i0x = coords[2 * i0];
        double i0y = coords[2 * i0 + 1];

        minDist = Double.MAX_VALUE;

        for (int i = 0; i < n; i++) {// find the point closest to the seed
            if (i == i0) {
                continue;
            }
            double d = dist(i0x, i0y, coords[2 * i], coords[2 * i + 1]);
            if (d < minDist && d > 0) {
                i1 = i;
                minDist = d;
            }
        }

        double i1x = coords[2 * i1];
        double i1y = coords[2 * i1 + 1];
        double minRadius = Double.MAX_VALUE;

        for (int i = 0; i < n; i++) {// find the third point which forms the smallest circumcircle with the first two
            if (i == i0 || i == i1) {
                continue;
            }
            double r = circumradius(i0x, i0y, i1x, i1y, coords[2 * i], coords[2 * i + 1]);
            if (r < minRadius) {
                i2 = i;
                minRadius = r;
            }
        }
        double i2x = coords[2 * i2];
        double i2y = coords[2 * i2 + 1];

        if (minRadius == Double.MAX_VALUE) {
            System.err.println("No Delaunay triangulation exists for this input.");
            return;
        }

        if (orient(i0x, i0y, i1x, i1y, i2x, i2y)) {
            int i = i1;
            double x = i1x;
            double y = i1y;
            i1 = i2;
            i1x = i2x;
            i1y = i2y;
            i2 = i;
            i2x = x;
            i2y = y;
        }

        DPoint center = circumcenter(i0x, i0y, i1x, i1y, i2x, i2y);
        this.cx = center.x;
        this.cy = center.y;

        double[] dists = new double[n];
        for (int i = 0; i < n; i++) {
            dists[i] = dist(coords[2 * i], coords[2 * i + 1], center.x, center.y);
        }

        {// sort the points by distance from the seed triangle circumcenter
            quicksort(ids, dists, 0, n - 1);
        }

        // set up the seed triangle as the starting hull
        hullStart = i0;
        int hullSize = 3;

        hullNext[i0] = hullPrev[i2] = i1;
        hullNext[i1] = hullPrev[i0] = i2;
        hullNext[i2] = hullPrev[i1] = i0;

        hullTria[i0] = 0;
        hullTria[i1] = 1;
        hullTria[i2] = 2;

        hullHash[hashKey(i0x, i0y)] = i0;
        hullHash[hashKey(i1x, i1y)] = i1;
        hullHash[hashKey(i2x, i2y)] = i2;

        trianglesLen = 0;
        addTriangle(i0, i1, i2, -1, -1, -1);

        double xp = 0;
        double yp = 0;

        for (int k = 0; k < ids.length; k++) {
            int i = ids[k];
            double x = coords[2 * i];
            double y = coords[2 * i + 1];

            // skip near-duplicate points
            if (k > 0 && Math.abs(x - xp) <= EPSILON && Math.abs(y - yp) <= EPSILON) {
                continue;
            }
            xp = x;
            yp = y;

            // skip seed triangle points
            if (i == i0 || i == i1 || i == i2) {
                continue;
            }

            // find a visible edge on the convex hull using edge hash
            int start = 0;
            for (int j = 0; j < hashSize; j++) {
                int key = hashKey(x, y);
                start = hullHash[(key + j) % hashSize];
                if (start != -1 && start != hullNext[start]) {
                    break;
                }
            }

            start = hullPrev[start];
            int e = start;
            int q = hullNext[e];

            while (!orient(x, y, coords[2 * e], coords[2 * e + 1], coords[2 * q], coords[2 * q + 1])) {
                e = q;
                if (e == start) {
                    e = Integer.MAX_VALUE;
                    break;
                }

                q = hullNext[e];
            }

            if (e == Integer.MAX_VALUE) {
                continue; // likely a near-duplicate point; skip it
            }

            // add the first triangle from the point
            int t = addTriangle(e, i, hullNext[e], -1, -1, hullTria[e]);

            // recursively flip triangles from the point until they satisfy the Delaunay
            // condition
            hullTria[i] = legalize(t + 2);
            hullTria[e] = t; // keep track of boundary triangles on the hull
            hullSize++;

            // walk forward through the hull, adding more triangles and flipping recursively
            int next = hullNext[e];
            q = hullNext[next];

            while (orient(x, y, coords[2 * next], coords[2 * next + 1], coords[2 * q], coords[2 * q + 1])) {
                t = addTriangle(next, i, q, hullTria[i], -1, hullTria[next]);
                hullTria[i] = legalize(t + 2);
                hullNext[next] = next; // mark as removed
                hullSize--;
                next = q;

                q = hullNext[next];
            }

            if (e == start) {// walk backward from the other side, adding more triangles and flipping
                q = hullPrev[e];

                while (orient(x, y, coords[2 * q], coords[2 * q + 1], coords[2 * e], coords[2 * e + 1])) {
                    t = addTriangle(q, i, e, -1, hullTria[e], hullTria[q]);
                    legalize(t + 2);
                    hullTria[q] = t;
                    hullNext[e] = e; // mark as removed
                    hullSize--;
                    e = q;
                    q = hullPrev[e];
                }
            }

            // update the hull indices
            hullStart = hullPrev[i] = e;
            hullNext[e] = hullPrev[next] = i;
            hullNext[i] = next;

            // save the two new edges in the hash table
            hullHash[hashKey(x, y)] = i;
            hullHash[hashKey(coords[2 * e], coords[2 * e + 1])] = e;
        }

        hull = new int[hullSize];
        int s = hullStart;
        for (int i = 0; i < hullSize; i++) {
            hull[i] = s;
            s = hullNext[s];
        }

        hullPrev = hullNext = hullTria = null; // get rid of temporary arrays

        // trim typed triangle mesh arrays
        int[] tempTriangles = new int[trianglesLen];
        System.arraycopy(triangles, 0, tempTriangles, 0, trianglesLen);
        triangles = tempTriangles;

        int[] tempHalfedges = new int[trianglesLen];
        System.arraycopy(halfedges, 0, tempHalfedges, 0, trianglesLen);
        halfedges = tempHalfedges;

        generate();
    }

    private List<DTriangle> trias = null;
    private List<DEdge> edges = null;
    private List<DPoint> poinz = null;
    private List<DEdge> hulls = null;
    private List<DEdge> voron = null;
    private List<DEdge> vhull = null;

    public List<DTriangle> getTriangles() {
        return trias;
    }

    public List<DEdge> getEdges() {
        return edges;
    }

    public List<DPoint> getPoints() {
        return poinz;
    }

    public List<DEdge> getHullEdges() {
        return hulls;
    }

    public List<DEdge> getVoronoiEdges() {
        return voron;
    }

    public List<DEdge> getVoronoiHullEdges() {
        return vhull;
    }

    public static List<DPoint> convert(List<Point2D> points) {
        List<DPoint> lst = new ArrayList<>();
        points.forEach(p -> lst.add(new DPoint(p.getX(), p.getY())));
        return lst;
    }

    public static DPoint[] unique(List<DPoint> points) {
        Set<DPoint> unq = new LinkedHashSet<>();
        for (DPoint v : points) {
            if (unq.contains(v)) {
                while (!unq.contains(v)) {
                    System.err.printf("[INF] found duplicated point (%f, %f), fix it will be plus +1e-6... \n", (float) v.x, (float) v.y);
                    v = new DPoint(v.x() + 1e-6, v.y() + 1e-6);
                }
            }
            unq.add(v);
        }

        return unq.toArray(new DPoint[]{});
    }

    private void generate() {
        Map<DEdge, DEdge> unq_edges = new HashMap<>();
        Map<DTriangle, DTriangle> unq_trias = new HashMap<>();

        for (int n = 0; triangles != null && n < triangles.length / 3; n++) {
            int i = triangles[3 * n + 0];
            int j = triangles[3 * n + 1];
            int k = triangles[3 * n + 2];
            DPoint a = points[i];
            DPoint b = points[j];
            DPoint c = points[k];

            DPoint[] tmp = {a, b, c};
            Arrays.sort(tmp);
            a = tmp[0];
            b = tmp[1];
            c = tmp[2];

            DEdge ab = new DEdge(a, b);
            DEdge bc = new DEdge(b, c);
            DEdge ca = new DEdge(c, a);

            if (unq_edges.containsKey(ab)) {
                ab = unq_edges.get(ab);
            } else {
                unq_edges.put(ab, ab);
            }

            if (unq_edges.containsKey(bc)) {
                bc = unq_edges.get(bc);
            } else {
                unq_edges.put(bc, bc);
            }

            if (unq_edges.containsKey(ca)) {
                ca = unq_edges.get(ca);
            } else {
                unq_edges.put(ca, ca);
            }

            DTriangle t = new DTriangle(a, b, c);
            if (unq_trias.containsKey(t)) {
                System.err.println("[ERR] duplicated triangle " + t + " vs " + unq_trias.get(t));
                t = unq_trias.get(t);
            } else {
                unq_trias.put(t, t);
            }

            t.edges(ab, bc, ca);
            ab.wing(t);
            bc.wing(t);
            ca.wing(t);
        }

        ////

        List<DEdge> hulledges = new ArrayList<>();
        for (int i = 0; i < hull.length; i++) {
            DPoint a = points[hull[i]];
            DPoint b = points[hull[(i + 1) % hull.length]];
            DEdge e = new DEdge(a, b);

            if (unq_edges.containsKey(e)) {
                e = unq_edges.get(e);
            } else {
                System.err.println("[ERR] cannot found valid edge " + e);
            }
            hulledges.add(e);
        }

        ////

        List<DEdge> voronoiedges = new ArrayList<>();
        List<DEdge> voronoihulledges = new ArrayList<>();
        Set<Integer> tmp = new LinkedHashSet<Integer>();
        for (int i = 0; i < triangles.length; i++) {
            int id = triangles[nextHalfEdge(i)];
            if (!tmp.contains(id)) {
                tmp.add(id);
                List<Integer> edges = edgesAroundPoint(i);
                List<DPoint> point = new ArrayList<>();

                for (Integer edge : edges) {
                    DPoint[] pnt = getTrianglePoints(triangleOfEdge(edge));
                    DPoint cen = getCentroid(pnt);
                    if (cen == null) {
                        continue;
                    }
                    point.add(cen);
                }

                for (int j = 0; j < point.size(); j++) {
                    DPoint a = point.get(j);
                    DPoint b = point.get((j + 1) % point.size());
                    DEdge e = new DEdge(a, b);

                    voronoiedges.add(e);
                }
            }
        }

        this.trias = new ArrayList<DTriangle>(unq_trias.values());
        this.edges = new ArrayList<DEdge>(unq_edges.values());
        this.poinz = Arrays.asList(points);
        this.hulls = hulledges;
        this.voron = voronoiedges;
        this.vhull = hulledges; // voronoihulledges; //it must be implement !!!
    }

    ///////////////////////////////////////////////////////////////////////////

    private int legalize(int a) {
        int i = 0;
        int ar;

        // recursion eliminated with a fixed-size stack
        while (true) {
            int b = halfedges[a];

            /*
             * if the pair of triangles doesn't satisfy the Delaunay condition (p1 is inside
             * the circumcircle of [p0, pl, pr]), flip them, then do the same check/flip
             * recursively for the new pair of triangles
             *
             * pl pl /||\ / \ al/ || \bl al/ \a / || \ / \ / a||b \ flip /___ar___\ p0\ ||
             * /p1 => p0\---bl---/p1 \ || / \ / ar\ || /br b\ /br \||/ \ / pr pr
             */
            int a0 = a - a % 3;
            ar = a0 + (a + 2) % 3;

            if (b == -1) { // convex hull edge
                if (i == 0) {
                    break;
                }
                a = EDGE_STACK[--i];
                continue;
            }

            int b0 = b - b % 3;
            int al = a0 + (a + 1) % 3;
            int bl = b0 + (b + 2) % 3;

            int p0 = triangles[ar];
            int pr = triangles[a];
            int pl = triangles[al];
            int p1 = triangles[bl];

            boolean illegal = inCircle(coords[2 * p0], coords[2 * p0 + 1], coords[2 * pr], coords[2 * pr + 1], coords[2 * pl],
                    coords[2 * pl + 1], coords[2 * p1], coords[2 * p1 + 1]);

            if (illegal) {
                triangles[a] = p1;
                triangles[b] = p0;

                int hbl = halfedges[bl];

                // edge swapped on the other side of the hull (rare); fix the halfedge reference
                if (hbl == -1) {
                    int e = hullStart;
                    do {
                        if (hullTria[e] == bl) {
                            hullTria[e] = a;
                            break;
                        }
                        e = hullPrev[e];
                    } while (e != hullStart);
                }
                link(a, hbl);
                link(b, halfedges[ar]);
                link(ar, bl);

                int br = b0 + (b + 1) % 3;

                // don't worry about hitting the cap: it can only happen on extremely degenerate
                // input
                if (i < EDGE_STACK.length) {
                    EDGE_STACK[i++] = br;
                }
            } else {
                if (i == 0) {
                    break;
                }
                a = EDGE_STACK[--i];
            }
        }

        return ar;
    }

    private int addTriangle(int i0, int i1, int i2, int a, int b, int c) {
        int t = trianglesLen;

        triangles[t] = i0;
        triangles[t + 1] = i1;
        triangles[t + 2] = i2;

        link(t, a);
        link(t + 1, b);
        link(t + 2, c);

        trianglesLen += 3;
        return t;
    }

    private void link(int a, int b) {
        halfedges[a] = b;
        if (b != -1) {
            halfedges[b] = a;
        }
    }

    private int hashKey(double x, double y) {
        return (int) (Math.floor(pseudoAngle(x - cx, y - cy) * hashSize) % hashSize);
    }

    private static boolean inCircle(double ax, double ay, double bx, double by, double cx, double cy, double px, double py) {
        double dx = ax - px;
        double dy = ay - py;
        double ex = bx - px;
        double ey = by - py;
        double fx = cx - px;
        double fy = cy - py;

        double ap = dx * dx + dy * dy;
        double bp = ex * ex + ey * ey;
        double cp = fx * fx + fy * fy;

        return dx * (ey * cp - bp * fy) - dy * (ex * cp - bp * fx) + ap * (ex * fy - ey * fx) < 0;
    }

    private static double pseudoAngle(double dx, double dy) {
        double p = dx / (Math.abs(dx) + Math.abs(dy));
        return (dy > 0 ? 3 - p : 1 + p) / 4; // [0..1]
    }

    private static void quicksort(int[] ids, double[] dists, int left, int right) {
        if (right - left <= 20) {
            for (int i = left + 1; i <= right; i++) {
                int temp = ids[i];
                double tempDist = dists[temp];
                int j = i - 1;
                while (j >= left && dists[ids[j]] > tempDist) {
                    ids[j + 1] = ids[j--];
                }
                ids[j + 1] = temp;
            }
        } else {
            int median = (left + right) >> 1;
            int i = left + 1;
            int j = right;
            swap(ids, median, i);
            if (dists[ids[left]] > dists[ids[right]]) {
                swap(ids, left, right);
            }
            if (dists[ids[i]] > dists[ids[right]]) {
                swap(ids, i, right);
            }
            if (dists[ids[left]] > dists[ids[i]]) {
                swap(ids, left, i);
            }

            int temp = ids[i];
            double tempDist = dists[temp];
            while (true) {
                do {
                    i++;
                } while (dists[ids[i]] < tempDist);
                do {
                    j--;
                } while (dists[ids[j]] > tempDist);
                if (j < i) {
                    break;
                }
                swap(ids, i, j);
            }
            ids[left + 1] = ids[j];
            ids[j] = temp;

            if (right - i + 1 >= j - left) {
                quicksort(ids, dists, i, right);
                quicksort(ids, dists, left, j - 1);
            } else {
                quicksort(ids, dists, left, j - 1);
                quicksort(ids, dists, i, right);
            }
        }
    }

    private static void swap(int[] arr, int i, int j) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    private static boolean orient(double px, double py, double qx, double qy, double rx, double ry) {
        return (qy - py) * (rx - qx) - (qx - px) * (ry - qy) < 0;
    }

    private static double circumradius(double ax, double ay, double bx, double by, double cx, double cy) {
        double dx = bx - ax;
        double dy = by - ay;
        double ex = cx - ax;
        double ey = cy - ay;
        double bl = dx * dx + dy * dy;
        double cl = ex * ex + ey * ey;
        double d = 0.5 / (dx * ey - dy * ex);
        double x = (ey * bl - dy * cl) * d;
        double y = (dx * cl - ex * bl) * d;
        return x * x + y * y;
    }

    protected static DPoint circumcenter(double ax, double ay, double bx, double by, double cx, double cy) {
        double dx = bx - ax;
        double dy = by - ay;
        double ex = cx - ax;
        double ey = cy - ay;
        double bl = dx * dx + dy * dy;
        double cl = ex * ex + ey * ey;
        double d = 0.5 / (dx * ey - dy * ex);
        double x = ax + (ey * bl - dy * cl) * d;
        double y = ay + (dx * cl - ex * bl) * d;

        return new DPoint(x, y);
    }

    private static double dist(double ax, double ay, double bx, double by) {
        double dx = ax - bx;
        double dy = ay - by;
        return dx * dx + dy * dy;
    }

    ///////////////////////////////////////////////////////////////////////////

    public int[] edgesOfTriangle(int t) {
        return new int[]{3 * t, 3 * t + 1, 3 * t + 2};
    }

    public int triangleOfEdge(int e) {
        return (int) Math.floor(e / 3);
    }

    public int nextHalfEdge(int e) {
        return (e % 3 == 2) ? e - 2 : e + 1;
    }

    public int prevHalfEdge(int e) {
        return (e % 3 == 0) ? e + 2 : e - 1;
    }

    public int[] pointsOfTriangle(int t) {
        int[] e = edgesOfTriangle(t);
        int a = triangles[e[0]];
        int b = triangles[e[1]];
        int c = triangles[e[2]];
        return new int[]{a, b, c};
    }

    public DPoint[] getTrianglePoints(int t) {
        int[] p = pointsOfTriangle(t);
        DPoint a = points[p[0]];
        DPoint b = points[p[1]];
        DPoint c = points[p[2]];

        return new DPoint[]{a, b, c};
    }

    public DPoint[] getEdgesOfTriangle(int t) {
        int[] e = edgesOfTriangle(t);
        DPoint a = points[e[0]];
        DPoint b = points[e[1]];
        DPoint c = points[e[2]];
        return new DPoint[]{a, b, c};
    }

    public DPoint[] GetHullPoints() {
        DPoint[] hullpoint = new DPoint[hull.length];
        for (int i = 0; i < hull.length; i++) {
            hullpoint[i] = points[hull[i]];
        }
        return hullpoint;
    }

    public DPoint getTriangleCircumcenter(int t) {
        DPoint[] vertices = getTrianglePoints(t);
        return getCircumcenter(vertices[0], vertices[1], vertices[2]);
    }

    public DPoint getCentroid(int t) {
        DPoint[] vertices = getTrianglePoints(t);
        return getCentroid(vertices);
    }

    public DPoint getCircumcenter(DPoint a, DPoint b, DPoint c) {
        return circumcenter(a.x, a.y, b.x, b.y, c.x, c.y);
    }

    public DPoint getCentroid(DPoint[] points) {
        double accumulatedArea = 0.0f;
        double centerX = 0.0f;
        double centerY = 0.0f;

        for (int i = 0, j = points.length - 1; i < points.length; j = i++) {
            double temp = points[i].x * points[j].y - points[j].x * points[i].y;
            accumulatedArea += temp;
            centerX += (points[i].x + points[j].x) * temp;
            centerY += (points[i].y + points[j].y) * temp;
        }

        if (Math.abs(accumulatedArea) < 1E-7f) {
            return null; // new DPoint(0, 0); // ???
        }

        accumulatedArea *= 3f;
        return new DPoint(centerX / accumulatedArea, centerY / accumulatedArea);
    }

    public List<Integer> edgesAroundPoint(int start) {
        List<Integer> lst = new ArrayList<>();
        int incoming = start;
        do {
            lst.add(incoming);
            int outgoing = nextHalfEdge(incoming);
            incoming = halfedges[outgoing];
        } while (incoming != -1 && incoming != start);

        return lst;
    }

    public List<Integer> trianglesAdjacentToTriangle(int t) {
        List<Integer> adjacentTriangles = new ArrayList<>();
        int[] triangleEdges = edgesOfTriangle(t);

        for (int e : triangleEdges) {
            int opposite = halfedges[e];
            if (opposite >= 0) {
                adjacentTriangles.add(triangleOfEdge(opposite));
            }
        }
        return adjacentTriangles;
    }
}
