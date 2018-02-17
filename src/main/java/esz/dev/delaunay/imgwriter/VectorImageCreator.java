package esz.dev.delaunay.imgwriter;

import de.erichseifert.vectorgraphics2d.Document;
import de.erichseifert.vectorgraphics2d.VectorGraphics2D;
import de.erichseifert.vectorgraphics2d.intermediate.CommandSequence;
import de.erichseifert.vectorgraphics2d.svg.SVGProcessor;
import de.erichseifert.vectorgraphics2d.util.PageSize;
import esz.dev.delaunay.Triangle;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;

import java.awt.*;
import java.awt.geom.Line2D;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class VectorImageCreator extends ImageCreator {

    private Graphics2D vg2d = new VectorGraphics2D();

    public VectorImageCreator(ArrayList<Triangle> triangles, Mat originalImage, AbstractFillColor fillColor,
                              String outputPath, boolean wireFrame) {
        super(triangles, originalImage, fillColor, outputPath, wireFrame);

    }

    @Override
    public void createTriangle(Point[] vertices) {
        vg2d.draw(new Line2D.Double(vertices[0].x, vertices[0].y, vertices[1].x, vertices[1].y));
        vg2d.draw(new Line2D.Double(vertices[1].x, vertices[1].y, vertices[2].x, vertices[2].y));
        vg2d.draw(new Line2D.Double(vertices[2].x, vertices[2].y, vertices[0].x, vertices[0].y));
    }

    @Override
    protected void writeToFile() {
        CommandSequence commands = ((VectorGraphics2D) vg2d).getCommands();
        SVGProcessor processor = new SVGProcessor();
        Size size = originalImage.size();
        Document document = processor.getDocument(commands, new PageSize(size.width, size.height));
        try {
            document.writeTo(new FileOutputStream(outputPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
