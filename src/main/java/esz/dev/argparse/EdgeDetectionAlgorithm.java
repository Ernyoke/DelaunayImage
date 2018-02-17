package esz.dev.argparse;

public enum EdgeDetectionAlgorithm {
    SOBEL("sobel"), LAPLACIAN("laplacian");

    private final String text;

    EdgeDetectionAlgorithm(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
