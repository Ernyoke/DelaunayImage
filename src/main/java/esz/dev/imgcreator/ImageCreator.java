package esz.dev.imgcreator;

import esz.dev.delaunay.Triangle;
import esz.dev.imgcreator.fillcolor.FillColorInterface;
import lombok.RequiredArgsConstructor;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public abstract class ImageCreator {
    protected final List<List<Point>> triangles;
    protected final Mat originalImage;
    protected final String outputPath;
    protected final boolean wireFrame;

    protected final FillColorInterface fillColor;

    public void createImageFromTriangles() throws IOException {
        triangles.forEach(triangle -> {
            createTriangle(triangle.stream().map(vertex -> new Point(vertex.y, vertex.x)).toList());
        });
        writeToFile();
    }

    public abstract void createTriangle(List<Point> vertices);

    abstract void writeToFile() throws IOException;
}
