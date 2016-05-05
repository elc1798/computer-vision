# Presentation notes:

## Blurring a matrix:
```
Mat blurred = new Mat();
Imgproc.medianBlur(frame, blurred, 5);
```

## Displaying an image:
```
postImage(blurred, "Blurred");
```

## Splitting HSV Channels:
```
Mat hsv = new Mat();
Imgproc.cvtColor(blurred, hsv, Imgproc.COLOR_BGR2HSV);
ArrayList<Mat> channels = new ArrayList<Mat>();
Core.split(hsv, channels);

postImage(channels.get(0), "Unthresholded Hue");
postImage(channels.get(1), "Unthresholded Saturation");
postImage(channels.get(2), "Unthresholded Value");
```

Note that when we post these images:
- Hue will make darker things more white, while brighter things are more black
- Saturation will make things closer to white dark, while things with color, approaching black appear grey - white
- Value appears as a greyscale version of our image, which makes sense: Value is the brightness setting.

This allows us to do some cool things:

- Because saturation makes bright things dark, if we invert the image, we will have a mask of brightness
- Because value is greyscale, bright things are still white
- This allows us to detect bright white objects, like lights
- We can let the 'Hue' range be lower, let the 'Saturation' range be extremely low, and let the 'Value' range be extremely high

## Creating sliders using Java Vision Gui
```
public IntegerSliderVariable minHue = new IntegerSliderVariable("Min Hue", 0, 0, 255);
public IntegerSliderVariable maxHue = new IntegerSliderVariable("Max Hue", 0, 0, 255);
public IntegerSliderVariable minSat = new IntegerSliderVariable("Min Saturation", 0, 0, 255);
public IntegerSliderVariable maxSat = new IntegerSliderVariable("Max Saturation", 0, 0, 255);
public IntegerSliderVariable minVal = new IntegerSliderVariable("Min Value", 254, 0, 255);
public IntegerSliderVariable maxVal = new IntegerSliderVariable("Max Value", 255, 0, 255);
```

## Using inRange to filter HSVs
```
// hue is the 0th index in hsv
Core.inRange(channels.get(0), new Scalar(minHue.value()), new Scalar(maxHue.value()), channels.get(0));
postImage(channels.get(0), "Hue thresholded");
// saturation is the 1st index in hsv
Core.inRange(channels.get(1), new Scalar(minSat.value()), new Scalar(maxSat.value()), channels.get(1));
postImage(channels.get(1), "Saturation thresholded");
// saturation is the 1st index in hsv
Core.inRange(channels.get(2), new Scalar(minVal.value()), new Scalar(maxVal.value()), channels.get(2));
postImage(channels.get(2), "Value thresholded");
```

## Recombining HSV thresholded channels
```
Mat thresholdedImage = new Mat();
Core.bitwise_and(channels.get(0), channels.get(1), thresholdedImage);
Core.bitwise_and(thresholdedImage, channels.get(2), thresholdedImage);
postImage(thresholdedImage, "Final threshed");
```

## Using erode and dilate technique to remove specks
```
// erode and dilate
Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
Imgproc.erode(thresholdedImage, thresholdedImage, kernel);
Imgproc.dilate(thresholdedImage, thresholdedImage, kernel);
postImage(thresholdedImage, "Erode and dilate");
```

It is possible to use different kernels. I find that using an erode kernel of
`(3,3)` and a dilate kernel of `(9,9)` works well.

## Finding contours

Now that we have found the objects we want, we want to find **where in our
image the objects are located**. We can do this by first creating an
`ArrayList` of the type `MatOfPoint`. In essence, a contour is just a bunch of
points that are close enough together to look like a closed figure. The type
`MatOfPoint` is exactly what it sounds like: an OpenCV Matrix object that
represents a point, which can be determined by the matrix dimensions.

```
ArrayList<MatOfPoint> contours = new ArrayList<>();
Imgproc.findContours(thresholdedImage, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
Mat frameCopy = frame.clone();
Imgproc.drawContours(frameCopy, contours, -1, new Scalar(255, 0, 255), 2);
```

So what does the above code do? Well, it finds the contours (edges) in our
thresholded image, which is binarilized. Being binarilized makes it very easy
to find edges. By using the `Imgproc.findContours` function, we can find the
contours in the thresholded image, and put those contours into our `ArrayList`.
The documentation for the `findContours` function states the following:

```
findContours(Mat image, List<MatOfPoint> contours, Mat hierarchy, int mode, int method)
```

The first 2 parameters are easy, but the rest are a bit more complicated.

## Hierarchy

An easy way to think about 'hierarchy' is like Russian Nesting Dolls, put
through an X-ray. Well, we can *see* all the dolls, but how do we know their
separate? How do we draw the dolls separately? Hierarchy allows us to define
overlap patterns within the image. Some shapes may surround others. The [OpenCV
Documentation](http://docs.opencv.org/3.1.0/d9/d8b/tutorial_py_contours_hierarchy.html)
does a very good job of taking you through this. A contour object has its own
information regarding what hierarchy it is, who is its child, who is its parent
etc.

OpenCV represents it as an array of four values : `[Next, Previous, First_Child, Parent]`
Let's look at an example. (Refer to the documentation example)

## Retrieval Mode

Now that we understand Hierarchy, we can understand the modes OpenCV gives us:

- `RETR_LIST` : This will treat all contours equally. The `First_Child` and
  `Parent` fields are empty.
- `RETR_EXTERNAL` : This will only use the outermost contours (contours that
  have no other contours that surround it)
- `RETR_CCOMP` : Consider a pure white washer on a pure black background. The
  inner ring of the contour is contour level 2, and the outer one is level 1.
- `RETR_TREE` : Retrieves all the contours and creates a full family hierarchy
  list. This is basically like a 'family tree' of contours, going from outermost
  to innermost.

## Method

- `CHAIN_APPROX_NONE` : Stores ALL points along contour
- `CHAIN_APPROX_SIMPLE` : Stores only the endpoints of straight lines

## Drawing

We can draw the contours onto an image by doing:

```
Imgproc.drawContours(frameCopy, contours, -1, new Scalar(255, 0, 255), 2);
```

This isn't really... useful however, unless we're doing a demo.

## Bounding Rectangles

OpenCV has a BoundingRect class, but I find that RotatedRect is a lot more
versatile:

```
double largestArea = 0.0;
RotatedRect largestRect = null;

for (int i = 0; i < contours.size(); i++) {
    double currArea = Imgproc.contourArea(contours.get(i));
    MatOfPoint2f tmp = new MatOfPoint2f();
    contours.get(i).convertTo(tmp, CvType.CV_32FC1);
    RotatedRect r = Imgproc.minAreaRect(tmp);
    // Draw this bounding rectangle onto `drawn`
    Point[] points = new Point[4];
    r.points(points);
    for (int line = 0; line < 4; line++) {
        Imgproc.line(base, points[line], points[(line + 1) % 4], new Scalar(127, 0, 127), 4);
    }
    if (currArea > largestArea) {
        largestArea = currArea;
        largestRect = r;
    }
}
```

If we ONLY want to draw the largest rectangle, we can move the:

```
// Draw this bounding rectangle onto `drawn`
Point[] points = new Point[4];
r.points(points);
for (int line = 0; line < 4; line++) {
    Imgproc.line(base, points[line], points[(line + 1) % 4], new Scalar(127, 0, 127), 4);
}
```

out of the larger for loop, and use `largestRect` instead of `r`

