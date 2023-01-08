# DelaunayImage

## Description

Generate artistic bitmap and/or vector graphic images using [Delaunay](https://en.wikipedia.org/wiki/Delaunay_triangulation) triangulation algorithm.

![alt text](res/img3_out.jpg "Big")

### Procedure:

1. Blur the original image using *Gaussian blur* algorithm.
2. Create a grayscale image from the blurred image.
3. Detect edges  using sobel or laplacian algorithm.
4. Apply a threshold on the image containing the edges of the original image. This serves as a noise reduction procedure.
5. Get the remaining points and create a *Delaunay* mesh using the [Bowyer-Watson](https://en.wikipedia.org/wiki/Bowyer%E2%80%93Watson_algorithm) algorithm.
6. Reconstruct the image using the *Delaunay* mesh.
7. Save the final image.

## Build

**Java 17** is necessary to be able to build the project!

```
git clone https://github.com/Ernyoke/DelaunayImage.git
mvn clean package
```

### Dependencies

* OpenCV
* JUnit5
* [VectorGraphics2D](https://github.com/eseifert/vectorgraphics2d)

## Example of usage

```
----Usage---

java -jar delaunay.jar <intput path> <output path> [args]

Mandatory (stationary) arguments: <input path> <output path>.
<input path>: path to the input image.
Extensions supported for input image: [all the extensions supported by opencv, see: https://docs.opencv.org/3.0-beta/modules/imgcodecs/doc/reading_and_writing_images.html]
<output path>: path to where the output image should be saved.
Extensions supported for bitmap output image: [all the extensions supported by opencv, see: https://docs.opencv.org/3.0-beta/modules/imgcodecs/doc/reading_and_writing_images.html]
Extensions supported for vector graphic output image: .svg, .eps (.pdf is currently no supported!)

Example of usage: java -jar delaunay.jar in.png out.png

-bk <nr>: blur kernel size, <nr> should be a positive odd integer. Default value: 35
-t <nr>: threshold value, <nr> should be a positive integer between 0 and 255. Default value: 200
-max <nr>: maximum number of points, <nr> should be a positive integer. Default value: 1000
-ea <alg>: edge detection algorithm, accepted values for <arg> are: sobel, laplacian. Default value: sobel
-sk <nr>: sobel kernel size, should be a value from the following set: [1, 3, 5, 7]. Default value: 3
-grayscale: setting this flag, the output image will be grayscale. Default value: false
-v: activate console logging. Default value: false
-wire: draw only wireframe for the triangles. Default value: false
-dbf: Delete border and all of the triangles which have a node on the border. Default value: false
-alg: Delaunay algorithm to be used. Accepted values: bw, delaunator. Default value: delaunator

Examples:
Example of usage: java -jar delaunay.jar in.png out.png -ea laplacian -sk 5 -max 2000 -t 200 -v
Example of usage: java -jar delaunay.jar in.png out.png -max 2000 -grayscale
```

## Output examples

| Original                                                                         | java -jar delaunay.jar in.jpg out.jpg -max 2000 -t 150 -grayscale |
|----------------------------------------------------------------------------------|-------------------------------------------------------------------|
| ![alt text](res/img1.jpg "Original")                                             | ![alt text](res/img_out_gray.jpg "gray")                          |
| java -jar delaunay.jar in.jpg out_color.jpg -ea laplacian -sk 5 -max 2000 -t 200 | java -jar delaunay.jar in.jpg out_color.jpg -max 1000 -t 150      |
| ![alt text](res/img_lap_out_color.jpg "laplacian")                               | ![alt text](res/img_out_color.jpg "sobel")                        |
| java -jar delaunay.jar in.jpg wire.jpg -wire                                     | java -jar delaunay.jar in.jpg wire.jpg -wire -dbf                 |
| ![alt text](res/wire.jpg "wireframe")                                            | ![alt text](res/wire_dbf.jpg "wireframe_dbf")                     |

## Tips and tricks

* Laplacian algorithm is very sensitive to noise. Try to smooth out noise by using the right kernel size and sigma values.
* The number of the triangles is determined by the number of the *-max* points. This will also influence the average area size of the triangles.
Smaller numbers of triangles will result in bigger ares and probably picture information loss.
* -v Verbose mode logs out every step of the procedure.

## License

This project is under the MIT License. See the LICENSE file for the full license text.
