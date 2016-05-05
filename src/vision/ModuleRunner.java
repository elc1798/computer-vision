package vision;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import util.DebugPrinter;

public class ModuleRunner {
    private static ArrayList<CaptureSourceToVisionModuleMapper> sourceDestMap = new ArrayList<CaptureSourceToVisionModuleMapper>();
    private static final int FPS = 10;

    static {
        DebugPrinter.println("OpenCV version: " + Core.VERSION);
        DebugPrinter.println("Native library path: " + System.getProperty("java.library.path"));
        // Load OpenCV Library
        ArrayList<String> possibleFiles = new ArrayList<String>();
        for (String path : System.getProperty("java.library.path").split(":")) {
            File directory = new File(path);
            assert (directory.isDirectory());
            File[] files = directory.listFiles();
            if (files == null) continue;
            for (File f : files) {
                if (f.getName().contains("java") && f.getName().contains(".so")) {
                    possibleFiles.add(f.getAbsolutePath());
                }
            }
        }
        DebugPrinter.println("FOUND POSSIBLE JAVA LINKED LIBRARIES: " + possibleFiles.toString());
        boolean success = false;
        for (String possibility : possibleFiles) {
            if (success) break;
            try {
                System.load(possibility);
                success = true;
            } catch (Exception e) {
                DebugPrinter.println(possibility + " Failed to load");
            }
        }
        if (!success) {
            try {
                System.load("/Users/photoXin/Development/cv-demo/lib/opencv-3.1.0/build/lib/libopencv_java310.so");
            } catch (Exception e) {
                DebugPrinter.println("FAILED LOADING OPENCV");
                System.exit(1);
            }
        }
    }

    public static void addMapping(CaptureSource captureSource, VisionModule... modules) {
        sourceDestMap.add(new CaptureSourceToVisionModuleMapper(captureSource, modules));
    }

    public void run() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    for (CaptureSourceToVisionModuleMapper captureSourceMap : sourceDestMap) {
                        if (captureSourceMap.captureSource.isOpened()) {
                            Mat frame = captureSourceMap.captureSource.read();
                            if (frame == null) {
                                // FIXME: We're reinitializing the capture
                                // source so we can loop it when we've reached
                                // the end of the stream. The proper method
                                // would be to set the frame pointer for the
                                // source to point back to the beginning of the
                                // stream, but this method does not
                                // reliably work.
                                captureSourceMap.captureSource.reinitializeCaptureSource();
                                DebugPrinter.println("Looping capture source");
                            } else {
                                for (VisionModule module : captureSourceMap.modules) {
                                    Thread t = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            long start = System.currentTimeMillis();
                                            module.run(frame);
                                            long duration = System.currentTimeMillis() - start;
                                            // DebugPrinter.println(module.getName()
                                            // + ": " + duration + " ms");
                                        }
                                    }, module.getName() + " Thread");
                                    t.setDaemon(true);
                                    t.start();
                                }
                            }
                        }
                    }
                    try {
                        Thread.sleep(1000 / FPS);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }, "Module Runner Thread");
        t.start();
    }

    public ArrayList<VisionModule> getModules() {
        ArrayList<VisionModule> modules = new ArrayList<VisionModule>();
        for (CaptureSourceToVisionModuleMapper map : sourceDestMap) {
            for (VisionModule module : map.modules) {
                modules.add(module);
            }
        }
        return modules;
    }

    private static class CaptureSourceToVisionModuleMapper {
        private CaptureSource captureSource;
        private VisionModule[] modules;

        public CaptureSourceToVisionModuleMapper(CaptureSource captureSource, VisionModule... modules) {
            this.captureSource = captureSource;
            this.modules = modules;
        }
    }

}
