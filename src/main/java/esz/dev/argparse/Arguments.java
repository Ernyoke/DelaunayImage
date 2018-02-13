package esz.dev.argparse;

public class Arguments {

    private String input = "";
    private String output = "";
    private int blurKernelSize = 35;
    private boolean verbose = false;
    private int threshold = 30;
    private int sobelKernelSize = 3;
    private int maxNrOfPoints = 1000;
    private boolean grayscale = false;
    private boolean deleteSuperTriangle = false;
    private boolean showEdgePoints = false;
    private String outputEdgePoints = "";

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public int getBlurKernelSize() {
        return blurKernelSize;
    }

    public void setBlurKernelSize(int blurKernelSize) {
        this.blurKernelSize = blurKernelSize;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int treshold) {
        this.threshold = treshold;
    }

    public int getMaxNrOfPoints() {
        return maxNrOfPoints;
    }

    public int getSobelKernelSize() {
        return sobelKernelSize;
    }

    public void setsobelKernelSize(int sobelTreshold) {
        this.sobelKernelSize = sobelTreshold;
    }

    public void setMaxNrOfPoints(int maxNrOfPoints) {
        this.maxNrOfPoints = maxNrOfPoints;
    }

    public boolean isGrayscale() {
        return grayscale;
    }

    public void setGrayscale(boolean grayscale) {
        this.grayscale = grayscale;
    }

    public boolean isDeleteSuperTriangle() {
        return deleteSuperTriangle;
    }

    public void setDeleteSuperTriangle(boolean deleteSuperTriangle) {
        this.deleteSuperTriangle = deleteSuperTriangle;
    }

    public boolean isShowEdgePoints() {
        return showEdgePoints;
    }

    public void setShowEdgePoints(boolean showEdgePoints) {
        this.showEdgePoints = showEdgePoints;
    }

    public String getOutputEdgePoints() {
        return outputEdgePoints;
    }

    public void setOutputEdgePoints(String outputEdgePoints) {
        this.outputEdgePoints = outputEdgePoints;
    }
}
