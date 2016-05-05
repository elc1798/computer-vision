package modules;

import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import vision.VisionModule;

public class FaceDetectModule extends VisionModule {

    private String cascadeParentFolder = "/Users/photoXin/Development/cv-demo/lib/opencv-3.1.0/data/haarcascades/";

    private String getXML(String xml) {
        return cascadeParentFolder + xml;
    }

    public void runMultipleCascadeClassifiers(Mat frame, String[] XMLFILES, String imageTag) {
        ArrayList<Rect> detected = new ArrayList<Rect>();

        for (String XML : XMLFILES) {
            CascadeClassifier tmp = new CascadeClassifier(getXML(XML));
            MatOfRect detections = new MatOfRect();
            tmp.detectMultiScale(frame, detections);
            for (Rect r : detections.toArray()) {
                detected.add(r);
            }
        }

        Mat drawn = frame.clone();
        for (Rect rect : detected) {
            Imgproc.rectangle(drawn, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                new Scalar(0, 255, 0), 3);
            Imgproc.circle(drawn, new Point(rect.x + rect.width / 2, rect.y + rect.height / 2), 1, new Scalar(0, 0, 255), 2);
        }
        postImage(drawn, imageTag);
        postTag(imageTag, "Number of " + imageTag, Integer.toString(detected.size()));
    }

    public void runCascadeClassifier(Mat frame, String XMLFILE, String imageTag) {
        CascadeClassifier faceDetector = new CascadeClassifier(getXML(XMLFILE));
        MatOfRect detections = new MatOfRect();
        faceDetector.detectMultiScale(frame, detections);

        Rect[] detectedObjects = detections.toArray();
        Mat drawn = frame.clone();
        for (Rect rect : detectedObjects) {
            Imgproc.rectangle(drawn, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                new Scalar(0, 255, 0), 3);
            Imgproc.circle(drawn, new Point(rect.x + rect.width / 2, rect.y + rect.height / 2), 1, new Scalar(0, 0, 255), 2);
        }
        postImage(drawn, imageTag);
        postTag(imageTag, "Number of " + imageTag, Integer.toString(detectedObjects.length));
    }

    public void findFrontalFaces(Mat frame) {
        runCascadeClassifier(frame, "haarcascade_frontalface_alt.xml", "Faces");
    }

    public void findFaceProfiles(Mat frame) {
        runCascadeClassifier(frame, "haarcascade_profileface.xml", "Facial Profiles");
    }

    public void findFaces(Mat frame) {
        runMultipleCascadeClassifiers(
            frame,
            new String[] {
                "haarcascade_frontalface_alt.xml",
                "haarcascade_profileface.xml"
        },
            "Faces");
    }

    @Override
    public void run(Mat frame) {
        postImage(frame, "Master");
        findFrontalFaces(frame);
        // findFaceProfiles(frame);
        // findFaces(frame);
    }

}
