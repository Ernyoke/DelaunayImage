package esz.dev.argparse;

import java.util.Arrays;
import java.util.Optional;

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

    public static Optional<EdgeDetectionAlgorithm> fromString(String string) {
        return Arrays.stream(EdgeDetectionAlgorithm.values()).filter(value -> value.text.equals(string)).findFirst();
    }
}
