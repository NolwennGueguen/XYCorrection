//package IOUtils;
//
//import ij.IJ;
//import org.opencv.core.Core;
//import org.opencv.core.CvType;
//import org.opencv.core.Mat;
//import org.opencv.imgcodecs.Imgcodecs;
//import org.opencv.imgproc.Imgproc;
//
//import java.io.*;
//import java.util.Properties;
//
//public class IO {
//    public static void printErrorToIJLog(Exception exception) {
//        StringWriter sw = new StringWriter();
//        PrintWriter ps = new PrintWriter(sw);
//        exception.printStackTrace(ps);
//        IJ.error(sw.toString());
//        try {
//            sw.close();
//        } catch (IOException e1) {
//            e1.printStackTrace();
//        }
//        ps.close();
//    }
//
//    public static void writeToFile(String filePath, Properties properties) {
//        try (PrintWriter out = new PrintWriter(filePath)) {
//            properties.store(out, "");
//        } catch (FileNotFoundException e) {
//            IO.printErrorToIJLog(e);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
    // Read images from path
//public static Mat readImage(String pathOfImage) throws IOException {
//        File f = new File(pathOfImage);
//        Mat img;
//        if(f.exists() && !f.isDirectory()) {
//        img = Imgcodecs.imread(pathOfImage, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
//        img.convertTo(img, CvType.CV_8UC1);
//        }else{
//        throw new IOException("Image file doesn't exist");
//        }
//        return img;
//        }
//
//    public static Mat equalizeImages(Mat img) {
//        Mat imgEqualized = new Mat(img.cols(), img.rows(), img.type());
//        Imgproc.equalizeHist(img, imgEqualized);
//        return imgEqualized;
//    }
//}
