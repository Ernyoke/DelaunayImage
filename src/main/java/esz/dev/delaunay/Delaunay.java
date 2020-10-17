package esz.dev.delaunay;

import esz.dev.argparse.Arguments;
import esz.dev.argparse.EdgeDetectionAlgorithm;
import esz.dev.delaunay.imgcreator.ImageCreator;
import esz.dev.delaunay.imgcreator.ImgCreatorBuilder;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

public class Delaunay {

    private final Arguments arguments;

    private static final Scalar WHITE = new Scalar(255, 255, 255);
    private static final Scalar BLACK = new Scalar(0, 0, 0);

    public Delaunay(Arguments arguments) {
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
        List<Triangle> triangles = bowyerWatson(edgePoints, originalImage.size());
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

    // https://en.wikipedia.org/wiki/Bowyer%E2%80%93Watson_algorithm
    private List<Triangle> bowyerWatson(List<Point> edgePoints, Size imageSize) {
        List<Triangle> triangles = new ArrayList<>(createInitialSuperTriangle(imageSize));
        for (Point point : edgePoints) {
            final List<Triangle> badTriangles = new ArrayList<>();
            final List<Triangle> goodTriangles = new ArrayList<>();
            for (Triangle triangle : triangles) {
                if (triangle.getCircumCircle().isInside(point)) {
                    badTriangles.add(triangle);
                } else {
                    goodTriangles.add(triangle);
                }
            }

            Set<Edge> polygon = new HashSet<>();
            for (Triangle triangle : badTriangles) {
                for (Edge edge : triangle.getEdges()) {
                    boolean sharedEdge = false;
                    for (Triangle otherTriangle : badTriangles) {
                        if (triangle != otherTriangle && otherTriangle.containsEdge(edge)) {
                            sharedEdge = true;
                            break;
                        }
                    }
                    if (!sharedEdge) {
                        polygon.add(edge);
                    }
                }
            }

            for (Edge edge : polygon) {
                goodTriangles.add(new Triangle(edge.getA(), edge.getB(), point));
            }

            triangles = goodTriangles;
        }

        if (arguments.isDeleteBorder()) {
            List<Triangle> superTriangle = createInitialSuperTriangle(imageSize);
            triangles.removeIf(triangle -> {
                for (Triangle st : superTriangle) {
                    for (Edge edge : triangle.getEdges()) {
                        if (st.containsVertex(edge.getA()) || st.containsVertex(edge.getB())) {
                            return true;
                        }
                    }
                }
                return false;
            });
        }

        return triangles;
    }

    private List<Triangle> createInitialSuperTriangle(Size size) {
        Point a = new Point(0.0, 0.0);
        Point b = new Point(size.height - 1, 0.0);
        Point c = new Point(size.height - 1, size.width - 1);
        Point d = new Point(0.0, size.width - 1);
        return List.of(new Triangle(a, b, c), new Triangle(a, c, d));
    }

    private void createFinalImage(List<Triangle> triangles, Mat originalImage) throws IOException {
        ImageCreator imageCreator = ImgCreatorBuilder.getWriter(arguments, triangles, originalImage);
        imageCreator.createImageFromTriangles();
    }
}
