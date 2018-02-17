import esz.dev.argparse.ArgParseException;
import esz.dev.argparse.ArgumentParser;
import esz.dev.delaunay.Delaunay;
import esz.dev.delaunay.DelaunayException;

public class Main {

    public static void main(String[] args) {

        nu.pattern.OpenCV.loadShared();

        ArgumentParser argumentParser = new ArgumentParser(args);
        try {
            argumentParser.process();
            Delaunay delayun = new Delaunay(argumentParser.process());
            delayun.generate();
        } catch (ArgParseException e) {
            System.out.println("Error: " + e.getMessage());
            ArgumentParser.showHelp();
        } catch (DelaunayException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
