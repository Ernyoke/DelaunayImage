package esz.dev.delaunay.imgwriter;

import esz.dev.delaunay.Triangle;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;

public abstract class ImageCreator {

    protected Mat originalImage;
    protected ArrayList<Triangle> triangles;
    protected String outputPath;
    protected boolean wireFrame;

    protected AbstractFillColor fillColor;

    public ImageCreator(ArrayList<Triangle> triangles, Mat originalImage, AbstractFillColor fillColor, String outputPath, boolean wireFrame) {
        this.originalImage = originalImage;
        this.triangles = triangles;
        this.fillColor = fillColor;
        this.outputPath = outputPath;
        this.wireFrame = wireFrame;
    }

    public void createImageFromTriangles() {
        triangles.forEach(triangle -> {
            Point[] vertices = triangle.getVertices();
            for (int i = 0; i < vertices.length; ++i) {
                vertices[i] = new Point(vertices[i].y, vertices[i].x);
            }
            createTriangle(vertices);
        });
        writeToFile();
    }

    public abstract void createTriangle(Point[] vertices);
    protected abstract void writeToFile();
}