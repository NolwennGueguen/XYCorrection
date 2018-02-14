package main;
import IOUtils.IO;
import ij.ImageJ;
import ij.ImagePlus;
import org.opencv.core.*;
import org.opencv.features2d.*;
//import org.opencv.features2d.Features2d;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;

import static org.opencv.features2d.Features2d.NOT_DRAW_SINGLE_POINTS;

//import static org.opencv.features2d.Features2d.NOT_DRAW_SINGLE_POINTS;

public class DriftCorrection {

    public static MatOfKeyPoint findKeypoints(Mat img, int detectorType) {
        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        FeatureDetector featureDetector = FeatureDetector.create(detectorType);
        featureDetector.detect(img, keypoints);
        return keypoints;
    }

    public static Mat calculDescriptors(Mat img, MatOfKeyPoint keypoints, int descriptorType) {
        Mat img_descript = new Mat();
        DescriptorExtractor extractor = DescriptorExtractor.create(descriptorType);
        extractor.compute(img, keypoints, img_descript);
        return img_descript;
    }

    public static MatOfDMatch matchingDescriptor(Mat img1_calcul_descriptors, Mat img2_calcul_descriptors, int descriptorMatcherType) {
        MatOfDMatch matcher = new MatOfDMatch();
        DescriptorMatcher matcherDescriptor = DescriptorMatcher.create(descriptorMatcherType);
        Mat img1_descriptor = convertMatDescriptorToCV32F(img1_calcul_descriptors);
        Mat img2_descriptor = convertMatDescriptorToCV32F(img2_calcul_descriptors);
        matcherDescriptor.match(img1_descriptor, img2_descriptor, matcher);
        System.out.println("Number of Matches : " + matcher.rows());
        return matcher;
    }
////    Old Method for calculate distances
//    static ArrayList<Double> calculDistances(DMatch[] matcher, Mat img_descriptor) {
//        double max_dist = Double.MIN_VALUE;
//        double min_dist = Double.MAX_VALUE;
//        ArrayList<Double> list = new ArrayList<Double>();
//        for (int i = 0; i < img_descriptor.rows(); i++) {
//            double dist = matcher[i].distance;
//            if (dist < min_dist) {
//                min_dist = dist;
//            }
//            if (dist > max_dist) {
//                max_dist = dist;
//            }
//        }
//        list.add(min_dist);
//        list.add(max_dist);
//        return list;
//    }
//
//    static ArrayList<DMatch> selectGoodMatches(Mat img_descriptor, MatOfDMatch matcher) {
//        DMatch[] matcherArray = matcher.toArray();
//        ArrayList<DMatch> good_matchesList = new ArrayList<DMatch>();
//        ArrayList<Double> list = calculDistances(matcherArray, img_descriptor);
//        double min_dist = list.get(0);
//        for (int i = 0; i < img_descriptor.rows(); i++) {
//            if (matcherArray[i].distance < ( 1.5 * min_dist)) {
//                good_matchesList.add(matcherArray[i]);
//            }
//        }
//        System.out.println("Number of Good Matches : " + good_matchesList.size());
//        return good_matchesList;
//    }
//

    //Method to calculate distance (in pixels) between each pair of points
    public static ArrayList getDistances(MatOfDMatch matcher, MatOfKeyPoint keyPoint1, MatOfKeyPoint keyPoint2) {
        DMatch[] matcherArray = matcher.toArray();
        KeyPoint[] keypoint1Array = keyPoint1.toArray();
        KeyPoint[] keypoint2Array = keyPoint2.toArray();
        ArrayList<Double> listOfDistances = new ArrayList<>();
        double x;
        double x1;
        double x2;
        double y;
        double y1;
        double y2;
        double d;
        double d2;
        for (int i =0; i < matcherArray.length; i++) {
            int dmQuery = matcherArray[i].queryIdx;
            int dmTrain = matcherArray[i].trainIdx;

            x1 = keypoint1Array[dmQuery].pt.x;
            x2 = keypoint2Array[dmTrain].pt.x;
            x = x2 - x1;

            y1 = keypoint1Array[dmQuery].pt.y;
            y2 = keypoint2Array[dmTrain].pt.y;
            y = y2 - y1;

            d2= Math.pow(x, 2) + Math.pow(y, 2);
            d = (Math.sqrt(d2)) / 0.065;

            listOfDistances.add(d);
        }
        return listOfDistances;
    }

//    public static ArrayList<Double> calculMinMaxDistances(MatOfDMatch matcher, MatOfKeyPoint keyPoint1, MatOfKeyPoint keyPoint2, Mat max_descriptor) {
//        double max_dist = Double.MIN_VALUE;
//        double min_dist = Double.MAX_VALUE;
//        ArrayList<Double> list = new ArrayList<Double>();
//        ArrayList<Double> listOfDistances = getDistances(matcher, keyPoint1, keyPoint2);
//        for (int i = 0; i < max_descriptor.rows(); i++) {
//            double dist = listOfDistances.get(i);
//            if (dist < min_dist) {
//                min_dist = dist;
//            }
//            if (dist > max_dist) {
//                max_dist = dist;
//            }
//        }
//        System.out.println("Min dist : " + min_dist);
//        System.out.println("Max dist : " + max_dist);
//        list.add(min_dist);
//        list.add(max_dist);
//        return list;
//    }

    public  static ArrayList<DMatch> selectGoodMatches(MatOfDMatch matcher, MatOfKeyPoint keyPoint1, MatOfKeyPoint keyPoint2, Mat min_descriptor, double nmAllowed, double nmByPx) {
        DMatch[] matcherArray = matcher.toArray();
//        double max_dist = calculMinMaxDistances(matcher, keyPoint1, keyPoint2, min_descriptor).get(1);
        ArrayList<Double> listOfDistances = getDistances(matcher, keyPoint1, keyPoint2);
//        System.out.println("Size of listOfDistances : " + listOfDistances.size());
        ArrayList<DMatch> good_matchesList = new ArrayList<>();
        for (int i = 0; i < min_descriptor.rows(); i++) {
            if (listOfDistances.get(i) <= nmAllowed * nmByPx) {
                good_matchesList.add(matcherArray[i]);
            }
        }
        System.out.println("Number of Good Matches : " + good_matchesList.size());
        return good_matchesList;
    }

    static ArrayList<Float> getGoodMatchesXCoordinates(MatOfKeyPoint keypoints, ArrayList<DMatch> good_matchesList) {
        ArrayList<Float> img_xList = new ArrayList<Float>();
        KeyPoint[] keypointsArray1 = keypoints.toArray();
        float x1;
        for (int i = 0; i < good_matchesList.size() ; i++) {
            x1 = (float) keypointsArray1[i].pt.x;
            img_xList.add(x1);
        }
        return img_xList;
    }

    static ArrayList<Float> getGoodMatchesYCoordinates(MatOfKeyPoint keypoints, ArrayList<DMatch> good_matchesList) {
        ArrayList<Float> img_yList = new ArrayList<Float>();
        KeyPoint[] keypointsArray1 = keypoints.toArray();
        float y1;
        for (int i = 0; i < good_matchesList.size() ; i++) {
            y1 = (float) keypointsArray1[i].pt.y;
            img_yList.add(y1);
        }
        return img_yList;
    }

    static Float getMeanXDisplacement(ArrayList<Float> img1_xCoordinates, ArrayList<Float> img2_xCoordinates) {
        int totalNumberOfX = img1_xCoordinates.size();
        float sumXDifferencesCoordinates = 0;
        float meanXDifferencesCoordinates;
        for (int i = 0; i < img1_xCoordinates.size(); i++) {
            float xDifference = img2_xCoordinates.get(i) - img1_xCoordinates.get(i);
            sumXDifferencesCoordinates = sumXDifferencesCoordinates + xDifference;
        }
        meanXDifferencesCoordinates = sumXDifferencesCoordinates/totalNumberOfX;
        return meanXDifferencesCoordinates;
    }

    static Float getMeanYDisplacement(ArrayList<Float> img1_yCoordinates, ArrayList<Float> img2_yCoordinates) {
        int totalNumberOfY = img1_yCoordinates.size();
        float sumYDifferencesCoordinates = 0;
        float meanYDifferencesCoordinates;
        for (int i = 0; i < img1_yCoordinates.size(); i++) {
            float yDifference = img2_yCoordinates.get(i) - img1_yCoordinates.get(i);
            sumYDifferencesCoordinates = sumYDifferencesCoordinates + yDifference;
        }
        meanYDifferencesCoordinates = sumYDifferencesCoordinates/totalNumberOfY;
        return meanYDifferencesCoordinates;
    }


    // CONVERTERS
    // Convert 8bits Mat images to Buffered
    static BufferedImage convertMatCV8UC3ToBufferedImage(Mat m) {
        int type = BufferedImage.TYPE_3BYTE_BGR;
        int bufferSize = m.channels() * m.cols() * m.rows();
        byte[] b = new byte[bufferSize];
        m.get(0, 0 ,b);
        BufferedImage img = new BufferedImage(m.cols(), m.rows(), type);
        img.getRaster().setDataElements(0, 0, m.cols(), m.rows(), b);
        return img;
    }

    static BufferedImage convertMatCV8UC1ToBufferedImage(Mat m) {
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
    static byte[] toByteArray(double[] doubleArray){
        int times = Double.SIZE / Byte.SIZE;
        byte[] bytes = new byte[doubleArray.length * times];
        for(int i=0;i<doubleArray.length;i++){
            ByteBuffer.wrap(bytes, i*times, times).putDouble(doubleArray[i]);
        }
        return bytes;
    }

    // Convert 64bits Mat images to Buffered
    static BufferedImage convertMatCV64ToBufferedImage(Mat m) {
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
    static  Mat convertMatDescriptorToCV32F(Mat descriptor) {
        if (descriptor.type() != CvType.CV_32F) {
            descriptor.convertTo(descriptor, CvType.CV_32F);
        }
        return descriptor;
    }

    //Convert DMatch ArrayList to Mat
    static Mat listToMat(ArrayList<DMatch> list) {
        MatOfDMatch mat = new MatOfDMatch();
        DMatch[] array = list.toArray(new DMatch[list.size()]);
        mat.fromArray(array);
        return mat;
    }

    //Display images with ImageJ, giving a title to image
    static void displayImageIJ(String titleOfImage, Mat img) {
        ImagePlus imgp = new ImagePlus();
        if (img.type() == CvType.CV_8UC3) {imgp = new ImagePlus(titleOfImage, convertMatCV8UC3ToBufferedImage(img));}
        else if (img.type() == CvType.CV_64FC1) {imgp = new ImagePlus(titleOfImage, convertMatCV64ToBufferedImage(img));}
        else if (img.type() == CvType.CV_8UC1) {imgp = new ImagePlus(titleOfImage, convertMatCV8UC1ToBufferedImage(img));}
        else{
            try {
                throw new Exception("Unknown image type");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        imgp.show();
    }

    //Draw Goodmatches
    static Mat drawGoodMatches(Mat img1, Mat img2, MatOfKeyPoint keypoints1, MatOfKeyPoint keypoints2, ArrayList<DMatch> good_matchesList) {
        Mat good_matches = listToMat(good_matchesList);
        Mat imgGoodMatches = new Mat();
        MatOfByte matchesMask = new MatOfByte();
        Features2d.drawMatches(img1, keypoints1, img2, keypoints2, (MatOfDMatch) good_matches, imgGoodMatches, Scalar.all(-1), Scalar.all(0.5), matchesMask, NOT_DRAW_SINGLE_POINTS);
        return imgGoodMatches;
    }

    public static void main (String[] args) {//driftCorrection(String pathOfImage1, String pathOfImage2) {
        long startTime = new Date().getTime();

//        new ImageJ();
        //Load openCv Library, required besides imports
        nu.pattern.OpenCV.loadShared();

        //Load images
        Mat img1 = null;
        Mat img2 = null;
        String imgDir = System.getProperty("user.dir") + "/src/main/ressources";
        try {
            img1 = IO.readImage(imgDir + "/2-21.tif");
            img2 = IO.readImage(imgDir + "/3-21.tif");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Initialize detectors and descriptors
        Integer detectorAlgo = FeatureDetector.BRISK;
        Integer descriptorExtractor = DescriptorExtractor.ORB;
        Integer descriptorMatcher = DescriptorMatcher.FLANNBASED;

        /* 1 - Detect keypoints */
        MatOfKeyPoint keypoints1 = findKeypoints(img1, detectorAlgo);
        MatOfKeyPoint keypoints2 = findKeypoints(img2, detectorAlgo);

//        System.out.println("Number of Keypoints img1 : " + keypoints1.rows());
//        System.out.println("Number of Keypoints img2 : " + keypoints2.rows());

//        Mat img1_Keypoints = new Mat();
//        Mat img2_Keypoints = new Mat();

        //Displaying keypoints
//        Features2d.drawKeypoints(img1, keypoints1, img1_Keypoints);
//        Features2d.drawKeypoints(img2, keypoints2, img2_Keypoints);
//        displayImageIJ("Image Keypoints 1", img1_Keypoints);
//        displayImageIJ("Image Keypoints 2", img2_Keypoints);

        /* 2 - Calculate descriptors */
        Mat img1_descriptors = calculDescriptors(img1, keypoints1, descriptorExtractor);
        Mat img2_descriptors = calculDescriptors(img2, keypoints2, descriptorExtractor);

        //Determine which descriptors are the biggest and the smallest
        Mat min_descriptor;
        Mat max_descriptor;
        if (img1_descriptors.rows() < img2_descriptors.rows()) {
            min_descriptor = img1_descriptors;
            max_descriptor = img2_descriptors;
        }
        else {
            min_descriptor = img2_descriptors;
            max_descriptor = img1_descriptors;
        }

        /* 3 - Matching descriptor using FLANN matcher */
        MatOfDMatch matcher = matchingDescriptor(img1_descriptors, img2_descriptors, descriptorMatcher);
        long endTime1 = new Date().getTime();
        long timeElapsed1 = endTime1 - startTime;
//        System.out.println("Duration in milliseconds : " + timeElapsed1);
//        Mat imgMatches = new Mat();
//        Features2d.drawMatches(img1, keypoints1, img2, keypoints2, matcher, imgMatches);
//        displayImageIJ("Matches",imgMatches);

        /* 4 - Select and display Good Matches */
        double nmAllowed = 1000;
        double nmByPx = 0.065;
        ArrayList<DMatch> good_matchesList = selectGoodMatches(matcher, keypoints1, keypoints2, min_descriptor, nmAllowed, nmByPx);
//        Mat imgGoodMatches = drawGoodMatches(img1, img2, keypoints1, keypoints2, good_matchesList);
//        displayImageIJ("Good Matches", imgGoodMatches);

        /* 5 - Get coordinates of GoodMatches Keypoints */
        ArrayList<Float> img1_keypoints_xCoordinates = getGoodMatchesXCoordinates(keypoints1, good_matchesList);
        ArrayList<Float> img1_keypoints_yCoordinates = getGoodMatchesYCoordinates(keypoints1, good_matchesList);

        ArrayList<Float> img2_keypoints_xCoordinates = getGoodMatchesXCoordinates(keypoints2, good_matchesList);
        ArrayList<Float> img2_keypoints_yCoordinates = getGoodMatchesYCoordinates(keypoints2, good_matchesList);

        /* 6 - Get X and Y mean displacements */
        float meanXdisplacement = getMeanXDisplacement(img1_keypoints_xCoordinates, img2_keypoints_xCoordinates );
        float meanYdisplacement = getMeanYDisplacement(img1_keypoints_yCoordinates, img2_keypoints_yCoordinates );
        System.out.println("X mean displacement : " + meanXdisplacement);
        System.out.println("Y mean displacement : " + meanYdisplacement);

        long endTime = new Date().getTime();
        long timeElapsed = endTime - startTime;
        System.out.println("Duration in milliseconds : " + timeElapsed1);
        System.out.println("Duration in milliseconds : " + timeElapsed);
    }
}

