package esz.dev.delaunay.imgcreator.fillcolor;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

public class GrayFillColor implements FillColorInterface {

    private static final int CV_TYPE = CvType.CV_8UC1;

    @Override
    public Scalar getFillColor(Mat originalImage, Point[] vertices) {
        double[] color = originalImage.get((int) ((vertices[0].y + vertices[1].y + vertices[2].y) / 3.0),
                (int) ((vertices[0].x + vertices[1].x + vertices[2].x) / 3.0));
        return new Scalar(color[0]);
    }

    @Override
    public int getImageType() {
        return CV_TYPE;
    }
}
