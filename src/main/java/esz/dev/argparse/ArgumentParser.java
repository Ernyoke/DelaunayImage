package esz.dev.argparse;

import java.util.Arrays;

public class ArgumentParser {

    private String[] args;

    public ArgumentParser(String[] args) {
        this.args = args;
    }

    private int parseInt(String argToPParse, String prevArg) throws ArgParseException {
        if (argToPParse == null) {
            throw new ArgParseException("No argument is present after " + prevArg + "!");
        }
        try {
            return Integer.parseInt(argToPParse);
        } catch (NumberFormatException  e) {
            throw new ArgParseException("Invalid argument after " + prevArg + "!");
        }
    }

    public Arguments process() throws ArgParseException {
        Arguments arguments = new Arguments();

        // Mandatory arguments
        if (args.length < 2) {
            throw new ArgParseException("Input and output paths are mandatory arguments!");
        } else {
            arguments.setInput(args[0]);
            arguments.setOutput(args[1]);
        }

        for (int i = 0; i < args.length; ++i) {
            String arg = args[i];
            switch (arg) {
                case "-bk": {
                    ++i;
                    if (i < args.length) {
                        int kernelSize = parseInt(args[i], arg);
                        if (kernelSize < 0) {
                            throw new ArgParseException("Blur kernel size must pe positive!");
                        }
                        if (kernelSize % 2 == 0) {
                            throw new ArgParseException("Blur kernel size must be and odd number!");
                        }
                        arguments.setBlurKernelSize(kernelSize);
                    } else {
                        throw new ArgParseException("No argument is present after -bk!");
                    }
                    break;
                }
                case "-v": {
                    arguments.setVerbose(true);
                    break;
                }
                case "-t": {
                    ++i;
                    if (i < args.length) {
                        int threshold = parseInt(args[i], arg);
                        if (threshold < 0 || threshold > 255) {
                            throw new ArgParseException("Threshold must be between 0 and 255!");
                        }
                        arguments.setThreshold(threshold);
                    } else {
                        throw new ArgParseException("No argument is present after -t!");
                    }
                    break;
                }
                case "-max": {
                    ++i;
                    if (i < args.length) {
                        int maxNrOfPoints = parseInt(args[i], arg);
                        if (maxNrOfPoints < 0) {
                            throw new ArgParseException("Number of max edge points must be a positive number!");
                        }
                        arguments.setMaxNrOfPoints(maxNrOfPoints);
                    } else {
                        throw new ArgParseException("No argument is present after -max!");
                    }
                    break;
                }
                case "-ea": {
                    ++i;
                    if (i < args.length) {
                        String algorithm = args[i];
                        final String[] algorithms = {"sobel", "laplacian"};
                        if (Arrays.stream(algorithms).anyMatch(alg -> algorithm.equals(alg))) {
                            arguments.setEdgeDetectionAlgorithm(args[i]);
                        } else {
                            throw new ArgParseException("Invalid edge detection algorithm! Allowed values are: sobel, laplacian");
                        }
                    } else {
                        throw new ArgParseException("No argument is present after -ea!");
                    }
                    break;
                }
                case "-sk": {
                    ++i;
                    if (i < args.length) {
                        final int[] acceptedKernels = {1, 3, 5, 7};
                        int kernelSize = parseInt(args[i], arg);
                        if (Arrays.stream(acceptedKernels).anyMatch(size -> size == kernelSize)) {
                            arguments.setSobelKernelSize(kernelSize);
                        } else {
                            throw new ArgParseException("Accepted sobel kernel sizes are 1, 3, 5, 7!");
                        }
                    } else {
                        throw new ArgParseException("No argument is present after -sk!");
                    }
                    break;
                }
                case "-grayscale": {
                    arguments.setGrayscale(true);
                    break;
                }
                case "-ep" : {
                    ++i;
                    if (i < args.length) {
                        arguments.setShowEdgePoints(true);
                        arguments.setOutputEdgePoints(args[i]);
                    } else {
                        throw new ArgParseException("No argument is present after -ep!");
                    }
                }
                case "-wire": {
                    arguments.setWireFrame(true);
                    break;
                }
                case "-dbf": {
                    arguments.setDeleteBorder(true);
                }
            }
        }

        return arguments;
    }

    public static void showHelp() {
        System.out.print("\n");
        System.out.print("----Usage---\n");
        System.out.print("\n");
        System.out.print("java -jar delaunay.jar <intput path> <output path> [args]\n");
        System.out.print("\n");
        System.out.print("Mandatory (stationary) arguments: <input path> <output path>.\n");
        System.out.print("<input path>: path to the input image. Extensions supported: .jpg, .jpeg, .png\n");
        System.out.print("<input path>: path to where the output image should be saved. Extensions supported for the output image: .jpg, .jpeg, .png\n");
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
        System.out.print("\n");
        System.out.print("Examples: \n");
        System.out.print("Example of usage: java -jar delaunay.jar in.png out.png -ea laplacian -sk 5 -max 2000 -t 200 -v \n");
        System.out.print("Example of usage: java -jar delaunay.jar in.png out.png -max 2000 -grayscale \n");
        System.out.print("\n");
    }
}
