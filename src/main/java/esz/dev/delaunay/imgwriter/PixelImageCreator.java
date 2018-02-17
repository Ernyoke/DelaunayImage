package esz.dev.delaunay.imgwriter;

import esz.dev.delaunay.Triangle;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class PixelImageCreator extends ImageCreator {

    private Mat image;

    private int thickness = 1;

    @FunctionalInterface
    private interface Style {
        void draw(Point[] vertices, Scalar pixel);
    }

    private Style style;

    public PixelImageCreator(ArrayList<Triangle> triangles, Mat originalImage, AbstractFillColor fillColor,
                             String outputPath, boolean wireFrame) {
        super(triangles, originalImage, fillColor, outputPath, wireFrame);

        if (wireFrame) {
            style = (Point[] vertices, Scalar pixel) -> {
                Imgproc.line(image, vertices[0], vertices[1], pixel, thickness);
                Imgproc.line(image, vertices[1], vertices[2], pixel, thickness);
                Imgproc.line(image, vertices[2], vertices[0], pixel, thickness);
            };
        } else {
            style = (Point[] vertices, Scalar pixel) -> {
                MatOfPoint matOfPoint = new MatOfPoint();
                matOfPoint.fromArray(vertices);
                Imgproc.fillConvexPoly(image, matOfPoint, pixel, 8, 0);
            };
        }

        image = new Mat(originalImage.size(), fillColor.getImageType());
    }

    @Override
    public void createTriangle(Point[] vertices) {
        style.draw(vertices, fillColor.getFillColor(originalImage, vertices));
    }

    @Override
    protected void writeToFile() {
        Imgcodecs.imwrite(outputPath, image);
    }
}
