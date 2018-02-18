package esz.dev.delaunay.imgcreator;

import esz.dev.delaunay.Triangle;
import esz.dev.delaunay.imgcreator.fillcolor.FillColorInterface;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;

public abstract class ImageCreator {

    Mat originalImage;
    ArrayList<Triangle> triangles;
    String outputPath;
    boolean wireFrame;

    FillColorInterface fillColor;

    ImageCreator(ArrayList<Triangle> triangles, Mat originalImage, FillColorInterface fillColor, String outputPath, boolean wireFrame) {
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
    abstract void writeToFile();
}
