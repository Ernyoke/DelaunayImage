package esz.dev.delaunay.imgcreator;

import de.erichseifert.vectorgraphics2d.Document;
import de.erichseifert.vectorgraphics2d.Processor;
import de.erichseifert.vectorgraphics2d.VectorGraphics2D;
import de.erichseifert.vectorgraphics2d.eps.EPSProcessor;
import de.erichseifert.vectorgraphics2d.intermediate.CommandSequence;
import de.erichseifert.vectorgraphics2d.pdf.PDFProcessor;
import de.erichseifert.vectorgraphics2d.svg.SVGProcessor;
import de.erichseifert.vectorgraphics2d.util.PageSize;
import esz.dev.delaunay.Triangle;
import esz.dev.delaunay.imgcreator.fillcolor.FillColorInterface;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class VectorImageCreator extends ImageCreator {

    private Graphics2D vg2d = new VectorGraphics2D();

    @FunctionalInterface
    private interface Style {
        void draw(int[] xVec, int[] yVec);
    }

    private Style style;

    VectorImageCreator(ArrayList<Triangle> triangles, Mat originalImage, FillColorInterface fillColor,
                              String outputPath, boolean wireFrame) {
        super(triangles, originalImage, fillColor, outputPath, wireFrame);

        if (wireFrame) {
            style = (int[] xVec, int[] yVec) -> {
                vg2d.fillPolygon(xVec, yVec, 3);
            };
        } else {
            style = (int[] xVec, int[] yVec) -> {
                vg2d.fillPolygon(xVec, yVec, 3);
                vg2d.drawPolygon(xVec, yVec, 3);
            };
        }
    }

    @Override
    public void createTriangle(Point[] vertices) {
        int[] xVec = {(int)Math.ceil(vertices[0].x), (int)Math.ceil(vertices[1].x), (int)Math.ceil(vertices[2].x)};
        int[] yVec = {(int)Math.ceil(vertices[0].y), (int)Math.ceil(vertices[1].y), (int)Math.ceil(vertices[2].y)};
        Scalar scalar = fillColor.getFillColor(originalImage, vertices);
        vg2d.setColor(scalarToColor(scalar));
        style.draw(xVec, yVec);
    }

    @Override
    void writeToFile() {
        CommandSequence commands = ((VectorGraphics2D) vg2d).getCommands();
        Processor processor = processorBuilder();
        Size size = originalImage.size();
        Document document = processor.getDocument(commands, new PageSize(size.width, size.height));
        try {
            document.writeTo(new FileOutputStream(outputPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Processor processorBuilder() {
        if (outputPath.endsWith(".svg")) {
            return new SVGProcessor();
        }
        if (outputPath.endsWith(".eps")) {
            return new EPSProcessor();
        }
        return null;
    }

    private Color scalarToColor(Scalar scalar) {
        double[] colorVec = scalar.val;
        if (colorVec.length == 3) {
            return new Color((int)scalar.val[2], (int)scalar.val[1], (int)scalar.val[0]);
        } else {
            return new Color((int)scalar.val[0], (int)scalar.val[0], (int)scalar.val[0]);
        }
    }
}
