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
                        arguments.setThreshold(parseInt(args[i], arg));
                    } else {
                        throw new ArgParseException("No argument is present after -t!");
                    }
                    break;
                }
                case "-max": {
                    ++i;
                    if (i < args.length) {
                        arguments.setMaxNrOfPoints(parseInt(args[i], arg));
                    } else {
                        throw new ArgParseException("No argument is present after -max!");
                    }
                    break;
                }
                case "-sk": {
                    ++i;
                    if (i < args.length) {
                        final int[] acceptedKernels = {1, 3, 5, 7};
                        int kernelSize = parseInt(args[i], arg);
                        if (Arrays.stream(acceptedKernels).anyMatch(size -> size == kernelSize)) {
                            arguments.setsobelKernelSize(kernelSize);
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
            }
        }

        return arguments;
    }

    public static void showHelp() {
        System.out.println("Help!");
    }
}
