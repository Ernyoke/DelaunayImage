package argparse;

import esz.dev.argparse.ArgumentParser;
import esz.dev.argparse.Arguments;
import esz.dev.argparse.EdgeDetectionAlgorithm;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArgumentParserTest {

    @Test
    public void validMandatoryArgsTest() {
        String[] args = {"in.png", "out.png"};
        ArgumentParser argumentParser = new ArgumentParser(args);
        try {
            Arguments arguments = argumentParser.process();
            Assertions.assertEquals(arguments.getInput(), args[0]);
            Assertions.assertEquals(arguments.getOutput(), args[1]);
        } catch (IllegalArgumentException e) {
            Assertions.fail("ArgumentParser should not throw exception!");
        }
    }

    @Test
    public void mandatoryArgsMissing() {
        String[] args = {};
        ArgumentParser argumentParser = new ArgumentParser(args);
        Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, argumentParser::process);
        Assertions.assertEquals(exception.getMessage(), "Input and output paths are mandatory arguments!");
    }

    @Test
    public void secondMandatoryArgMissing() {
        String[] args = {"in.png"};
        ArgumentParser argumentParser = new ArgumentParser(args);
        Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, argumentParser::process);
        Assertions.assertEquals(exception.getMessage(), "Input and output paths are mandatory arguments!");
    }

    @Test
    public void validThreshold() {
        String[] args = {"in.png", "out.png", "-t", "100"};
        ArgumentParser argumentParser = new ArgumentParser(args);
        try {
            Arguments arguments = argumentParser.process();
            Assertions.assertNotNull(arguments);
            Assertions.assertEquals(arguments.getThreshold(), 100);
        } catch (IllegalArgumentException e) {
            Assertions.fail("ArgumentParser should not throw exception!");
        }
    }

    @Test
    public void invalidThreshold() {
        String[] args = {"in.png", "out.png", "-t", "300"};
        ArgumentParser argumentParser = new ArgumentParser(args);
        Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, argumentParser::process);
        Assertions.assertEquals(exception.getMessage(), "Threshold must be between 0 and 255!");
    }

    @Test
    public void thresholdMissing() {
        String[] args = {"in.png", "out.png", "-t"};
        ArgumentParser argumentParser = new ArgumentParser(args);
        Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, argumentParser::process);
        Assertions.assertEquals(exception.getMessage(), "No argument is present after -t!");
    }

    @Test
    public void validBlurKernelSize() {
        String[] args = {"in.png", "out.png", "-bk", "35"};
        ArgumentParser argumentParser = new ArgumentParser(args);
        try {
            Arguments arguments = argumentParser.process();
            Assertions.assertNotNull(arguments);
            Assertions.assertEquals(arguments.getBlurKernelSize(), 35);
        } catch (IllegalArgumentException e) {
            Assertions.fail("ArgumentParser should not throw exception!");
        }
    }

    @Test
    public void invalidBlurKernelSize() {
        String[] args = {"in.png", "out.png", "-bk", "40"};
        ArgumentParser argumentParser = new ArgumentParser(args);
        Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, argumentParser::process);
        Assertions.assertEquals(exception.getMessage(), "Blur kernel size must be and odd number!");
    }

    @Test
    public void negativedBlurKernelSize() {
        String[] args = {"in.png", "out.png", "-bk", "-40"};
        ArgumentParser argumentParser = new ArgumentParser(args);
        Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, argumentParser::process);
        Assertions.assertEquals(exception.getMessage(), "Blur kernel size must pe positive!");
    }

    @Test
    public void blurKernelSizeValueMissing() {
        String[] args = {"in.png", "out.png", "-bk"};
        ArgumentParser argumentParser = new ArgumentParser(args);
        Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, argumentParser::process);
        Assertions.assertEquals(exception.getMessage(), "No argument is present after -bk!");
    }

    @Test
    public void validEdgeDetectionAlgorithm() {
        String[] args= {"in.png", "out.png", "-ea", "laplacian"};
        ArgumentParser argumentParser = new ArgumentParser(args);
        try {
            Arguments arguments = argumentParser.process();
            Assertions.assertNotNull(arguments);
            Assertions.assertEquals(arguments.getEdgeDetectionAlgorithm(), EdgeDetectionAlgorithm.LAPLACIAN);
        } catch (IllegalArgumentException e) {
            Assertions.fail("ArgumentParser should not throw exception!");
        }
    }

    @Test
    public void invalidEdgeDetectionAlgorithm() {
        String[] args= {"in.png", "out.png", "-ea", "asd"};
        ArgumentParser argumentParser = new ArgumentParser(args);
        Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, argumentParser::process);
        Assertions.assertEquals(exception.getMessage(), "Invalid edge detection algorithm! Allowed values are: sobel, laplacian");
    }

    @Test
    public void edgeDetectionAlgorithmMissing() {
        String[] args= {"in.png", "out.png", "-ea"};
        ArgumentParser argumentParser = new ArgumentParser(args);
        Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, argumentParser::process);
        Assertions.assertEquals(exception.getMessage(), "No argument is present after -ea!");
    }

    @Test
    public void validSobelKernelSize() {
        String[] args = {"in.png", "out.png", "-sk", "3"};
        ArgumentParser argumentParser = new ArgumentParser(args);
        try {
            Arguments arguments = argumentParser.process();
            Assertions.assertNotNull(arguments);
            Assertions.assertEquals(arguments.getSobelKernelSize(), 3);
        } catch (IllegalArgumentException e) {
            Assertions.fail("ArgumentParser should not throw exception!");
        }
    }

    @Test
    public void invalidSobelKernelSize() {
        String[] args = {"in.png", "out.png", "-sk", "15"};
        ArgumentParser argumentParser = new ArgumentParser(args);
        Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, argumentParser::process);
        Assertions.assertEquals(exception.getMessage(), "Accepted sobel kernel sizes are 1, 3, 5, 7!");
    }

    @Test
    public void sobelKernelSizeValueMissing() {
        String[] args = {"in.png", "out.png", "-sk"};
        ArgumentParser argumentParser = new ArgumentParser(args);
        Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, argumentParser::process);
        Assertions.assertEquals(exception.getMessage(), "No argument is present after -sk!");
    }

    @Test
    public void validEdgePointArguments() {
        String[] args = {"in.png", "out.png", "-ep", "res/edge.png"};
        ArgumentParser argumentParser = new ArgumentParser(args);
        try {
            Arguments arguments = argumentParser.process();
            Assertions.assertNotNull(arguments);
            Assertions.assertTrue(arguments.isShowEdgePoints());
            Assertions.assertEquals(arguments.getOutputEdgePoints(), "res/edge.png");
        } catch (IllegalArgumentException e) {
            Assertions.fail("ArgumentParser should not throw exception!");
        }
    }

    @Test
    public void edgePointsPathMissing() {
        String[] args = {"in.png", "out.png", "-ep"};
        ArgumentParser argumentParser = new ArgumentParser(args);
        Throwable exception = Assertions.assertThrows(IllegalArgumentException.class, argumentParser::process);
        Assertions.assertEquals(exception.getMessage(), "No argument is present after -ep!");
    }

    @Test
    public void verboseSet() {
        String[] args = {"in.png", "out.png", "-v"};
        ArgumentParser argumentParser = new ArgumentParser(args);
        try {
            Arguments arguments = argumentParser.process();
            Assertions.assertNotNull(arguments);
            Assertions.assertTrue(arguments.isVerbose());
        } catch (IllegalArgumentException e) {
            Assertions.fail("ArgumentParser should not throw exception!");
        }
    }

    @Test
    public void grayScaleSet() {
        String[] args = {"in.png", "out.png", "-grayscale"};
        ArgumentParser argumentParser = new ArgumentParser(args);
        try {
            Arguments arguments = argumentParser.process();
            Assertions.assertNotNull(arguments);
            Assertions.assertTrue(arguments.isGrayscale());
        } catch (IllegalArgumentException e) {
            Assertions.fail("ArgumentParser should not throw exception!");
        }
    }

    @Test
    public void wireFrameSet() {
        String[] args = {"in.png", "out.png", "-wire"};
        ArgumentParser argumentParser = new ArgumentParser(args);
        try {
            Arguments arguments = argumentParser.process();
            Assertions.assertNotNull(arguments);
            Assertions.assertTrue(arguments.isWireFrame());
        } catch (IllegalArgumentException e) {
            Assertions.fail("ArgumentParser should not throw exception!");
        }
    }

    @Test
    public void deleteBorderSet() {
        String[] args = {"in.png", "out.png", "-dbf"};
        ArgumentParser argumentParser = new ArgumentParser(args);
        try {
            Arguments arguments = argumentParser.process();
            Assertions.assertNotNull(arguments);
            Assertions.assertTrue(arguments.isDeleteBorder());
        } catch (IllegalArgumentException e) {
            Assertions.fail("ArgumentParser should not throw exception!");
        }
    }
}
