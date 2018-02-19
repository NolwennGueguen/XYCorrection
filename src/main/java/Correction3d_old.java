import ij.ImageJ;
import ij.ImagePlus;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.opencv.features2d.Features2d.NOT_DRAW_SINGLE_POINTS;


public class Correction3d_old {

    // Read images from path
    public static Mat readImage(String pathOfImage) {
        Mat img = Imgcodecs.imread(pathOfImage, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
        img.convertTo(img, CvType.CV_8UC1);
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

    static MatOfKeyPoint findKeypoints(Mat img, int detectorType) {
        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        FeatureDetector featureDetector = FeatureDetector.create(detectorType);
        featureDetector.detect(img, keypoints);
        return keypoints;
    }

    static Mat calculDescriptors(Mat img, MatOfKeyPoint keypoints, int descriptorType) {
        Mat img_descript = new Mat();
        DescriptorExtractor extractor = DescriptorExtractor.create(descriptorType);
        extractor.compute(img, keypoints, img_descript);
        return img_descript;
    }

    static MatOfDMatch matchingDescriptor(Mat img1_calcul_descriptors, Mat img2_calcul_descriptors, int descriptorMatcherType) {
        MatOfDMatch matcherToConvert = new MatOfDMatch();
        DescriptorMatcher matcher = DescriptorMatcher.create(descriptorMatcherType);
        Mat img1_descriptor = convertMatDescriptorToCV32F(img1_calcul_descriptors);
        Mat img2_descriptor = convertMatDescriptorToCV32F(img2_calcul_descriptors);
        matcher.match(img1_descriptor, img2_descriptor, matcherToConvert);
        return matcherToConvert;
    }

    static ArrayList<Double> calculDistances(DMatch[] matcher, Mat img_descriptor) {
        double max_dist = Double.MIN_VALUE;
        double min_dist = Double.MAX_VALUE;
        ArrayList<Double> list = new ArrayList<Double>();
        for (int i = 0; i < img_descriptor.rows(); i++) {
            double dist = matcher[i].distance;
            if (dist < min_dist) {
                min_dist = dist;
            }
            if (dist > max_dist) {
                max_dist = dist;
            }
        }
        list.add(min_dist);
        list.add(max_dist);
        return list;
    }

    static ArrayList<DMatch> selectGoodMatches(Mat img_descriptor, MatOfDMatch matcher) {
        DMatch[] matcherArray = matcher.toArray();
        ArrayList<DMatch> good_matchesList = new ArrayList<DMatch>();
        ArrayList<Double> list = calculDistances(matcherArray, img_descriptor);
        double min_dist = list.get(0);
        for (int i = 0; i < img_descriptor.rows(); i++) {
            if (matcherArray[i].distance < ( 2 * min_dist)) {
                good_matchesList.add(matcherArray[i]);
            }
        }
        System.out.println("Number of Good Matches : " + good_matchesList.size());
        return good_matchesList;
    }

    static Mat drawGoodMatches(Mat img1, Mat img2, MatOfKeyPoint keypoints1, MatOfKeyPoint keypoints2, ArrayList<DMatch> good_matchesList) {
        Mat good_matches = listToMat(good_matchesList);
        Mat imgGoodMatches = new Mat();
        MatOfByte matchesMask = new MatOfByte();
        Features2d.drawMatches(img1, keypoints1, img2, keypoints2, (MatOfDMatch) good_matches, imgGoodMatches, Scalar.all(-1), Scalar.all(0.5), matchesMask, NOT_DRAW_SINGLE_POINTS);
        return imgGoodMatches;
    }

    static  ArrayList<MatOfPoint2f> localizeObject(MatOfKeyPoint keypoints1, MatOfKeyPoint keypoints2, ArrayList<DMatch> good_matchesList) {
        DMatch[] good_matchesArray = listToArray(good_matchesList);
        ArrayList<MatOfPoint2f> imgs_ptList = new ArrayList<MatOfPoint2f>();
        ArrayList<Point> img1_ptList = new ArrayList<Point>();
        ArrayList<Point> img2_ptList = new ArrayList<Point>();
        KeyPoint[] keypointsArray1 = keypoints1.toArray();
        KeyPoint[] keypointsArray2 = keypoints2.toArray();
        for (int i = 0; i < good_matchesList.size() ; i++) {
            img1_ptList.add(keypointsArray1[good_matchesArray[i].queryIdx].pt);
            img2_ptList.add(keypointsArray2[good_matchesArray[i].trainIdx].pt);
        }
        MatOfPoint2f img1_pt = new MatOfPoint2f();
        img1_pt.fromList(img1_ptList);
        MatOfPoint2f img2_pt = new MatOfPoint2f();
        img2_pt.fromList(img2_ptList);
        imgs_ptList.add(img1_pt);
        imgs_ptList.add(img2_pt);
        return imgs_ptList;
    }

    static  ArrayList<Float> getGoodMatchesCoordinates(MatOfKeyPoint keypoints, ArrayList<DMatch> good_matchesList) {
        ArrayList<Float> img_xList = new ArrayList<Float>();
        ArrayList<Float> img_yList = new ArrayList<Float>();
        ArrayList<Float> img_ptList = new ArrayList<Float>();
        KeyPoint[] keypointsArray1 = keypoints.toArray();
        float x1;
        float y1;
        for (int i = 0; i < good_matchesList.size() ; i++) {
            x1 = (float) keypointsArray1[i].pt.x;
            y1 = (float) keypointsArray1[i].pt.y;
            img_xList.add(x1);
            img_yList.add(y1);
        }
        img_ptList.addAll(img_xList);
        img_ptList.addAll(img_yList);
        return img_ptList;
    }

    // CONVERTERS
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

    //Convert DMatch ArrayList to DMatch Array
    static DMatch[] listToArray(ArrayList<DMatch> list) {
        DMatch[] array = list.toArray(new DMatch[list.size()]);
        return array;
    }

    //Convert DMatch ArrayList to Mat
    static Mat listToMat(ArrayList<DMatch> list) {
        MatOfDMatch mat = new MatOfDMatch();
        DMatch[] array = list.toArray(new DMatch[list.size()]);
        mat.fromArray(array);
        return mat;
    }

    //Convert DMatch Array to Mat
    static Mat arrayToMat(DMatch[] array) {
        MatOfDMatch mat = new MatOfDMatch();
        mat.fromArray(array);
        return mat;
    }

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

    // Get XY moves
    public static void getXYmoves(Mat img1, Mat img2) {

        new ImageJ();
        Mat img1_Keypoints = new Mat();
        Mat img2_Keypoints = new Mat();

        Integer detectorAlgo = FeatureDetector.BRISK;
        Integer descriptorExtractor = DescriptorExtractor.BRISK;
        Integer descriptorMatcher = DescriptorMatcher.BRUTEFORCE;
        String detectorAlg = "BRISK";
        String descriptorExtract = "BRISK";
        String descriptorMatch = "FLANNBASED";
        String pathOfFiles = "/home/nolwenngueguen/Téléchargements/ImagesTest/";

        /* 1 - Detect keypoints */
        MatOfKeyPoint keypoints1 = findKeypoints(img1, detectorAlgo);
        MatOfKeyPoint keypoints2 = findKeypoints(img2, detectorAlgo);

        //Displaying keypoints
        Features2d.drawKeypoints(img1, keypoints1, img1_Keypoints);
        Features2d.drawKeypoints(img2, keypoints2, img2_Keypoints);
        System.out.println("Number of keypoints in image 1 : " + keypoints1.size());
        System.out.println("Number of keypoints in image 2 : " + keypoints2.size());

//        displayImageIJ("Image Keypoints 1", img1_Keypoints);
//        displayImageIJ("Image Keypoints 2", img2_Keypoints);

        /* 2 - Calculate descriptors */
        Mat img1_descriptors = calculDescriptors(img1, keypoints1, descriptorExtractor);
        Mat img2_descriptors = calculDescriptors(img2, keypoints2, descriptorExtractor);

        /* 3 - Matching descriptor using FLANN matcher */
        MatOfDMatch matcher = matchingDescriptor(img1_descriptors, img2_descriptors, descriptorMatcher);

        //Display Matches
        Mat imgMatches = new Mat();
        Features2d.drawMatches(img1, keypoints1, img2, keypoints2, matcher, imgMatches);
        System.out.println("Number of matches : " + matcher.size());
//        displayImageIJ("Image Matching", imgMatches);
//        Imgcodecs.imwrite(pathOfFiles+"/ResultatsTests/"+detectorAlg+"_"+descriptorExtract+"_"+descriptorMatch+".tif", imgMatches);

        /* 4 - Select and display Good Matches */
        ArrayList<DMatch> good_matchesList = selectGoodMatches(img1_descriptors, matcher);
        Mat imgGoodMatches = drawGoodMatches(img1, img2, keypoints1, keypoints2, good_matchesList);
//        displayImageIJ("Good Matches", imgGoodMatches);
//        Imgcodecs.imwrite(pathOfFiles+"/ResultatsTests/GoodMatch"+detectorAlg+"_"+descriptorExtract+"_"+descriptorMatch+".tif", imgGoodMatches);

        /* 5 - Localize the objects */
        ArrayList<MatOfPoint2f> imgs_ptList = localizeObject(keypoints1, keypoints2, good_matchesList);
        MatOfPoint2f img1_pt = imgs_ptList.get(0);
        MatOfPoint2f img2_pt = imgs_ptList.get(1);
        // Homography test and display
        Mat outputMask = new Mat();
        Mat homog = Calib3d.findHomography(img1_pt, img2_pt);//, Calib3d.LMEDS, 15, outputMask,2000, 0.995);
//        displayImageIJ("Find Homography",Homog);
//        Imgcodecs.imwrite(pathOfFiles+"/ResultatsTests/Homography"+detectorAlg+"_"+descriptorExtract+"_"+descriptorMatch+".tif", Homog);

        Mat output = new Mat();

        Imgproc.warpPerspective(img1, output, homog, new Size(img1.rows(),img1.cols()));
//        displayImageIJ("Ouptut", output);


        /* 6 - Get coordinates of GoodMatches Keypoints */
        ArrayList<Float> img1_keypoints_coordinates = getGoodMatchesCoordinates(keypoints1, good_matchesList);
        ArrayList<Float> img1_keypoints_x = new ArrayList<Float>(img1_keypoints_coordinates.subList(0, good_matchesList.size()));
        ArrayList<Float> img1_keypoints_y = new ArrayList<Float>(img1_keypoints_coordinates.subList(good_matchesList.size(), img1_keypoints_coordinates.size()));
        System.out.println("Img1 x coordinates " + img1_keypoints_x.toString());
        System.out.println("Img1 x size " + img1_keypoints_x.size());
        System.out.println("Img1 y coordinates " + img1_keypoints_y.toString());
        System.out.println("Img1 y size " + img1_keypoints_y.size());

//        ArrayList<Float> img2_keypoints_coordinates = getGoodMatchesCoordinates(keypoints2, good_matchesList);
//
//        System.out.println("Img1 coordinates " + img1_keypoints_coordinates.toString());
//        System.out.println("Img2 coordinates " + img2_keypoints_coordinates.toString());
    }

    public static void main(String[] args) {
        long startTime = new Date().getTime();

        //Load openCv Library, required besides imports
        System.load("/home/nolwenngueguen/Téléchargements/opencv-3.4.0/build/lib/libopencv_java340.so");

        Mat img1 = readImage("/home/nolwenngueguen/Téléchargements/ImagesTest/3-21.tif");
        Mat img2 = readImage("/home/nolwenngueguen/Téléchargements/ImagesTest/4-21.tif");

        getXYmoves(img1, img2);

        long endTime = new Date().getTime();
        long timeElapsed = endTime - startTime;
        System.out.println("Duration in milliseconds : " + timeElapsed);
    }
}
