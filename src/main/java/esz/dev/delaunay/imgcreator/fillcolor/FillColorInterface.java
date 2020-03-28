package esz.dev.delaunay.imgcreator.fillcolor;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

public interface FillColorInterface {
    Scalar getFillColor(Mat originalImage, Point[] vertices);

    int getImageType();
}
