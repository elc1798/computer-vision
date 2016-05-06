package modules;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import gui.DoubleSliderVariable;
import gui.IntegerSliderVariable;
import vision.VisionModule;

public class CircleDetection extends VisionModule {

    public IntegerSliderVariable blurKernel = new IntegerSliderVariable("Blur Kernel", 1, 1, 4);
    public IntegerSliderVariable sigmaX = new IntegerSliderVariable("SigmaX", 0, 0, 10);
    public DoubleSliderVariable areaThresh = new DoubleSliderVariable("Area Thresh", 0.0, 0.0, 1000.0);

    public void drawCircles(Mat frame, Mat circles) {
        System.out.println(circles.dump());
        try {
            Imgproc.circle(frame, new Point(circles.get(0, 0)[0], circles.get(0, 0)[1]), (int) circles.get(0, 0)[2], new Scalar(0, 255, 0), 2);
        } catch (Exception e) {
        }
        postImage(frame, "CIRCLES");
    }

    public void run(Mat frame) {
        postImage(frame, "Master");

        Mat blurred = new Mat();
        Imgproc.GaussianBlur(frame, blurred, new Size(blurKernel.value() * 2 + 1, blurKernel.value() * 2 + 1), sigmaX.value());

        Mat gray = new Mat();
        Imgproc.cvtColor(blurred, gray, Imgproc.COLOR_BGR2GRAY);

        postImage(gray, "Gray");

        Mat circles = new Mat();
        Imgproc.HoughCircles(gray, circles, Imgproc.HOUGH_GRADIENT, 1, gray.rows() / 20);

        Mat frameCopy = frame.clone();
        drawCircles(frameCopy, circles);
    }

}
