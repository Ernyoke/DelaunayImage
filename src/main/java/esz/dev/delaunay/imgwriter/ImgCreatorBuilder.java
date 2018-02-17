package esz.dev.delaunay.imgwriter;

import esz.dev.argparse.Arguments;
import esz.dev.delaunay.Triangle;
import org.opencv.core.Mat;

import java.util.ArrayList;

public class ImgCreatorBuilder {

    public static ImageCreator getWriter(Arguments arguments, ArrayList<Triangle>triangles, Mat originalImage) {
        AbstractFillColor fillColor;
        if (arguments.isGrayscale()) {
            fillColor = new GrayFillColor();
        } else {
            fillColor = new BgrFillColor();
        }

        ImageCreator imageCreator;
        if (arguments.getOutput().endsWith(".svg")) {
            imageCreator = new VectorImageCreator(triangles, originalImage, fillColor, arguments.getOutput(),
                    arguments.isWireFrame());
        } else {
            imageCreator = new PixelImageCreator(triangles, originalImage, fillColor, arguments.getOutput(),
                    arguments.isWireFrame());
        }
        return imageCreator;
    }
}
