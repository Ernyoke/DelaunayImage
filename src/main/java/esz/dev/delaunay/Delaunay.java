package esz.dev.delaunay;

import esz.dev.argparse.Arguments;
import esz.dev.delaunay.imgcreator.ImageCreator;
import esz.dev.delaunay.imgcreator.ImgCreatorBuilder;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Delaunay {

    private Arguments arguments;

    private static final Scalar WHITE = new Scalar(255, 255, 255);
    private static final Scalar BLACK = new Scalar(0, 0, 0);

    public Delaunay(Arguments arguments) {
        this.arguments = arguments;
    }

    private Mat createEmptyImage(Size size, int type) {
        return new Mat(size, type, WHITE);
    }

    private Mat createEmptyGrayscaleImage(Size size) {
        return createEmptyImage(size, CvType.CV_8UC1);
    }

    private Mat createSobelImage(Mat grayImage) {
        int depth = CvType.CV_16S;
        int scale = 1;
        int delta = 0;

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
        int depth = CvType.CV_16S;
        int scale = 1;
        int delta = 0;

        Mat gradient = createEmptyGrayscaleImage(grayImage.size());
        Imgproc.Laplacian(grayImage, gradient, depth, arguments.getSobelKernelSize(), scale, delta, Core.BORDER_DEFAULT);
        gradient.convertTo(gradient, CvType.CV_8U);

        return gradient;
    }

    private ArrayList<Point> getEdgePoints(Mat gradientImage, int threshold, int maxNrOfPoints) {
        ArrayList<Point> allEdgePoints = new ArrayList();
        int offset = 127;
        for (int i = 0; i < gradientImage.rows(); ++i) {
            for (int j = 0; j < gradientImage.cols(); ++j) {
                byte[] pixel = new byte[3];
                gradientImage.get(i, j, pixel);
                if (pixel[0] + offset >= threshold) {
                    allEdgePoints.add(new Point(i, j));
                }
            }
        }

        if (allEdgePoints.size() <= maxNrOfPoints) {
            return allEdgePoints;
        } else {
            ArrayList<Point> edgePoints = new ArrayList<>();
            double counter = (double) allEdgePoints.size() / maxNrOfPoints;
            for (double i = 0.0; i < allEdgePoints.size(); i += counter) {
                edgePoints.add(allEdgePoints.get((int) Math.floor(i)));
            }
            return edgePoints;
        }
    }

    private void drawEdgePoints(ArrayList<Point> edgePoints, Size size) {
        if (arguments.isShowEdgePoints()) {
            Mat image = createEmptyGrayscaleImage(size);
            image.setTo(BLACK);
            edgePoints.forEach(point -> {
                image.put((int)point.x, (int)point.y, 255);
            });
            Imgcodecs.imwrite(arguments.getOutputEdgePoints(), image);
        }
    }

    private ArrayList<Triangle> createInitialSuperTriangle(Size size) {
        Point a = new Point(0.0, 0.0);
        Point b = new Point(size.height - 1, 0.0);
        Point c = new Point(size.height - 1, size.width - 1);
        Point d = new Point(0.0, size.width - 1);
        return new ArrayList<>(Arrays.asList(new Triangle(a, b, c), new Triangle(a, c, d)));
    }

    // https://en.wikipedia.org/wiki/Bowyer%E2%80%93Watson_algorithm
    private ArrayList<Triangle> bowyerWatson(ArrayList<Point> edgePoints, Size imageSize) {
        ArrayList<Triangle> triangles = createInitialSuperTriangle(imageSize);
        for (Point point : edgePoints) {
            ArrayList<Triangle> badTriangles = new ArrayList<>();
            ArrayList<Triangle> goodTriangles = new ArrayList<>();
            for (Triangle triangle : triangles) {
                if (triangle.getCircumCircle().isInside(point)) {
                    badTriangles.add(triangle);
                } else {
                    goodTriangles.add(triangle);
                }
            }

            HashSet<Edge> polygon = new HashSet<>();
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
            ArrayList<Triangle> superTriangle = createInitialSuperTriangle(imageSize);
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

    private void createFinalImage(ArrayList<Triangle> triangles, Mat originalImage) throws IOException {
        ImageCreator imageCreator = ImgCreatorBuilder.getWriter(arguments, triangles, originalImage);
        imageCreator.createImageFromTriangles();
    }

    public void generate() throws DelaunayException, IOException {
        if (arguments == null) {
            throw new DelaunayException("No arguments were set!");
        }

        Verbose.printStart(arguments);

        Mat originalImage = Imgcodecs.imread(arguments.getInput(), Imgcodecs.IMREAD_COLOR);
        if (originalImage.empty()) {
            throw new DelaunayException("Input image could not be loaded from location: " + arguments.getInput());
        }
        Verbose.printImageLoaded(arguments);

        // blur the original image
        Mat bluredImage = createEmptyImage(originalImage.size(), originalImage.type());
        Imgproc.GaussianBlur(originalImage, bluredImage, new Size(arguments.getBlurKernelSize(), arguments.getBlurKernelSize()), 0);
        Verbose.printCreatedBlurImage(arguments);

        // create grayscale image from the original
        Mat grayscaleImage = createEmptyGrayscaleImage(bluredImage.size());
        Imgproc.cvtColor(bluredImage, grayscaleImage, Imgproc.COLOR_RGB2GRAY);
        Verbose.printCreatedGrayScaleFromBlur(arguments);

        // detect edges
        Mat detectedEdges = null;
        switch (arguments.getEdgeDetectionAlgorithm()) {
            case SOBEL: {
                detectedEdges = createSobelImage(grayscaleImage);
                break;
            }
            case LAPLACIAN: {
                detectedEdges = createLaplacianImage(grayscaleImage);
                break;
            }
            default: {
                throw new DelaunayException("Invalid edgedetection algorithm!");
            }
        }
        Verbose.printCreatedSobelImage(arguments);

        // get edge points from the image
        ArrayList<Point> edgePoints = getEdgePoints(detectedEdges, arguments.getThreshold(), arguments.getMaxNrOfPoints());
        drawEdgePoints(edgePoints, originalImage.size());
        Verbose.printEdgePointsDetected(arguments);

        // get triangles form edge points
        ArrayList<Triangle> triangles = bowyerWatson(edgePoints, originalImage.size());
        Verbose.printMeshCreated(arguments);

        // create final image
        createFinalImage(triangles, originalImage);
        Verbose.printOutputSaved(arguments);
    }
}
