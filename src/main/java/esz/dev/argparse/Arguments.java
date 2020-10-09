package esz.dev.argparse;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
public class Arguments {

    private final String input;
    private final String output;

    @Builder.Default
    private final int blurKernelSize = 35;

    @Builder.Default
    private final boolean verbose = false;

    @Builder.Default
    private final int threshold = 150;

    @Builder.Default
    private final EdgeDetectionAlgorithm edgeDetectionAlgorithm = EdgeDetectionAlgorithm.SOBEL;

    @Builder.Default
    private final int sobelKernelSize = 3;

    @Builder.Default
    private final int maxNrOfPoints = 1000;

    @Builder.Default
    private final boolean grayscale = false;

    @Builder.Default
    private final boolean deleteBorder = false;

    @Builder.Default
    private final boolean showEdgePoints = false;

    @Builder.Default
    private final String outputEdgePoints = "";

    @Builder.Default
    private final boolean wireFrame = false;

    @Builder.Default
    private final int thickness = 1;
}
