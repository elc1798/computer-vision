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

public class VisionModule2 extends VisionModule {

    public IntegerSliderVariable adaptive = new IntegerSliderVariable("adaptive thresh", 255, 0, 255);
    public IntegerSliderVariable blockSize = new IntegerSliderVariable("Adaptive Thresh Block Size", 500, 0, 750);
    public IntegerSliderVariable C = new IntegerSliderVariable("Adaptive Thresh C", 48, 0, 250);

    public void drawBoundingCircles(ArrayList<MatOfPoint> contours, Mat base) {

        for (int i = 0; i < contours.size(); i++) {
            Point center = new Point();
            float[] radius = new float[contours.size()];
            MatOfPoint2f tmp = new MatOfPoint2f();
            contours.get(i).convertTo(tmp, CvType.CV_32FC1);
            Imgproc.minEnclosingCircle(tmp, center, radius);
            if (Math.abs(radius[0] * radius[0] * Math.PI - Imgproc.contourArea(contours.get(i))) < 5.0) {
                Imgproc.circle(base, center, (int) radius[0], new Scalar(255, 0, 0), 2);
            }
        }

        postImage(base, "Bounding circles");
        postTag("Bounding circles", "Circles", "Circles: " + contours.size());
    }

    public void run(Mat frame) {
        postImage(frame, "Master");

        Mat blurred = new Mat();
        Imgproc.medianBlur(frame, blurred, 5);
        Mat gray = new Mat();
        Imgproc.cvtColor(blurred, gray, Imgproc.COLOR_BGR2GRAY);
        Mat adaptiveThresholded = new Mat();
        Imgproc.adaptiveThreshold(gray, adaptiveThresholded, adaptive.value(), Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,
            2 * blockSize.value() + 1,
            C.value());

        postImage(adaptiveThresholded, "Adaptive");

        // ArrayList<MatOfPoint> contours = new ArrayList<>();
        // Imgproc.findContours(thresholdedImage, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
    }
}
