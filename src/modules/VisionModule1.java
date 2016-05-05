package modules;

import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import gui.IntegerSliderVariable;
import vision.VisionModule;

public class VisionModule1 extends VisionModule {
    public IntegerSliderVariable minHue = new IntegerSliderVariable("Min Hue", 0, 0, 255);
    public IntegerSliderVariable maxHue = new IntegerSliderVariable("Max Hue", 0, 0, 255);
    public IntegerSliderVariable minSat = new IntegerSliderVariable("Min Saturation", 0, 0, 255);
    public IntegerSliderVariable maxSat = new IntegerSliderVariable("Max Saturation", 00, 0, 255);
    public IntegerSliderVariable minVal = new IntegerSliderVariable("Min Value", 254, 0, 255);
    public IntegerSliderVariable maxVal = new IntegerSliderVariable("Max Value", 255, 0, 255);

    public void drawBoundingRectangles(ArrayList<MatOfPoint> contours, Mat base) {
        double largestArea = 0.0;
        RotatedRect largestRect = null;

        for (int i = 0; i < contours.size(); i++) {
            double currArea = Imgproc.contourArea(contours.get(i));
            MatOfPoint2f tmp = new MatOfPoint2f();
            contours.get(i).convertTo(tmp, CvType.CV_32FC1);
            RotatedRect r = Imgproc.minAreaRect(tmp);
            if (currArea > largestArea) {
                largestArea = currArea;
                largestRect = r;
            }
        }

        if (largestRect == null) return;

        // Draw this bounding rectangle onto `drawn`
        Point[] points = new Point[4];
        largestRect.points(points);
        for (int line = 0; line < 4; line++) {
            Imgproc.line(base, points[line], points[(line + 1) % 4], new Scalar(127, 0, 127), 4);
        }
        Imgproc.circle(base, largestRect.center, 1, new Scalar(0, 0, 255), 2);

        double[] vector = new double[3];
        vector[0] = largestRect.center.x - base.width() / 2.0;
        vector[1] = largestRect.center.y - base.height() / 2.0;
        vector[2] = largestRect.angle;

        double h = base.height();
        Point textPos = new Point(0, h - 30);
        Point shadingTL = new Point(0, h - 50);
        Point shadingBR = new Point(80, h);
        Imgproc.rectangle(base, shadingTL, shadingBR, new Scalar(200, 200, 200), Core.FILLED);
        Imgproc.putText(base, "X: " + Math.round(vector[0]), textPos, 0, 0.6, new Scalar(255, 0, 0));
        textPos.y += 20;
        Imgproc.putText(base, "Y: " + Math.round(vector[1]), textPos, 0, 0.6, new Scalar(0, 0, 255));
    }

    public void run(Mat frame) {
        postImage(frame, "Master");

        // blur
        Mat blurred = new Mat();
        Imgproc.medianBlur(frame, blurred, 5);
        postImage(blurred, "Blurred");

        // split into hsv channels
        Mat hsv = new Mat();
        Imgproc.cvtColor(blurred, hsv, Imgproc.COLOR_BGR2HSV);
        ArrayList<Mat> channels = new ArrayList<Mat>();
        Core.split(hsv, channels);

        // postImage(channels.get(0), "Unthresholded Hue");
        // postImage(channels.get(1), "Unthresholded Saturation");
        // postImage(channels.get(2), "Unthresholded Value");

        // hue is the 0th index in hsv
        Core.inRange(channels.get(0), new Scalar(minHue.value()), new Scalar(maxHue.value()), channels.get(0));
        // postImage(channels.get(0), "Hue thresholded");
        // saturation is the 1st index in hsv
        Core.inRange(channels.get(1), new Scalar(minSat.value()), new Scalar(maxSat.value()), channels.get(1));
        // postImage(channels.get(1), "Saturation thresholded");
        // saturation is the 1st index in hsv
        Core.inRange(channels.get(2), new Scalar(minVal.value()), new Scalar(maxVal.value()), channels.get(2));
        // postImage(channels.get(2), "Value thresholded");

        // combine thresholded images
        Mat thresholdedImage = new Mat();
        Core.bitwise_and(channels.get(0), channels.get(1), thresholdedImage);
        Core.bitwise_and(thresholdedImage, channels.get(2), thresholdedImage);
        // postImage(thresholdedImage, "Final threshed");

        // erode and dilate
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.erode(thresholdedImage, thresholdedImage, kernel);
        Imgproc.dilate(thresholdedImage, thresholdedImage, kernel);
        postImage(thresholdedImage, "Erode and dilate");

        // find all outer contours
        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(thresholdedImage, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat frameCopy = frame.clone();
        drawBoundingRectangles(contours, frameCopy);
        postImage(frameCopy, "Contours");
        postTag("Contours", "contourCount", "Contours: " + contours.size());
    }
}
