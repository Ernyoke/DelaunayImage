package esz.dev.argparse;

import java.util.Arrays;
import java.util.Optional;

public enum DelaunayAlgorithm {
    BW("bw"), DELAUNATOR("delaunator");

    private final String text;

    DelaunayAlgorithm(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

    public static Optional<DelaunayAlgorithm> fromString(String string) {
        return Arrays.stream(DelaunayAlgorithm.values()).filter(value -> value.text.equals(string)).findFirst();
    }
}
