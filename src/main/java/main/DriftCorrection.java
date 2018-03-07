package main;
import ij.ImageJ;
import ij.ImagePlus;
import org.opencv.core.*;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import static org.opencv.features2d.Features2d.NOT_DRAW_SINGLE_POINTS;

public class DriftCorrection {

    public static final double UMPERMIN = 3000;
    public static final double INTERVALINMIN = 30;
    public static final double UMPERPIX = 0.065;
    public static final Integer DETECTORALGO = FeatureDetector.BRISK;
    public static final Integer DESCRIPTOREXTRACTOR = DescriptorExtractor.ORB;
    public static final Integer DESCRIPTORMATCHER = DescriptorMatcher.FLANNBASED;

    public static Mat readImage(String pathOfImage) {
        Mat img = Imgcodecs.imread(pathOfImage, CvType.CV_16UC1);
        Mat img1 = new Mat(img.cols(), img.rows(), CvType.CV_8UC1);
        img.convertTo(img1, CvType.CV_8UC1, 0.00390625);
        Mat img2 = equalizeImages(img1);
        return img2;
    }

    private static Mat equalizeImages(Mat img) {
        Mat imgEqualized = new Mat(img.cols(), img.rows(), img.type());
        Imgproc.equalizeHist(img, imgEqualized);
        return imgEqualized;
    }

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
        return matcher;
    }

    //Calculate distance (in pixels) between each pair of points :
    private static ArrayList<Double> getDistances(MatOfDMatch matcher, MatOfKeyPoint keyPoint1, MatOfKeyPoint keyPoint2) {
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
        for (DMatch aMatcherArray : matcherArray) {
            int dmQuery = aMatcherArray.queryIdx;
            int dmTrain = aMatcherArray.trainIdx;

            x1 = keypoint1Array[dmQuery].pt.x;
            x2 = keypoint2Array[dmTrain].pt.x;
            x = x2 - x1;

            y1 = keypoint1Array[dmQuery].pt.y;
            y2 = keypoint2Array[dmTrain].pt.y;
            y = y2 - y1;

            d = Math.hypot(x, y);
            listOfDistances.add(d);
        }
        return listOfDistances;
    }

    public  static ArrayList<DMatch> selectGoodMatches(MatOfDMatch matcher, MatOfKeyPoint keyPoint1, MatOfKeyPoint keyPoint2, double umPerMin, double umPerPixel, double intervalInMin) {
        DMatch[] matcherArray = matcher.toArray();
        ArrayList<Double> listOfDistances = getDistances(matcher, keyPoint1, keyPoint2);
        ArrayList<DMatch> good_matchesList = new ArrayList<>();
        for (int i = 0; i < matcherArray.length; i++) {
            if (listOfDistances.get(i) <= (umPerMin/intervalInMin)){ // /umPerPixel) * intervalInMin) {
                good_matchesList.add(matcherArray[i]);
            }
        }
        return good_matchesList;
    }

    private static ArrayList<Float> getGoodMatchesXCoordinates(MatOfKeyPoint keypoints, ArrayList<DMatch> good_matchesList, Boolean isReferenceImage) {
        ArrayList<Float> img_xList = new ArrayList<>();
        KeyPoint[] keypointsArray1 = keypoints.toArray();
        float x;
        int id;
        for (DMatch aGood_matchesList : good_matchesList) {
            if (isReferenceImage) {
                id = aGood_matchesList.queryIdx;
            } else {
                id = aGood_matchesList.trainIdx;
            }
            x = (float) keypointsArray1[id].pt.x;
            img_xList.add(x);
        }
        return img_xList;
    }

    private static ArrayList<Float> getGoodMatchesYCoordinates(MatOfKeyPoint keypoints, ArrayList<DMatch> good_matchesList, Boolean isReferenceImage) {
        ArrayList<Float> img_yList = new ArrayList<>();
        KeyPoint[] keypointsArray1 = keypoints.toArray();
        float y;
        int id;
        for (DMatch aGood_matchesList : good_matchesList) {
            if (isReferenceImage) {
                id = aGood_matchesList.queryIdx;
            } else {
                id = aGood_matchesList.trainIdx;
            }
            y = (float) keypointsArray1[id].pt.y;
            img_yList.add(y);
        }
        return img_yList;
    }

    private static Float getMeanXDisplacement(ArrayList<Float> img1_xCoordinates, ArrayList<Float> img2_xCoordinates) {
        int totalNumberOfX = img1_xCoordinates.size();
        float sumXDistancesCoordinates = 0;
        float meanXDifferencesCoordinates;
        for (int i = 0; i < img1_xCoordinates.size(); i++) {
            float xDistance = img2_xCoordinates.get(i) - img1_xCoordinates.get(i);
            sumXDistancesCoordinates += xDistance;
        }
        meanXDifferencesCoordinates = sumXDistancesCoordinates/totalNumberOfX;
        return meanXDifferencesCoordinates;
    }

    private static Float getMeanYDisplacement(ArrayList<Float> img1_yCoordinates, ArrayList<Float> img2_yCoordinates) {
        int totalNumberOfY = img1_yCoordinates.size();
        float sumYDistancesCoordinates = 0;
        float meanYDifferencesCoordinates;
        for (int i = 0; i < img1_yCoordinates.size(); i++) {
            float yDifference = img2_yCoordinates.get(i) - img1_yCoordinates.get(i);
            sumYDistancesCoordinates += yDifference;
        }
        meanYDifferencesCoordinates = sumYDistancesCoordinates/totalNumberOfY;
        return meanYDifferencesCoordinates;
    }

    private static Float getXVariance(ArrayList<Float> img1_xCoordinates, ArrayList<Float> img2_xCoordinates, Float meanXDisplacement) {
        int totalNumberOfX = img1_xCoordinates.size();
        float sumDiffSquared = 0;
        float varianceX;
        for (int i = 0; i < img1_xCoordinates.size(); i++) {
            float xDiff = img2_xCoordinates.get(i) - img1_xCoordinates.get(i);
            sumDiffSquared += Math.pow(xDiff - meanXDisplacement, 2);
        }
        varianceX = sumDiffSquared/totalNumberOfX;
        return  varianceX;
    }

    private static Float getYVariance(ArrayList<Float> img1_yCoordinates, ArrayList<Float> img2_yCoordinates, Float meanYDisplacement) {
        int totalNumberOfY = img1_yCoordinates.size();
        float sumDiffSquared = 0;
        float varianceY;
        for (int i = 0; i < img1_yCoordinates.size(); i++) {
            float yDiff = img2_yCoordinates.get(i) - img1_yCoordinates.get(i);
            sumDiffSquared += Math.pow(yDiff - meanYDisplacement, 2);
        }
        varianceY = sumDiffSquared/totalNumberOfY;
        return varianceY;
    }

    // CONVERTERS
    // Convert 8bits 3 Channels Mat images to Buffered
    private static BufferedImage convertMatCV8UC3ToBufferedImage(Mat m) {
        int type = BufferedImage.TYPE_3BYTE_BGR;
        int bufferSize = m.channels() * m.cols() * m.rows();
        byte[] b = new byte[bufferSize];
        m.get(0, 0 ,b);
        BufferedImage img = new BufferedImage(m.cols(), m.rows(), type);
        img.getRaster().setDataElements(0, 0, m.cols(), m.rows(), b);
        return img;
    }
    // Convert 8bits 1 Channel Mat images to Buffered
    private static BufferedImage convertMatCV8UC1ToBufferedImage(Mat m) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        int bufferSize = m.channels() * m.cols() * m.rows();
        byte[] b = new byte[bufferSize];
        m.get(0, 0 ,b);
        BufferedImage img = new BufferedImage(m.cols(), m.rows(), type);
        img.getRaster().setDataElements(0, 0, m.cols(), m.rows(), b);
        return img;
    }
    // Convert 64bits Mat images to Buffered
    private static BufferedImage convertMatCV64ToBufferedImage(Mat m) {
        int type = BufferedImage.TYPE_3BYTE_BGR;
        int bufferSize = m.channels() * m.cols() * m.rows();
        double[] d = new double[bufferSize];
        m.get(0, 0, d);
        BufferedImage img = new BufferedImage(m.cols(), m.rows(), type);
        byte[] b = toByteArray(d);
        img.getRaster().getDataElements(0, 0, m.cols(), m.rows(), b);
        return img;
    }

    // Convert double Array to byte Array
    //https://stackoverflow.com/questions/15533854/converting-byte-array-to-double-array
    private static byte[] toByteArray(double[] doubleArray){
        int times = Double.SIZE / Byte.SIZE;
        byte[] bytes = new byte[doubleArray.length * times];
        for(int i=0;i<doubleArray.length;i++){
            ByteBuffer.wrap(bytes, i*times, times).putDouble(doubleArray[i]);
        }
        return bytes;
    }

    //Convert Descriptors to CV_32F
    private static  Mat convertMatDescriptorToCV32F(Mat descriptor) {
        if (descriptor.type() != CvType.CV_32F) {
            descriptor.convertTo(descriptor, CvType.CV_32F);
        }
        return descriptor;
    }

    //Convert DMatch ArrayList to Mat
    private static Mat listToMat(ArrayList<DMatch> list) {
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

    public static void main (String[] args) { //double[] driftCorrection(Mat img1, Mat img2) {
//        new ImageJ();
//        long start = Date.st
        //Load openCv Library, required besides imports
        nu.pattern.OpenCV.loadShared();

//        //Load images
//        Mat img1 = null;
//        Mat img2 = null;
//        String imgDir = System.getProperty("user.dir") + "/src/main/ressources";
//        try {
//            img1 = IO.readImage(imgDir + "/4-21.tif");
//            img2 = IO.readImage(imgDir + "/6-21.tif");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        Mat img1 = readImage("/home/dataNolwenn/ImagesTest/Test1.tif");
        Mat img2 = readImage("/home/dataNolwenn/ImagesTest/Test2.tif");

        /* 1 - Detect keypoints */
        MatOfKeyPoint keypoints1 = findKeypoints(img1, DETECTORALGO);
        MatOfKeyPoint keypoints2 = findKeypoints(img2, DETECTORALGO);
        Mat img1_Keypoints = new Mat();
        Mat img2_Keypoints = new Mat();
        Features2d.drawKeypoints(img1, keypoints1, img1_Keypoints);
        Features2d.drawKeypoints(img2, keypoints2, img2_Keypoints);
        displayImageIJ("Img1", img1_Keypoints);
        displayImageIJ("Img2", img2_Keypoints);


        /* 2 - Calculate descriptors */
        Mat img1_descriptors = calculDescriptors(img1, keypoints1, DESCRIPTOREXTRACTOR);
        Mat img2_descriptors = calculDescriptors(img2, keypoints2, DESCRIPTOREXTRACTOR);

        /* 3 - Matching descriptor using FLANN matcher */
        MatOfDMatch matcher = matchingDescriptor(img1_descriptors, img2_descriptors, DESCRIPTORMATCHER);
        System.out.println("Number of Matches : " + matcher.rows());

        Mat img1_keypoints = new Mat();
        Mat img2_keypoints = new Mat();
        Mat img1_img2_matches = new Mat();
        Features2d.drawKeypoints(img1, keypoints1, img1_keypoints);
        Features2d.drawKeypoints(img2, keypoints2, img2_keypoints);
        Features2d.drawMatches(img1, keypoints1, img2, keypoints2, matcher, img1_img2_matches);
//        Imgcodecs.imwrite("/home/nolwenngueguen/Téléchargements/ImagesTest/ImagesPNG/img1Keypoints.tif", img1_keypoints);
//        Imgcodecs.imwrite("/home/nolwenngueguen/Téléchargements/ImagesTest/ImagesPNG/img2Keypoints.tif", img2_keypoints);
//        Imgcodecs.imwrite("/home/nolwenngueguen/Téléchargements/ImagesTest/ImagesPNG/img1img2Matches.tif", img1_img2_matches);

        /* 4 - Select and display Good Matches */
        ArrayList<DMatch> good_matchesList = selectGoodMatches(matcher, keypoints1, keypoints2, UMPERMIN, UMPERPIX, INTERVALINMIN);
        System.out.println("Number of Good Matches : " + good_matchesList.size());

        Mat imgGoodMatches = drawGoodMatches(img1, img2, keypoints1, keypoints2, good_matchesList);
//        displayImageIJ("Good Matches", imgGoodMatches);
//        Imgcodecs.imwrite("/home/nolwenngueguen/Téléchargements/ImagesTest/ImagesPNG/goodMatches.tif", imgGoodMatches);

        /* 5 - Get coordinates of GoodMatches Keypoints */
        ArrayList<Float> img1_keypoints_xCoordinates = getGoodMatchesXCoordinates(keypoints1, good_matchesList,true);
        ArrayList<Float> img1_keypoints_yCoordinates = getGoodMatchesYCoordinates(keypoints1, good_matchesList, true);

        ArrayList<Float> img2_keypoints_xCoordinates = getGoodMatchesXCoordinates(keypoints2, good_matchesList,false);
        ArrayList<Float> img2_keypoints_yCoordinates = getGoodMatchesYCoordinates(keypoints2, good_matchesList, false);

        /* 6 - Get X and Y mean displacements */
        float meanXdisplacement = getMeanXDisplacement(img1_keypoints_xCoordinates, img2_keypoints_xCoordinates );
        float meanYdisplacement = getMeanYDisplacement(img1_keypoints_yCoordinates, img2_keypoints_yCoordinates );
        System.out.println("X mean displacement : " + meanXdisplacement);
        System.out.println("Y mean displacement : " + meanYdisplacement + "\n");

        double xVariance = getXVariance(img1_keypoints_xCoordinates, img2_keypoints_xCoordinates, meanXdisplacement);
        double yVariance = getYVariance(img1_keypoints_yCoordinates, img2_keypoints_yCoordinates, meanYdisplacement);
        System.out.println("X variance : " + xVariance);
        System.out.println("Y variance : " + yVariance + "\n");

//        return new double[]{(double) meanXdisplacement, (double) meanYdisplacement, (double) matcher.rows(), (double) good_matchesList.size()};
    }
}

