package esz.dev.imgcreator.fillcolor;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.util.List;

public class GrayFillColor implements FillColorInterface {

    private static final int CV_TYPE = CvType.CV_8UC1;

    @Override
    public Scalar getFillColor(Mat originalImage, List<Point> vertices) {
        double[] color = originalImage.get((int) ((vertices.get(0).y + vertices.get(1).y + vertices.get(2).y) / 3.0),
                (int) ((vertices.get(0).x + vertices.get(1).x + vertices.get(2).x) / 3.0));
        return new Scalar(color[0]);
    }

    @Override
    public int getImageType() {
        return CV_TYPE;
    }
}
