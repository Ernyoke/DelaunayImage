package esz.dev.delaunay.imgcreator;

import de.erichseifert.vectorgraphics2d.Document;
import de.erichseifert.vectorgraphics2d.Processor;
import de.erichseifert.vectorgraphics2d.VectorGraphics2D;
import de.erichseifert.vectorgraphics2d.eps.EPSProcessor;
import de.erichseifert.vectorgraphics2d.intermediate.CommandSequence;
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
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class VectorImageCreator extends ImageCreator {

    private final Graphics2D vg2d = new VectorGraphics2D();

    private final BiConsumer<int[], int[]> drawFunction;

    VectorImageCreator(List<Triangle> triangles, Mat originalImage, FillColorInterface fillColor,
                       String outputPath, boolean wireFrame) {
        super(triangles, originalImage, fillColor, outputPath, wireFrame);

        if (wireFrame) {
            drawFunction = (int[] xVec, int[] yVec) -> vg2d.fillPolygon(xVec, yVec, 3);
        } else {
            drawFunction = (int[] xVec, int[] yVec) -> {
                vg2d.fillPolygon(xVec, yVec, 3);
                vg2d.drawPolygon(xVec, yVec, 3);
            };
        }
    }

    @Override
    public void createTriangle(Point[] vertices) {
        int[] xVec = {(int) Math.ceil(vertices[0].x), (int) Math.ceil(vertices[1].x), (int) Math.ceil(vertices[2].x)};
        int[] yVec = {(int) Math.ceil(vertices[0].y), (int) Math.ceil(vertices[1].y), (int) Math.ceil(vertices[2].y)};
        Scalar scalar = fillColor.getFillColor(originalImage, vertices);
        vg2d.setColor(scalarToColor(scalar));
        drawFunction.accept(xVec, yVec);
    }

    @Override
    void writeToFile() throws IOException {
        CommandSequence commands = ((VectorGraphics2D) vg2d).getCommands();
        Processor processor = processorBuilder().orElseThrow(() -> new RuntimeException("No processor available!"));
        Size size = originalImage.size();
        Document document = processor.getDocument(commands, new PageSize(size.width, size.height));
        document.writeTo(new FileOutputStream(outputPath));
    }

    private Optional<Processor> processorBuilder() {
        if (outputPath.endsWith(".svg")) {
            return Optional.of(new SVGProcessor());
        }
        if (outputPath.endsWith(".eps")) {
            return Optional.of(new EPSProcessor());
        }
        return Optional.empty();
    }

    private Color scalarToColor(Scalar scalar) {
        double[] colorVec = scalar.val;
        if (colorVec.length == 3) {
            return new Color((int) scalar.val[2], (int) scalar.val[1], (int) scalar.val[0]);
        } else {
            return new Color((int) scalar.val[0], (int) scalar.val[0], (int) scalar.val[0]);
        }
    }
}
