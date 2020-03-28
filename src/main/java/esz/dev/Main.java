package esz.dev;

import esz.dev.argparse.ArgParseException;
import esz.dev.argparse.ArgumentParser;
import esz.dev.delaunay.Delaunay;
import esz.dev.delaunay.DelaunayException;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        nu.pattern.OpenCV.loadShared();

        ArgumentParser argumentParser = new ArgumentParser(args);
        try {
            argumentParser.process();
            Delaunay delaunay = new Delaunay(argumentParser.process());
            delaunay.generate();
        } catch (ArgParseException e) {
            System.out.println("Error: " + e.getMessage());
            ArgumentParser.showHelp();
        } catch (DelaunayException | IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
