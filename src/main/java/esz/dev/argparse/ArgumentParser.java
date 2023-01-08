package esz.dev.argparse;

import java.util.*;
import java.util.function.Consumer;

public class ArgumentParser {
    private final Queue<String> argStack = new ArrayDeque<>();
    private final Arguments.ArgumentsBuilder builder = Arguments.builder();
    private final Map<String, Consumer<String>> dispatchToArgumentHandler = new HashMap<>();

    public ArgumentParser(String[] args) {
        for (String arg : args) {
            argStack.offer(arg);
        }

        dispatchToArgumentHandler.put("-bk", this::setBlurKernelSize);
        dispatchToArgumentHandler.put("-v", this::setVerboseFlag);
        dispatchToArgumentHandler.put("-t", this::setThreshold);
        dispatchToArgumentHandler.put("-max", this::setMaxNumberOfPoints);
        dispatchToArgumentHandler.put("-ea", this::setEdgeDetectionAlgorithm);
        dispatchToArgumentHandler.put("-sk", this::setSobelKernelSize);
        dispatchToArgumentHandler.put("-grayscale", this::setGrayscaleImageFlag);
        dispatchToArgumentHandler.put("-vx", this::setOutputVerticesFlag);
        dispatchToArgumentHandler.put("-wire", this::setWireFrameFlag);
        dispatchToArgumentHandler.put("-dbf", this::setDeleteBorderFlag);
        dispatchToArgumentHandler.put("-alg", this::setDelaunayAlgorithm);
    }

    private int parseInt(String argToPParse, String prevArg) {
        if (argToPParse == null) {
            throw new IllegalArgumentException("No argument is present after " + prevArg + "!");
        }
        try {
            return Integer.parseInt(argToPParse);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid argument after " + prevArg + "!");
        }
    }

    public Arguments process() {
        // Mandatory arguments
        if (argStack.size() < 2) {
            throw new IllegalArgumentException("Input and output paths are mandatory arguments!");
        } else {
            builder.input(argStack.poll());
            builder.output(argStack.poll());
        }

        while (!argStack.isEmpty()) {
            String currentArgument = argStack.poll();
            dispatchToArgumentHandler
                    .getOrDefault(currentArgument, unused -> {
                        throw new IllegalArgumentException("Illegal argument found!");
                    })
                    .accept(currentArgument);
        }

        return builder.build();
    }

    private void setBlurKernelSize(String argument) {
        checkNextArgumentsExistence(1, "No argument is present after " + argument + "!");
        int kernelSize = parseInt(argStack.poll(), argument);
        if (kernelSize < 0) {
            throw new IllegalArgumentException("Blur kernel size must pe positive!");
        }
        if (kernelSize % 2 == 0) {
            throw new IllegalArgumentException("Blur kernel size must be and odd number!");
        }
        builder.blurKernelSize(kernelSize);
    }

    private void setVerboseFlag(String argument) {
        builder.verbose(true);
    }

    private void setThreshold(String argument) {
        checkNextArgumentsExistence(1, "No argument is present after " + argument + "!");
        int threshold = parseInt(argStack.poll(), argument);
        if (threshold < 0 || threshold > 255) {
            throw new IllegalArgumentException("Threshold must be between 0 and 255!");
        }
        builder.threshold(threshold);
    }

    private void setMaxNumberOfPoints(String argument) {
        checkNextArgumentsExistence(1, "No argument is present after " + argument + "!");
        int maxNrOfPoints = parseInt(argStack.poll(), argument);
        if (maxNrOfPoints < 0) {
            throw new IllegalArgumentException("Number of max edge points must be a positive number!");
        }
        builder.maxNrOfPoints(maxNrOfPoints);
    }

    private void setEdgeDetectionAlgorithm(String argument) {
        checkNextArgumentsExistence(1, "No argument is present after " + argument + "!");
        builder.edgeDetectionAlgorithm(EdgeDetectionAlgorithm.fromString(argStack.poll()).orElseThrow(() ->
                new IllegalArgumentException("Invalid edge detection algorithm! Allowed values are: sobel, laplacian")));
    }

    private void setSobelKernelSize(String argument) {
        checkNextArgumentsExistence(1, "No argument is present after " + argument + "!");
        final int[] acceptedKernels = {1, 3, 5, 7};
        final int kernelSize = parseInt(argStack.poll(), argument);
        if (Arrays.stream(acceptedKernels).anyMatch(size -> size == kernelSize)) {
            builder.sobelKernelSize(kernelSize);
        } else {
            throw new IllegalArgumentException("Accepted sobel kernel sizes are 1, 3, 5, 7!");
        }
    }

    private void setGrayscaleImageFlag(String argument) {
        builder.grayscale(true);
    }

    private void setOutputVerticesFlag(String argument) {
        checkNextArgumentsExistence(1, "No argument is present after " + argument + "!");
        builder.showVertices(true);
        builder.verticesPath(argStack.poll());
    }

    private void setWireFrameFlag(String argument) {
        builder.wireFrame(true);
    }

    private void setDeleteBorderFlag(String argument) {
        builder.deleteBorder(true);
    }

    private void checkNextArgumentsExistence(int numberOfArguments, String errorMessage) {
        if (argStack.size() < numberOfArguments) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private void setDelaunayAlgorithm(String argument) {
        checkNextArgumentsExistence(1, "No argument is present after " + argument + "!");
        builder.delaunayAlgorithm(DelaunayAlgorithm.fromString(argStack.poll()).orElseThrow(() ->
                new IllegalArgumentException("Invalid edge detection algorithm! Allowed values are: bw, delaunator")));
    }

    public static void showHelp() {
        System.out.print("\n");
        System.out.print("----Usage---\n");
        System.out.print("\n");
        System.out.print("java -jar delaunay.jar <intput path> <output path> [args]\n");
        System.out.print("\n");
        System.out.print("Mandatory (stationary) arguments: <input path> <output path>.\n");
        System.out.print("<input path>: path to the input image.\n");
        System.out.print("Extensions supported for input image: [all the extensions supported by opencv, see: https://docs.opencv.org/3.0-beta/modules/imgcodecs/doc/reading_and_writing_images.html] \n");
        System.out.print("<output path>: path to where the output image should be saved.\n");
        System.out.print("Extensions supported for bitmap output image: [all the extensions supported by opencv, see: https://docs.opencv.org/3.0-beta/modules/imgcodecs/doc/reading_and_writing_images.html] \n");
        System.out.print("Extensions supported for vector graphic output image: .svg, .eps (.pdf is currently no supported!)\n");
        System.out.print("\n");
        System.out.print("Example of usage: java -jar delaunay.jar in.png out.png \n");
        System.out.print("\n");
        System.out.print("-bk <nr>: blur kernel size, <nr> should be a positive odd integer. Default value: 35 \n");
        System.out.print("-t <nr>: threshold value, <nr> should be a positive integer between 0 and 255. Default value: 200\n");
        System.out.print("-max <nr>: maximum number of points, <nr> should be a positive integer. Default value: 1000 \n");
        System.out.print("-ea <alg>: edge detection algorithm, accepted values for <arg> are: sobel, laplacian. Default value: sobel \n");
        System.out.print("-sk <nr>: sobel kernel size, should be a value from the following set: [1, 3, 5, 7]. Default value: 3 \n");
        System.out.print("-grayscale: setting this flag, the output image will be grayscale. Default value: false \n");
        System.out.print("-v: activate console logging. Default value: false \n");
        System.out.print("-wire: draw only wireframe for the triangles. Default value: false \n");
        System.out.print("-dbf: Delete border and all of the triangles which have a node on the border. Default value: false \n");
        System.out.print("-alg: Delaunay algorithm to be used. Accepted values: bw, delaunator. Default value: delaunator \n");
        System.out.print("\n");
        System.out.print("Examples: \n");
        System.out.print("Example of usage: java -jar delaunay.jar in.png out.png -ea laplacian -sk 5 -max 2000 -t 200 -v \n");
        System.out.print("Example of usage: java -jar delaunay.jar in.png out.png -max 2000 -grayscale \n");
        System.out.print("\n");
    }
}
