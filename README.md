# DelaunayImage

## Description
Generate artistic images using [Delaunay](https://en.wikipedia.org/wiki/Delaunay_triangulation) triangulation algorithm.

![alt text](res/img3_out.jpg "Logo Title Text 1")

### Procedure steps:
1. Blur the original image using *Gaussian blur* procedure.
2. Create a grayscale image from the blurred image.
3. Detect edges  using sobel or laplacian algorithm.
4. Apply a threshold on the image containing the edges of the original image. This serves as a noise reduction procedure.
5. Get the remaining points and create a *Delaunay* mesh using the [Bowyer-Watson](https://en.wikipedia.org/wiki/Bowyer%E2%80%93Watson_algorithm) algorithm.
6. Reconstruct the image using the *Delaunay* mesh.

## Build
```
git clone [repo]
mvn clean install
```

### Dependecies
* OpenCV
* JUnit5

## Example of usage

```
----Usage---

java -jar delaunay.jar <intput path> <output path> [args]

Mandatory (stationary) arguments: <input path> <output path>.
<input path>: path to the input image. Extensions supported: .jpg, .jpeg, .png
<input path>: path to where the output image should be saved. Extensions supported for the output image: .jpg, .jpeg, .png

Example of usage: java -jar delaunay.jar in.png out.png 

-bk <nr>: blur kernel size, <nr> should be a positive odd integer. Default value: 35 
-t <nr>: threshold value, <nr> should be a positive integer between 0 and 255. Default value: 200
-max <nr>: maximum number of points, <nr> should be a positive integer. Default value: 1000 
-ea <alg>: edge detection algorithm, accepted values for <arg> are: sobel, laplacian. Default value: sobel 
-sk <nr>: sobel kernel size, should be a value from the following set: [1, 3, 5, 7]. Default value: 3 
-grayscale: setting this flag, the output image will be grayscale. Default value: false 
-verbose: activate console logging. Default value: false 

Examples: 
Example of usage: java -jar delaunay.jar in.png out.png -ea laplacian -sk 5 -max 2000 -t 200 -v 
Example of usage: java -jar delaunay.jar in.png out.png -max 2000 -grayscale 
```

## Output examples

## Tips and tricks
* Laplacian algorithm is very sensitive to noise. Try to smooth out noise by using the right kernel size and sigma values.
* The number of the triangles is determined by the number of the *-max* points. This will also influence the average area size of the triangles.
Smaller numbers of triangles will result in bigger ares and probably picture information loss.
* -v Verbose mode logs out every step of the procedure.

## License
