package esz.dev;

import esz.dev.argparse.ArgumentParser;
import esz.dev.delaunay.Delaunay;

import java.io.IOException;

public class Main {
    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        ArgumentParser argumentParser = new ArgumentParser(args);
        try {
            argumentParser.process();
            Delaunay delaunay = new Delaunay(argumentParser.process());
            delaunay.generate();
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
            ArgumentParser.showHelp();
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
