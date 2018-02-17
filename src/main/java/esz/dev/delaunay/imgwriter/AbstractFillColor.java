package esz.dev.delaunay.imgwriter;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

public abstract  class AbstractFillColor {
    public abstract Scalar getFillColor(Mat originalImage, Point[] vertices);
    public abstract int getImageType();
}
