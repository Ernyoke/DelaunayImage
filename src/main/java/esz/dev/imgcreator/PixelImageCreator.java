package esz.dev.imgcreator;

import esz.dev.delaunay.Triangle;
import esz.dev.imgcreator.fillcolor.FillColorInterface;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.List;
import java.util.function.BiConsumer;

public class PixelImageCreator extends ImageCreator {

    private Mat image;

    private static final int THICKNESS = 1;

    private final BiConsumer<List<Point>, Scalar> drawFunction;

    PixelImageCreator(List<List<Point>> triangles, Mat originalImage, FillColorInterface fillColor,
                      String outputPath, boolean wireFrame) {
        super(triangles, originalImage, outputPath, wireFrame, fillColor);

        if (wireFrame) {
            drawFunction = (List<Point> vertices, Scalar pixel) -> {
                Imgproc.line(image, vertices.get(0), vertices.get(1), pixel, THICKNESS);
                Imgproc.line(image, vertices.get(1), vertices.get(2), pixel, THICKNESS);
                Imgproc.line(image, vertices.get(2), vertices.get(0), pixel, THICKNESS);
            };
        } else {
            drawFunction = (List<Point> vertices, Scalar pixel) -> {
                MatOfPoint matOfPoint = new MatOfPoint();
                matOfPoint.fromList(vertices);
                Imgproc.fillConvexPoly(image, matOfPoint, pixel, 8, 0);
            };
        }

        image = new Mat(originalImage.size(), fillColor.getImageType());
    }

    @Override
    public void createTriangle(List<Point> vertices) {
        drawFunction.accept(vertices, fillColor.getFillColor(originalImage, vertices));
    }

    @Override
    void writeToFile() {
        Imgcodecs.imwrite(outputPath, image);
    }
}
