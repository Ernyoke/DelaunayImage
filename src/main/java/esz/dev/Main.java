package esz.dev;

import esz.dev.argparse.ArgumentParser;
import esz.dev.delaunay.Delaunay;

import java.io.IOException;


public class Main {
    public static void main(String[] args) {

        nu.pattern.OpenCV.loadShared();

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
