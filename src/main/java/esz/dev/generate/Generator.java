package esz.dev.generate;

import esz.dev.argparse.Arguments;
import esz.dev.argparse.EdgeDetectionAlgorithm;
import esz.dev.delaunay.DelaunayRunner;
import esz.dev.imgcreator.ImageCreator;
import esz.dev.imgcreator.ImgCreatorBuilder;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Generator {

    private final Arguments arguments;

    private static final Scalar WHITE = new Scalar(255, 255, 255);
    private static final Scalar BLACK = new Scalar(0, 0, 0);

    public Generator(Arguments arguments) {
        this.arguments = arguments;
    }

    public void generate() throws IOException {
        if (arguments == null) {
            throw new IllegalArgumentException("No arguments were set!");
        }

        Verbose.printStart(arguments);

        Mat originalImage = Imgcodecs.imread(arguments.getInput(), Imgcodecs.IMREAD_COLOR);
        if (originalImage.empty()) {
            throw new IOException("Input image could not be loaded from location: " + arguments.getInput());
        }
        Verbose.printImageLoaded(arguments);

        // blur the original image
        Mat blurredImage = createEmptyImage(originalImage.size(), originalImage.type());
        Imgproc.GaussianBlur(originalImage, blurredImage, new Size(arguments.getBlurKernelSize(), arguments.getBlurKernelSize()), 0);
        Verbose.printCreatedBlurImage(arguments);

        // create grayscale image from the original
        Mat grayscaleImage = createEmptyGrayscaleImage(blurredImage.size());
        Imgproc.cvtColor(blurredImage, grayscaleImage, Imgproc.COLOR_RGB2GRAY);
        Verbose.printCreatedGrayScaleFromBlur(arguments);

        // detect edges
        Map<EdgeDetectionAlgorithm, Function<Mat, Mat>> enumToFunction = new HashMap<>();
        enumToFunction.put(EdgeDetectionAlgorithm.SOBEL, this::createSobelImage);
        enumToFunction.put(EdgeDetectionAlgorithm.LAPLACIAN, this::createLaplacianImage);
        Mat detectedEdges = enumToFunction.get(arguments.getEdgeDetectionAlgorithm()).apply(grayscaleImage);
        Verbose.printCreatedSobelImage(arguments);

        // get edge points from the image
        List<Point> edgePoints = getVertices(detectedEdges, arguments.getThreshold(), arguments.getMaxNrOfPoints());
        drawVertices(edgePoints, originalImage.size());
        Verbose.printEdgePointsDetected(arguments);

        // get triangles form edge points
        var alg = arguments.getDelaunayAlgorithm();
        DelaunayRunner delaunayRunner = DelaunayRunner.builder(alg);
        List<List<Point>> triangles = delaunayRunner.run(edgePoints, originalImage.size(), arguments.isDeleteBorder());
        Verbose.printMeshCreated(arguments);

        // create final image
        createFinalImage(triangles, originalImage);
        Verbose.printOutputSaved(arguments);
    }

    private Mat createEmptyImage(Size size, int type) {
        return new Mat(size, type, WHITE);
    }

    private Mat createSobelImage(Mat grayImage) {
        final int depth = CvType.CV_16S;
        final int scale = 1;
        final int delta = 0;

        Mat gradientX = createEmptyGrayscaleImage(grayImage.size());
        Imgproc.Sobel(grayImage, gradientX, depth, 1, 0, arguments.getSobelKernelSize(), scale, delta, Core.BORDER_DEFAULT);

        Mat gradientY = createEmptyGrayscaleImage(grayImage.size());
        Imgproc.Sobel(grayImage, gradientY, depth, 0, 1, arguments.getSobelKernelSize(), scale, delta, Core.BORDER_DEFAULT);

        Mat absGradientX = createEmptyGrayscaleImage(gradientX.size());
        Core.convertScaleAbs(gradientX, absGradientX);

        Mat absGradientY = createEmptyGrayscaleImage(gradientY.size());
        Core.convertScaleAbs(gradientY, absGradientY);

        Mat gradient = createEmptyGrayscaleImage(grayImage.size());
        Core.addWeighted(absGradientX, 0.5, absGradientY, 0.5, 0, gradient);

        return gradient;
    }

    private Mat createLaplacianImage(Mat grayImage) {
        final int depth = CvType.CV_16S;
        final int scale = 1;
        final int delta = 0;

        Mat gradient = createEmptyGrayscaleImage(grayImage.size());
        Imgproc.Laplacian(grayImage, gradient, depth, arguments.getSobelKernelSize(), scale, delta, Core.BORDER_DEFAULT);
        gradient.convertTo(gradient, CvType.CV_8U);

        return gradient;
    }

    private Mat createEmptyGrayscaleImage(Size size) {
        return createEmptyImage(size, CvType.CV_8UC1);
    }

    private List<Point> getVertices(Mat gradientImage, int threshold, int maxNrOfPoints) {
        final int offset = 127;
        List<Point> vertices = new ArrayList<>();
        for (int i = 0; i < gradientImage.rows(); ++i) {
            for (int j = 0; j < gradientImage.cols(); ++j) {
                byte[] pixel = new byte[3];
                gradientImage.get(i, j, pixel);
                if (pixel[0] + offset >= threshold) {
                    vertices.add(new Point(i, j));
                }
            }
        }

        if (vertices.size() <= maxNrOfPoints) {
            return vertices;
        } else {
            List<Point> edgePoints = new ArrayList<>();
            double counter = (double) vertices.size() / maxNrOfPoints;
            for (double i = 0.0; i < vertices.size(); i += counter) {
                edgePoints.add(vertices.get((int) Math.floor(i)));
            }
            return edgePoints;
        }
    }

    private void drawVertices(List<Point> edgePoints, Size size) {
        if (arguments.isShowVertices()) {
            Mat image = createEmptyGrayscaleImage(size);
            image.setTo(BLACK);
            edgePoints.forEach(point -> image.put((int) point.x, (int) point.y, 255));
            Imgcodecs.imwrite(arguments.getVerticesPath(), image);
        }
    }

    private void createFinalImage(List<List<Point>> triangles, Mat originalImage) throws IOException {
        ImageCreator imageCreator = ImgCreatorBuilder.getWriter(arguments, triangles, originalImage);
        imageCreator.createImageFromTriangles();
    }
}

