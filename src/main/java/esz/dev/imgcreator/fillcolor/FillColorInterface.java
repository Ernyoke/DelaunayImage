package esz.dev.imgcreator.fillcolor;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.util.List;

public interface FillColorInterface {
    Scalar getFillColor(Mat originalImage, List<Point> vertices);

    int getImageType();
}
