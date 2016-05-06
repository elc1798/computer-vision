package modules;

import vision.DeviceCaptureSource;
import vision.ModuleRunner;

public class VisionModuleSuite {

    private static int demoID = 2;

    /**
     * Add any mappings here from capture sources to vision modules
     * Available capture sources:
     * - DeviceCaptureSource
     * - VideoCaptureSource
     * - ImageCaptureSource
     */

    static {
        String imageDirectory = VisionModuleSuite.class.getResource("").getPath() + "../../images/";
        switch (demoID) {
            case 0:
                ModuleRunner.addMapping(new DeviceCaptureSource(0), new HSVThresholding());
                break;
            case 1:
                ModuleRunner.addMapping(new DeviceCaptureSource(0), new FaceDetectModule());
                break;
            case 2:
                ModuleRunner.addMapping(new DeviceCaptureSource(0), new AdaptiveThresholding());
            default:
                break;
        }
    }
}
