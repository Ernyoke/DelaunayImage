package esz.dev.delaunay.imgcreator;

import esz.dev.delaunay.Triangle;
import esz.dev.delaunay.imgcreator.fillcolor.FillColorInterface;
import lombok.RequiredArgsConstructor;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public abstract class ImageCreator {
    protected final List<Triangle> triangles;
    protected final Mat originalImage;
    protected final String outputPath;
    protected final boolean wireFrame;

    protected final FillColorInterface fillColor;

    public void createImageFromTriangles() throws IOException {
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

    abstract void writeToFile() throws IOException;
}
