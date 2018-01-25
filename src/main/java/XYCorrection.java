import ij.ImageJ;
import ij.ImagePlus;
import org.bytedeco.javacpp.opencv_core;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static org.opencv.features2d.Features2d.NOT_DRAW_SINGLE_POINTS;


public class XYCorrection {

    // Read images from path
    public static Mat readImage(String pathOfImage) {
        Mat img = Imgcodecs.imread(pathOfImage, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
//        Mat img2 = new Mat();
//        img2.convertTo(img, CvType.CV_8UC1);
        return img;
    }

//    // Correct images with warpAffine
//    public static Mat correctImages(Mat img1, Mat img2) {
//        Mat warp_matrix = Mat.eye(2,3, CvType.CV_32F); //Set and initialize the matrix of identity
//        Mat img2_corrected = new Mat();
//        int motionType = Video.MOTION_TRANSLATION;
//        Video.findTransformECC(img1, img2, warp_matrix, motionType);
//        Imgproc.warpAffine(img2, img2_corrected, warp_matrix, img1.size(),Imgproc.INTER_LINEAR + Imgproc.WARP_INVERSE_MAP);
//        return img2_corrected;
//    }

    // Get XY moves
    public static Mat getXYmoves(Mat img1, Mat img2) {

        new ImageJ();
        MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
        MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
        MatOfDMatch matches1to2 = new MatOfDMatch();
        Mat imgMatches = new Mat();
        Mat img1_Keypoints = new Mat();
        Mat img2_Keypoints = new Mat();

        Integer detectorAlgo = FeatureDetector.BRISK;
        Integer descriptorExtractor = DescriptorExtractor.BRISK;
        Integer descriptorMatcher = DescriptorMatcher.FLANNBASED;
        String detectorAlg = "BRISK";
        String descriptorExtract = "BRISK";
        String descriptorMatch = "FLANNBASED";
        String pathOfFiles = "/home/nolwenngueguen/Téléchargements/ImagesTest/";

        /* 1 - Detect keypoints */
        FeatureDetector featureDetector = FeatureDetector.create(detectorAlgo);
        featureDetector.detect(img1, keypoints1);
        featureDetector.detect(img2, keypoints2);

        //Displaying keypoints
        Features2d.drawKeypoints(img1, keypoints1, img1_Keypoints);
        Features2d.drawKeypoints(img2, keypoints2, img2_Keypoints);

        Features2d.drawMatches(img1, keypoints1, img2, keypoints2, matches1to2, imgMatches);
//        displayImageIJ("Image Keypoints 1", img1_Keypoints);
//        displayImageIJ("Image Keypoints 2", img2_Keypoints);

//        displayImageIJ("Image Matching", imgMatches);

        /* 2 - Calculate descriptors */
        DescriptorExtractor extractor = DescriptorExtractor.create(descriptorExtractor);
        Mat img1_descript = new Mat();
        Mat img2_descript = new Mat();
        extractor.compute(img1, keypoints1, img1_descript);
        extractor.compute(img2, keypoints2, img2_descript);
//        System.out.println("\nImg1 Descriptor \n"  + "Nb Rows : " + img1_descript.rows() + "\n" + "Nb Cols : " + img1_descript.cols());
//        System.out.println("\nImg2 Descriptor \n"  + "Nb Rows : " + img2_descript.rows() + "\n" + "Nb Cols : " + img2_descript.cols());

        /* 3 - Matching descriptor using FLANN matcher */
        DescriptorMatcher match1to2 = DescriptorMatcher.create(descriptorMatcher);
        //FlannBasedMatcher match1to2 = FlannBasedMatcher.create();
        MatOfDMatch match1to2convert = new MatOfDMatch();

        Mat img1_descriptor = convertMatDescriptorToCV32F(img1_descript);
        Mat img2_descriptor = convertMatDescriptorToCV32F(img2_descript);
        match1to2.match(img1_descriptor, img2_descriptor, match1to2convert);

        //Display Matches
        Features2d.drawMatches(img1, keypoints1, img2, keypoints2, match1to2convert, imgMatches);
//        displayImageIJ("Image Matching", imgMatches);
//        Imgcodecs.imwrite(pathOfFiles+"/ResultatsTests/"+detectorAlg+"_"+descriptorExtract+"_"+descriptorMatch+".tif", imgMatches);

        //Convert matches from MatOfDMatch to Array
        DMatch[] matches = match1to2convert.toArray();

        /* 4 - Calculate min/max distances between keypoints */
        double max_dist = 0;
        double min_dist = 30000000;
        for (int i =0; i < img1_descriptor.rows(); i++) {
            double dist = matches[i].distance;
            if (dist < min_dist) {
                min_dist = dist;
            }
            if (dist > max_dist) {
                max_dist = dist;
            }
        }

        System.out.println("Min dist : " + min_dist);
        System.out.println("Max dist : " + max_dist);

        //Keep only matches whose distance is < XXX *min_dist
        MatOfDMatch good_matches = new MatOfDMatch();
        ArrayList <DMatch> good_matchesArrayList = new ArrayList<DMatch>();
        for (int i=0; i< img1_descriptor.rows(); i++) {
            if (matches[i].distance < (2*min_dist)) {
                good_matchesArrayList.add(matches[i]);
            }
        }
        DMatch[] good_matchesArray = good_matchesArrayList.toArray(new DMatch[good_matchesArrayList.size()]);
        System.out.println("\nGood Matches found : " + good_matchesArray.length);
        System.out.println("All Matches : " + img1_descriptor.rows());
        good_matches.fromArray(good_matchesArray);

        //Display Good Matches
        Mat imgGoodMatches = new Mat();
        MatOfByte matchesMask = new MatOfByte();
        Features2d.drawMatches(img1, keypoints1, img2, keypoints2, good_matches, imgGoodMatches, Scalar.all(-1), Scalar.all(0.5), matchesMask, NOT_DRAW_SINGLE_POINTS);
//        displayImageIJ("Image Good Matching", imgGoodMatches);
//        Imgcodecs.imwrite(pathOfFiles+"/ResultatsTests/GoodMatch"+detectorAlg+"_"+descriptorExtract+"_"+descriptorMatch+".tif", imgGoodMatches);

        /* 5 - Localize the objects */
        ArrayList<Point> img1_ptArray = new ArrayList<Point>();
        ArrayList<Point> img2_ptArray = new ArrayList<Point>();
        KeyPoint[] keypoints1Array = keypoints1.toArray();
        KeyPoint[] keypoints2Array = keypoints2.toArray();
        // Get keypoints of good matches
        for (int i = 0; i < good_matchesArrayList.size(); i++) {
            img1_ptArray.add(keypoints1Array[good_matchesArray[i].queryIdx].pt);
            img2_ptArray.add(keypoints2Array[good_matchesArray[i].trainIdx].pt);
        }
        //Type conversion
        MatOfPoint2f img1_pt = new MatOfPoint2f();
        img1_pt.fromList(img1_ptArray);
        MatOfPoint2f img2_pt = new MatOfPoint2f();
        img2_pt.fromList(img2_ptArray);
        // Homography test and display
//        Mat outputMask = new Mat();
//        Mat homog = Calib3d.findHomography(img1_pt, img2_pt);//, Calib3d.LMEDS, 15, outputMask,2000, 0.995);
//        System.out.println(printMatContent(homog));
////        displayImageIJ("Find Homography",Homog);
////        Imgcodecs.imwrite(pathOfFiles+"/ResultatsTests/Homography"+detectorAlg+"_"+descriptorExtract+"_"+descriptorMatch+".tif", Homog);
////
//        Mat output = new Mat();
//
//        Imgproc.warpPerspective(img1, output, homog, new Size(img1.rows(),img1.cols()));
//        System.out.println(output.type());
//        displayImageIJ("Ouptut", output);

        /* End of method */
        System.out.println("\n*********************");
        System.out.println("Comparison Parameters");
        System.out.println("*********************\n");
        System.out.println("Detection Algorithm : " + detectorAlg);
        System.out.println("Extraction Algorithm : " + descriptorExtract);
        System.out.println("Matcher Algorithm : " + descriptorMatch);

        return img1_Keypoints;
    }

//    //Read images with Image J //Probably not necessary
//    public static ImagePlus readImageIJ(String pathOfImage) {
//        ImagePlus img = IJ.openImage(pathOfImage);
//        return img;
//    }

    // CONVERSIONS
    // Convert 8bits Mat images to Buffered
    public static BufferedImage convertMatCV8UC3ToBufferedImage(Mat m) {
        int type = BufferedImage.TYPE_3BYTE_BGR;
        int bufferSize = m.channels() * m.cols() * m.rows();
        byte[] b = new byte[bufferSize];
        m.get(0, 0 ,b);
        BufferedImage img = new BufferedImage(m.cols(), m.rows(), type);
        img.getRaster().setDataElements(0, 0, m.cols(), m.rows(), b);
        return img;
    }

    public static BufferedImage convertMatCV8UC1ToBufferedImage(Mat m) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        int bufferSize = m.channels() * m.cols() * m.rows();
        byte[] b = new byte[bufferSize];
        m.get(0, 0 ,b);
        BufferedImage img = new BufferedImage(m.cols(), m.rows(), type);
        img.getRaster().setDataElements(0, 0, m.cols(), m.rows(), b);
        return img;
    }

    // Convert double Array to byte Array
    //https://stackoverflow.com/questions/15533854/converting-byte-array-to-double-array
    public static byte[] toByteArray(double[] doubleArray){
        int times = Double.SIZE / Byte.SIZE;
        byte[] bytes = new byte[doubleArray.length * times];
        for(int i=0;i<doubleArray.length;i++){
            ByteBuffer.wrap(bytes, i*times, times).putDouble(doubleArray[i]);
        }
        return bytes;
    }

    // Convert 64bits Mat images to Buffered
    public static BufferedImage convertMatCV64ToBufferedImage(Mat m) {
        int type = BufferedImage.TYPE_3BYTE_BGR;
        int bufferSize = m.channels() * m.cols() * m.rows();
        double[] d = new double[bufferSize];
        m.get(0, 0, d);
        BufferedImage img = new BufferedImage(m.cols(), m.rows(), type);
        byte[] b = toByteArray(d);
        img.getRaster().getDataElements(0, 0, m.cols(), m.rows(), b);
        return img;
    }

    //Convert Descriptors to CV_32F
    public static  Mat convertMatDescriptorToCV32F(Mat descriptor) {
        if (descriptor.type() != CvType.CV_32F) {
            descriptor.convertTo(descriptor, CvType.CV_32F);
        }
        return descriptor;
    }

    // Display images
    //useless method
//    public static void displayImage(Image img) {
//        ImageIcon icon=new ImageIcon(img);
//        JFrame frame=new JFrame();
//        frame.setLayout(new FlowLayout());
//        frame.setSize(img.getWidth(null)+50, img.getHeight(null)+50);
//        JLabel lbl=new JLabel();
//        lbl.setIcon(icon);
//        frame.add(lbl);
//        frame.setVisible(true);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//    }

    // Display images with ImageJ
    public static ImagePlus displayImageIJ(Mat img) {
        ImagePlus imgp = new ImagePlus();
        if (img.type() == CvType.CV_8UC3) {imgp = new ImagePlus("", convertMatCV8UC3ToBufferedImage(img));}
        else if (img.type() == CvType.CV_64FC1) {imgp = new ImagePlus("", convertMatCV64ToBufferedImage(img));}
        else if (img.type() == CvType.CV_8UC1) {imgp = new ImagePlus("", convertMatCV8UC1ToBufferedImage(img));}
        imgp.show();
        return imgp;
    }

    //Display images with ImageJ, giving a title to image
    public static ImagePlus displayImageIJ(String titleOfImage, Mat img) {
        ImagePlus imgp = new ImagePlus();
        if (img.type() == CvType.CV_8UC3) {imgp = new ImagePlus(titleOfImage, convertMatCV8UC3ToBufferedImage(img));}
        else if (img.type() == CvType.CV_64FC1) {imgp = new ImagePlus(titleOfImage, convertMatCV64ToBufferedImage(img));}
        else if (img.type() == CvType.CV_8UC1) {imgp = new ImagePlus(titleOfImage, convertMatCV8UC1ToBufferedImage(img));}
        imgp.show();
        return imgp;
    }

    //Display content of matrix
    public static String printMatContent(Mat img) {
        Mat matArray = new Mat(img.rows(), img.cols(),CvType.CV_8UC1);
        for(int row=0;row<img.rows();row++){
            for(int col=0;col<img.cols();col++) {
                matArray.put(row, col, img.get(row, col));
            }
        }
        System.out.println("Printing the matrix dump");
        return matArray.dump();
    }

    public static void main(String[] args) {
        long startTime = new Date().getTime();

        //Load openCv Library, required besides imports
        System.load("/home/nolwenngueguen/Téléchargements/opencv-3.4.0/build/lib/libopencv_java340.so");

        Mat img1 = readImage("/home/nolwenngueguen/Téléchargements/ImagesTest/1-21.tif");
        Mat img2 = readImage("/home/nolwenngueguen/Téléchargements/ImagesTest/2-21.tif");
//        Mat img2_corrected = correctImages(img1, img2);
        getXYmoves(img1, img2);

        long endTime = new Date().getTime();
        long timeElapsed = endTime - startTime;
        System.out.println("Duration in milliseconds : " + timeElapsed);

    }
}
