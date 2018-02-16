package esz.dev.delaunay;

import esz.dev.argparse.Arguments;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Delaunay {

    private Arguments arguments;

    public Delaunay(Arguments arguments) {
        this.arguments = arguments;
    }

    private Mat createEmptyImage(Size size, int type) {
        return Mat.zeros(size, type);
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

    private void drawEdgePoints(ArrayList<Point> edgePoints, Size size, boolean draw) {
        if (draw) {
            Mat image = createEmptyGrayscaleImage(size);
            edgePoints.forEach(point -> {
                image.put((int)point.x, (int)point.y, 255);
            });
            Imgcodecs.imwrite("res/edgepoints.jpg", image);
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

        if (arguments.isDeleteSuperTriangle()) {
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

    private Mat createGrayScaleImageFromTriangles(ArrayList<Triangle> triangles, Mat originalImage) {
        Mat image = createEmptyGrayscaleImage(originalImage.size());
        triangles.forEach(triangle -> {
            MatOfPoint matOfPoint = new MatOfPoint();
            Point[] vertices = triangle.getVertices();
            for (int i = 0; i < vertices.length; ++i) {
                vertices[i] = new Point(vertices[i].y, vertices[i].x);
            }
            matOfPoint.fromArray(vertices);
            double[] color = originalImage.get((int) ((vertices[0].y + vertices[1].y + vertices[2].y) / 3.0),
                    (int) ((vertices[0].x + vertices[1].x + vertices[2].x) / 3.0));
            Imgproc.fillConvexPoly(image, matOfPoint, new Scalar(color[0]), 8, 0);
        });
        return image;
    }

    private Mat createColorImageFromTriangles(ArrayList<Triangle> triangles, Mat originalImage) {
        Mat image = createEmptyImage(originalImage.size(), originalImage.type());
        triangles.forEach(triangle -> {
            MatOfPoint matOfPoint = new MatOfPoint();
            Point[] vertices = triangle.getVertices();
            for (int i = 0; i < vertices.length; ++i) {
                vertices[i] = new Point(vertices[i].y, vertices[i].x);
            }
            matOfPoint.fromArray(vertices);
            double[] color = originalImage.get((int) ((vertices[0].y + vertices[1].y + vertices[2].y) / 3.0),
                    (int) ((vertices[0].x + vertices[1].x + vertices[2].x) / 3.0));
            Imgproc.fillConvexPoly(image, matOfPoint, new Scalar(color[0], color[1], color[2]), 8, 0);
        });
        return image;
    }

    // -----------------VERBOSE output methods----------------
    private void printStart() {
        if (arguments.isVerbose()) {
            System.out.println("----- DELAUNAYIMAGE ----");
            System.out.println("Process started in verbose mode");
        }
    }

    private void printImageLoaded() {
        if (arguments.isVerbose()) {
            System.out.println("Image loaded form location: " + arguments.getInput());
        }
    }

    private void printCreatedBlurImage() {
        if (arguments.isVerbose()) {
            System.out.println("Applied blur to original image!");
        }
    }

    private void printCreatedGrayScaleFromBlur() {
        if (arguments.isVerbose()) {
            System.out.println("Grayscale image created from blurred image!");
        }
    }

    private void printCreatedSobelImage() {
        if (arguments.isVerbose()) {
            System.out.println("Sobel filter applied to grayscale image!");
        }
    }

    private void printEdgePointsDetected() {
        if (arguments.isVerbose()) {
            System.out.println("Edge points calculated form sobel image!");
        }
    }

    private void printMeshCreated() {
        if (arguments.isVerbose()) {
            System.out.println("Triangulation finished! Mesh created!");
        }
    }

    private void printOutputSaved() {
        if (arguments.isVerbose()) {
            System.out.println("Output saved: " + arguments.getOutput());
        }
    }
    // -------------------------------------------------------

    public void generate() throws DelaunayException {
        if (arguments == null) {
            throw new DelaunayException("No arguments were set!");
        }

        printStart();

        Mat originalImage = Imgcodecs.imread(arguments.getInput(), Imgcodecs.IMREAD_COLOR);
        if (originalImage.empty()) {
            throw new DelaunayException("Input image could not be loaded from location: " + arguments.getInput());
        }
        printImageLoaded();

        // blur the original image
        Mat bluredImage = createEmptyImage(originalImage.size(), originalImage.type());
        Imgproc.GaussianBlur(originalImage, bluredImage, new Size(arguments.getBlurKernelSize(), arguments.getBlurKernelSize()), 0);
        printCreatedBlurImage();

        // create grayscale image from the original
        Mat grayscaleImage = createEmptyGrayscaleImage(bluredImage.size());
        Imgproc.cvtColor(bluredImage, grayscaleImage, Imgproc.COLOR_RGB2GRAY);
        printCreatedGrayScaleFromBlur();

        // detect edges
        Mat detectedEdges = null;
        switch (arguments.getEdgeDetectionAlgorithm()) {
            case "sobel": {
                detectedEdges = createSobelImage(grayscaleImage);
                break;
            }
            case "laplacian": {
                detectedEdges = createLaplacianImage(grayscaleImage);
                break;
            }
            default: {
                throw new DelaunayException("Invalid edgedetection algorithm!");
            }
        }
        printCreatedSobelImage();

        // get edge points from the image
        ArrayList<Point> edgePoints = getEdgePoints(detectedEdges, arguments.getThreshold(), arguments.getMaxNrOfPoints());
        drawEdgePoints(edgePoints, originalImage.size(), true);
        printEdgePointsDetected();

        // get triangles form edge points
        ArrayList<Triangle> triangles = bowyerWatson(edgePoints, originalImage.size());
        printMeshCreated();

        // create final image
        Mat finalImage;
        if (arguments.isGrayscale()) {
            finalImage = createGrayScaleImageFromTriangles(triangles, originalImage);
        } else {
            finalImage = createColorImageFromTriangles(triangles, originalImage);
        }
        try {
            Imgcodecs.imwrite(arguments.getOutput(), finalImage);
        } catch (Exception e) {
            throw new DelaunayException("Image could not be saved on location: " + arguments.getOutput());
        }
        printOutputSaved();
    }
}
