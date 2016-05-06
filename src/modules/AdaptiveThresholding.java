package modules;

import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import gui.IntegerSliderVariable;
import vision.VisionModule;

public class AdaptiveThresholding extends VisionModule {

    public IntegerSliderVariable minHue = new IntegerSliderVariable("Min Hue", 109, 0, 255);
    public IntegerSliderVariable maxHue = new IntegerSliderVariable("Max Hue", 168, 0, 255);
    public IntegerSliderVariable minSat = new IntegerSliderVariable("Min Saturation", 103, 0, 255);
    public IntegerSliderVariable maxSat = new IntegerSliderVariable("Max Saturation", 144, 0, 255);
    public IntegerSliderVariable minVal = new IntegerSliderVariable("Min Value", 0, 0, 255);
    public IntegerSliderVariable maxVal = new IntegerSliderVariable("Max Value", 255, 0, 255);

    public IntegerSliderVariable adaptive = new IntegerSliderVariable("adaptive thresh", 255, 0, 255);
    public IntegerSliderVariable blockSize = new IntegerSliderVariable("Adaptive Thresh Block Size", 750, 0, 750);
    public IntegerSliderVariable C = new IntegerSliderVariable("Adaptive Thresh C", 0, 0, 250);
    public IntegerSliderVariable INV = new IntegerSliderVariable("Invert", 0, 0, 2);

    public void run(Mat frame) {
        postImage(frame, "Master");

        Mat blurred = new Mat();
        Imgproc.medianBlur(frame, blurred, 5);
        Mat hsv = new Mat();
        Imgproc.cvtColor(blurred, hsv, Imgproc.COLOR_BGR2HSV);
        ArrayList<Mat> channels = new ArrayList<Mat>();
        Core.split(hsv, channels);

        Core.inRange(channels.get(0), new Scalar(minHue.value()), new Scalar(maxHue.value()), channels.get(0));
        Core.inRange(channels.get(1), new Scalar(minSat.value()), new Scalar(maxSat.value()), channels.get(1));
        postImage(channels.get(0), "Hue");
        postImage(channels.get(1), "Sat");

        Mat adaptiveThresholded = new Mat();
        Imgproc.adaptiveThreshold(channels.get(2), adaptiveThresholded, adaptive.value(), Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
            INV.value() == 0 ? Imgproc.THRESH_BINARY : Imgproc.THRESH_BINARY_INV,
            2 * blockSize.value() + 1,
            C.value());

        postImage(adaptiveThresholded, "Adaptive");

        Mat thresholdedImage = new Mat();
        Core.bitwise_and(adaptiveThresholded, channels.get(0), thresholdedImage);
        Core.bitwise_and(thresholdedImage, channels.get(1), thresholdedImage);

        // Erode and dilate to remove noise
        Mat erodeKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Mat dilateKernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(9, 9));
        Imgproc.erode(thresholdedImage, thresholdedImage, erodeKernel);
        Imgproc.dilate(thresholdedImage, thresholdedImage, dilateKernel);
        postImage(thresholdedImage, "Combined");
    }
}
