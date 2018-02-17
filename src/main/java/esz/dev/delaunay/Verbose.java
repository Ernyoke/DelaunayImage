package esz.dev.delaunay;

import esz.dev.argparse.Arguments;

public class Verbose {

    public static void printStart(Arguments arguments) {
        if (arguments.isVerbose()) {
            System.out.println("----- DELAUNAYIMAGE ----");
            System.out.println("Process started in verbose mode");
        }
    }

    public static void printImageLoaded(Arguments arguments) {
        if (arguments.isVerbose()) {
            System.out.println("Image loaded form location: " + arguments.getInput());
        }
    }

    public static void printCreatedBlurImage(Arguments arguments) {
        if (arguments.isVerbose()) {
            System.out.println("Applied blur to original image!");
        }
    }

    public static void printCreatedGrayScaleFromBlur(Arguments arguments) {
        if (arguments.isVerbose()) {
            System.out.println("Grayscale image created from blurred image!");
        }
    }

    public static void printCreatedSobelImage(Arguments arguments) {
        if (arguments.isVerbose()) {
            System.out.println("Sobel filter applied to grayscale image!");
        }
    }

    public static void printEdgePointsDetected(Arguments arguments) {
        if (arguments.isVerbose()) {
            System.out.println("Edge points calculated form sobel image!");
        }
    }

    public static void printMeshCreated(Arguments arguments) {
        if (arguments.isVerbose()) {
            System.out.println("Triangulation finished! Mesh created!");
        }
    }

    public static void printOutputSaved(Arguments arguments) {
        if (arguments.isVerbose()) {
            System.out.println("Output saved: " + arguments.getOutput());
        }
    }
}
