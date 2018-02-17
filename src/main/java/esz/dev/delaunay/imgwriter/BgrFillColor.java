package esz.dev.delaunay.imgwriter;

import org.opencv.core.*;

public class BgrFillColor extends AbstractFillColor {

    private static final int CV_TYPE = CvType.CV_8UC3;

    @Override
    public Scalar getFillColor(Mat originalImage, Point[] vertices) {
        double[] color = originalImage.get((int) ((vertices[0].y + vertices[1].y + vertices[2].y) / 3.0),
                (int) ((vertices[0].x + vertices[1].x + vertices[2].x) / 3.0));
        return new Scalar(color[0], color[1], color[2]);
    }

    @Override
    public int getImageType() {
        return CV_TYPE;
    }


}
