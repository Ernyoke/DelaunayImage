package esz.dev.imgcreator;

import esz.dev.argparse.Arguments;
import esz.dev.imgcreator.fillcolor.BgrFillColor;
import esz.dev.imgcreator.fillcolor.FillColorInterface;
import esz.dev.imgcreator.fillcolor.GrayFillColor;
import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.List;

public class ImgCreatorBuilder {

    public static ImageCreator getWriter(Arguments arguments, List<List<Point>> triangles, Mat originalImage) {
        FillColorInterface fillColor = arguments.isGrayscale() ? new GrayFillColor() : new BgrFillColor();

        ImageCreator imageCreator;
        if (arguments.getOutput().endsWith(".svg") || arguments.getOutput().endsWith(".eps")) {
            imageCreator = new VectorImageCreator(triangles, originalImage, fillColor, arguments.getOutput(),
                    arguments.isWireFrame());
        } else {
            imageCreator = new PixelImageCreator(triangles, originalImage, fillColor, arguments.getOutput(),
                    arguments.isWireFrame());
        }
        return imageCreator;
    }
}
