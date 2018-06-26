package main;

import ij.IJ;
import org.opencv.core.Mat;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

public class DriftCorrection {
    //Set default parameters
    public static final double umPerStep = 0;
    public static final String driftCalcul = "Mean";

    //Global variables
    public static final double calibration = 9.2593;
    public static final double intervalInMin = 2; //120000/60000;
    public static final String savingPath = "/home/dataNolwenn/Résultats/2018-04-11_AF/ImagesTest/";
    public static final String algoToUseMultiple = "AKAZEBRISK";

    public static void main (String[] args) {
        //Load openCv Library, required besides imports
        System.load("/home/nolwenngueguen/Téléchargements/opencv-3.4.0/build/lib/libopencv_java340.so");

        long startTime = new Date().getTime();

        //Load images
//        String imgDir = System.getProperty("user.dir") + "/src/main/ressources";
//        Mat referenceImage = DriftCalculation.readImage(imgDir + "/4-21.tif");
//        Mat targetImage = DriftCalculation.readImage(imgDir + "/6-21.tif");
        Mat referenceImage = DriftCalculation.readImage(savingPath + "Pos1_T1_Z14_Ref.tif");
        Mat targetImage = DriftCalculation.readImage(savingPath + "Pos1_T1_Z14_Target.tif");


        //Define fileName
        String fileName = "Pos1" + driftCalcul + algoToUseMultiple;

        double xCorrection = 0;
        double yCorrection = 0;
        double threshold = 0;

        double[] xyDriftsBRISKORB = new double[13];
        double[] xyDriftsORBORB = new double[13];
        double[] xyDriftsORBBRISK = new double[13];
        double[] xyDriftsBRISKBRISK = new double[13];
        double[] xyDriftsAKAZEBRISK = new double[13];
        double[] xyDriftsAKAZEORB = new double[13];
        double[] xyDriftsAKAZEAKAZE = new double[13];

        List<double[]> listOfDrifts = calculateMultipleXYDrifts(targetImage, referenceImage, FeatureDetector.BRISK, FeatureDetector.ORB,
                FeatureDetector.AKAZE, DescriptorExtractor.BRISK, DescriptorExtractor.ORB,
                DescriptorExtractor.AKAZE, DescriptorMatcher.FLANNBASED);
        if (listOfDrifts.size() < 7) {
            xCorrection = 0;
            yCorrection = 0;
            System.out.println("List of results empty");
        } else {
            xyDriftsBRISKORB = listOfDrifts.get(0);
            xyDriftsORBORB = listOfDrifts.get(1);
            xyDriftsORBBRISK = listOfDrifts.get(2);
            xyDriftsBRISKBRISK = listOfDrifts.get(3);
            xyDriftsAKAZEBRISK = listOfDrifts.get(4);
            xyDriftsAKAZEORB = listOfDrifts.get(5);
            xyDriftsAKAZEAKAZE = listOfDrifts.get(6);

            double[] drifts = new double[13];

            switch (algoToUseMultiple) {
                case "BRISKORB":
                    drifts = xyDriftsBRISKORB;
                    break;
                case "ORBORB":
                    drifts = xyDriftsORBORB;
                    break;
                case "ORBBRISK":
                    drifts = xyDriftsORBBRISK;
                    break;
                case "BRISKBRISK":
                    drifts = xyDriftsBRISKBRISK;
                    break;
                case "AKAZEBRISK":
                    drifts = xyDriftsAKAZEBRISK;
                    break;
                case "AKAZEORB":
                    drifts = xyDriftsAKAZEORB;
                    break;
                case "AKAZEAKAZE":
                    drifts = xyDriftsAKAZEAKAZE;
                    break;
                default:
                    IJ.error("Unknown method of algorithm combination");
            }

            switch (driftCalcul) {
                case "Mean":
                    xCorrection = drifts[0];
                    yCorrection = drifts[1];
                    threshold = 0.05;
                    break;
                case "Median":
                    xCorrection = drifts[5];
                    yCorrection = drifts[6];
                    threshold = 0.05;
                    break;
                case "Minimum Distance":
                    xCorrection = drifts[7];
                    yCorrection = drifts[8];
                    threshold = 0.001;
                    break;
                case "Harmonic Mean":
                    xCorrection = drifts[9];
                    yCorrection = drifts[10];
                    threshold = 0.05;
                    break;
                default:
                    IJ.error("Unknown method of correction");
            }
            System.out.println("Correction applied : " + algoToUseMultiple.toString() + " with " + driftCalcul.toString());
        }

        if (Double.isNaN(xCorrection) || Double.isNaN(yCorrection)) {
            xCorrection = 0;
            yCorrection = 0;
            System.out.println("X or Y Correction NaN");
        } else {
            if (Math.abs(xCorrection) < threshold) {
                xCorrection = 0;
                System.out.println("X Correction < Threshold");
            }
            if (Math.abs(yCorrection) < threshold) {
                yCorrection = 0;
                System.out.println("Y Correction < Threshold");
            }
        }

        System.out.println("X Correction : " + xCorrection);
        System.out.println("Y Correction : " + yCorrection);

        long endTime = new Date().getTime();
        long acquisitionTimeElapsed = endTime - startTime;
        System.out.println("Calculation duration in ms : " + acquisitionTimeElapsed);

        writeMultipleOutput(acquisitionTimeElapsed, fileName, xCorrection, yCorrection,
                xyDriftsBRISKORB, xyDriftsORBORB, xyDriftsORBBRISK, xyDriftsBRISKBRISK,
                xyDriftsAKAZEBRISK, xyDriftsAKAZEORB, xyDriftsAKAZEAKAZE);


    }


    public static List<double[]> calculateMultipleXYDrifts(Mat currentImgMat, Mat imgRef_Mat, Integer detectorAlgo1, Integer detectorAlgo2, Integer detectorAlgo3,
                                                           Integer descriptorExtractor1, Integer descriptorExtractor2, Integer descriptorExtractor3,
                                                           Integer descriptorMatcher){
        int nThread = Runtime.getRuntime().availableProcessors() - 2;
        ExecutorService es = Executors.newFixedThreadPool(nThread);
        Future[] jobs = new Future[7];
        //BRISK-ORB
        jobs[0] = es.submit(new ThreadAttribution(imgRef_Mat, currentImgMat, calibration,
                intervalInMin, umPerStep, detectorAlgo1, descriptorExtractor2, descriptorMatcher));
        //ORB-ORB
        jobs[1] = es.submit(new ThreadAttribution(imgRef_Mat, currentImgMat, calibration,
                intervalInMin, umPerStep, detectorAlgo2, descriptorExtractor2, descriptorMatcher));
        //ORB-BRISK
        jobs[2] = es.submit(new ThreadAttribution(imgRef_Mat, currentImgMat, calibration,
                intervalInMin, umPerStep, detectorAlgo2, descriptorExtractor1, descriptorMatcher));
        //BRISK-BRISK
        jobs[3] = es.submit(new ThreadAttribution(imgRef_Mat, currentImgMat, calibration,
                intervalInMin, umPerStep, detectorAlgo1, descriptorExtractor1, descriptorMatcher));
        //AKAZE-BRISK
        jobs[4] = es.submit(new ThreadAttribution(imgRef_Mat, currentImgMat, calibration,
                intervalInMin, umPerStep, detectorAlgo3, descriptorExtractor1, descriptorMatcher));
        //AKAZE-ORB
        jobs[5] = es.submit(new ThreadAttribution(imgRef_Mat, currentImgMat, calibration,
                intervalInMin, umPerStep, detectorAlgo3, descriptorExtractor2, descriptorMatcher));
        //AKAZE-AKAZE
        jobs[6] = es.submit(new ThreadAttribution(imgRef_Mat, currentImgMat, calibration,
                intervalInMin, umPerStep, detectorAlgo3, descriptorExtractor3, descriptorMatcher));

        List<double[]> drifts = new ArrayList<>();
        double[] currentRes = null;
        int algoIndex = -1;
        try {
            for (int i = 0; i < jobs.length; i++) {
                currentRes = (double[]) jobs[i].get();
                algoIndex = i;
                drifts.add(i, currentRes);
            }
        } catch (InterruptedException | ExecutionException e) {
            try {
                for (double d : currentRes){
                    System.out.println("Error in algo " + algoIndex + "_" + d);
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        es.shutdown();
        try{
            es.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return drifts;
    }

    public static void writeMultipleOutput(long acquisitionDuration, String fileName, double xCorrection, double yCorrection, double[] xyDriftsBRISKORB,
                                           double[] xyDriftsORBORB, double[] xyDriftsORBBRISK, double[] xyDriftsBRISKBRISK,
                                           double[] xyDriftsAKAZEAKAZE, double[] xyDriftsAKAZEBRISK, double[] xyDriftsAKAZEORB) {

        File f1 = new File(savingPath + fileName + "_Stats" + ".csv");

        String[] headersOfFile = new String[]{"xCorrection", "yCorrection", "calculationDuration(ms)",
                "meanXdisplacementBRISKORB", "meanYdisplacementBRISKORB", "meanXdisplacementORBORB", "meanYdisplacementORBORB",
                "meanXdisplacementORBBRISK", "meanYdisplacementORBBRISK", "meanXdisplacementBRISKBRISK", "meanYdisplacementBRISKBRISK",
                "meanXdisplacementAKAZEBRISK", "meanYdisplacementAKAZEBRISK", "meanXdisplacementAKAZEORB", "meanYdisplacementAKAZEORB",
                "meanXdisplacementAKAZEAKAZE", "meanYdisplacementAKAZEAKAZE",

                "numberOfMatchesBRISKORB", "numberOfMatchesORBORB", "numberOfMatchesORBBRISK", "numberOfMatchesBRISKBRISK",
                "numberOfMatchesAKAZEBRISK", "numberOfMatchesAKAZEORB", "numberOfMatchesAKAZEAKAZE",

                "numberOfGoodMatchesBRISKORB", "numberOfGoodMatchesORBORB", "numberOfGoodMatchesORBBRISK",
                "numberOfGoodMatchesBRISKBRISK", "numberOfGoodMatchesAKAZEBRISK", "numberOfGoodMatchesAKAZEORB",
                "numberOfGoodMatchesAKAZEAKAZE",

                "algorithmDurationBRISKORB(ms)", "algorithmDurationORBORB(ms)", "algorithmDurationORBBRISK(ms)",
                "algorithmDurationBRISKBRISK(ms)", "algorithmDurationAKAZEBRISK(ms)", "algorithmDurationAKAZEORB(ms)",
                "algorithmDurationAKAZEAKAZE(ms)",

                "medianXdisplacementBRISKORB", "medianYdisplacementBRISKORB", "medianXdisplacementORBORB", "medianYdisplacementORBORB",
                "medianXdisplacementORBBRISK", "medianYdisplacementORBBRISK", "medianXdisplacementBRISKBRISK", "medianYdisplacementBRISKBRISK",
                "medianXdisplacementAKAZEBRISK", "medianYdisplacementAKAZEBRISK", "medianXdisplacementAKAZEORB", "medianYdisplacementAKAZEORB",
                "medianXdisplacementAKAZEAKAZE", "medianYdisplacementAKAZEAKAZE",


                "minXdisplacementBRISKORB", "minYdisplacementBRISKORB", "minXdisplacementORBORB", "minYdisplacementORBORB",
                "minXdisplacementORBBRISK", "minYdisplacementORBBRISK", "minXdisplacementBRISKBRISK", "minYdisplacementBRISKBRISK",
                "minXdisplacementAKAZEBRISK", "minYdisplacementAKAZEBRISK", "minXdisplacementAKAZEORB", "minYdisplacementAKAZEORB",
                "minXdisplacementAKAZEAKAZE", "minYdisplacementAKAZEAKAZE",

                "harmonicMeanXdisplacementBRISKORB", "harmonicMeanYdisplacementBRISKORB", "harmonicMeanXdisplacementORBORB", "harmonicMeanYdisplacementORBORB",
                "harmonicMeanXdisplacementORBBRISK", "harmonicMeanYdisplacementORBBRISK", "harmonicMeanXdisplacementBRISKBRISK", "harmonicMeanYdisplacementBRISKBRISK",
                "harmonicMeanXdisplacementAKAZEBRISK", "harmonicMeanYdisplacementAKAZEBRISK", "harmonicMeanXdisplacementAKAZEORB", "harmonicMeanYdisplacementAKAZEORB",
                "harmonicMeanXdisplacementAKAZEAKAZE", "harmonicMeanYdisplacementAKAZEAKAZE"

        };

        double meanXdisplacementBRISKORB = xyDriftsBRISKORB[0];
        double meanYdisplacementBRISKORB = xyDriftsBRISKORB[1];
        double numberOfMatchesBRISKORB = xyDriftsBRISKORB[2];
        double numberOfGoodMatchesBRISKORB = xyDriftsBRISKORB[3];
        double algorithmDurationBRISKORB = xyDriftsBRISKORB[4];
        double medianXDisplacementBRISKORB = xyDriftsBRISKORB[5];
        double medianYDisplacementBRISKORB = xyDriftsBRISKORB[6];
        double minXDisplacementBRISKORB = xyDriftsBRISKORB[7];
        double minYDisplacementBRISKORB = xyDriftsBRISKORB[8];
        double harmonicMeanXDisplacementBRISKORB = xyDriftsBRISKORB[9];
        double harmonicMeanYDisplacementBRISKORB = xyDriftsBRISKORB[10];

        double meanXdisplacementORBORB = xyDriftsORBORB[0];
        double meanYdisplacementORBORB = xyDriftsORBORB[1];
        double numberOfMatchesORBORB = xyDriftsORBORB[2];
        double numberOfGoodMatchesORBORB = xyDriftsORBORB[3];
        double algorithmDurationORBORB = xyDriftsORBORB[4];
        double medianXDisplacementORBORB = xyDriftsORBORB[5];
        double medianYDisplacementORBORB = xyDriftsORBORB[6];
        double minXDisplacementORBORB = xyDriftsORBORB[7];
        double minYDisplacementORBORB = xyDriftsORBORB[8];
        double harmonicMeanXDisplacementORBORB = xyDriftsORBORB[9];
        double harmonicMeanYDisplacementORBORB = xyDriftsORBORB[10];

        double meanXdisplacementORBBRISK = xyDriftsORBBRISK[0];
        double meanYdisplacementORBBRISK = xyDriftsORBBRISK[1];
        double numberOfMatchesORBBRISK = xyDriftsORBBRISK[2];
        double numberOfGoodMatchesORBBRISK = xyDriftsORBBRISK[3];
        double algorithmDurationORBBRISK = xyDriftsORBBRISK[4];
        double medianXDisplacementORBBRISK = xyDriftsORBBRISK[5];
        double medianYDisplacementORBBRISK = xyDriftsORBBRISK[6];
        double minXDisplacementORBBRISK = xyDriftsORBBRISK[7];
        double minYDisplacementORBBRISK = xyDriftsORBBRISK[8];
        double harmonicMeanXDisplacementORBBRISK = xyDriftsORBBRISK[9];
        double harmonicMeanYDisplacementORBBRISK = xyDriftsORBBRISK[10];

        double meanXdisplacementBRISKBRISK = xyDriftsBRISKBRISK[0];
        double meanYdisplacementBRISKBRISK = xyDriftsBRISKBRISK[1];
        double numberOfMatchesBRISKBRISK = xyDriftsBRISKBRISK[2];
        double numberOfGoodMatchesBRISKBRISK = xyDriftsBRISKBRISK[3];
        double algorithmDurationBRISKBRISK = xyDriftsBRISKBRISK[4];
        double medianXDisplacementBRISKBRISK = xyDriftsBRISKBRISK[5];
        double medianYDisplacementBRISKBRISK = xyDriftsBRISKBRISK[6];
        double minXDisplacementBRISKBRISK = xyDriftsBRISKBRISK[7];
        double minYDisplacementBRISKBRISK = xyDriftsBRISKBRISK[8];
        double harmonicMeanXDisplacementBRISKBRISK = xyDriftsBRISKBRISK[9];
        double harmonicMeanYDisplacementBRISKBRISK = xyDriftsBRISKBRISK[10];

        double meanXdisplacementAKAZEBRISK = xyDriftsAKAZEBRISK[0];
        double meanYdisplacementAKAZEBRISK = xyDriftsAKAZEBRISK[1];
        double numberOfMatchesAKAZEBRISK = xyDriftsAKAZEBRISK[2];
        double numberOfGoodMatchesAKAZEBRISK = xyDriftsAKAZEBRISK[3];
        double algorithmDurationAKAZEBRISK = xyDriftsAKAZEBRISK[4];
        double medianXDisplacementAKAZEBRISK = xyDriftsAKAZEBRISK[5];
        double medianYDisplacementAKAZEBRISK = xyDriftsAKAZEBRISK[6];
        double minXDisplacementAKAZEBRISK = xyDriftsAKAZEBRISK[7];
        double minYDisplacementAKAZEBRISK = xyDriftsAKAZEBRISK[8];
        double harmonicMeanXDisplacementAKAZEBRISK = xyDriftsAKAZEBRISK[9];
        double harmonicMeanYDisplacementAKAZEBRISK = xyDriftsAKAZEBRISK[10];

        double meanXdisplacementAKAZEORB = xyDriftsAKAZEORB[0];
        double meanYdisplacementAKAZEORB = xyDriftsAKAZEORB[1];
        double numberOfMatchesAKAZEORB = xyDriftsAKAZEORB[2];
        double numberOfGoodMatchesAKAZEORB = xyDriftsAKAZEORB[3];
        double algorithmDurationAKAZEORB = xyDriftsAKAZEORB[4];
        double medianXDisplacementAKAZEORB = xyDriftsAKAZEORB[5];
        double medianYDisplacementAKAZEORB = xyDriftsAKAZEORB[6];
        double minXDisplacementAKAZEORB = xyDriftsAKAZEORB[7];
        double minYDisplacementAKAZEORB = xyDriftsAKAZEORB[8];
        double harmonicMeanXDisplacementAKAZEORB = xyDriftsAKAZEORB[9];
        double harmonicMeanYDisplacementAKAZEORB = xyDriftsAKAZEORB[10];

        double meanXdisplacementAKAZEAKAZE = xyDriftsAKAZEAKAZE[0];
        double meanYdisplacementAKAZEAKAZE = xyDriftsAKAZEAKAZE[1];
        double numberOfMatchesAKAZEAKAZE = xyDriftsAKAZEAKAZE[2];
        double numberOfGoodMatchesAKAZEAKAZE = xyDriftsAKAZEAKAZE[3];
        double algorithmDurationAKAZEAKAZE = xyDriftsAKAZEAKAZE[4];
        double medianXDisplacementAKAZEAKAZE = xyDriftsAKAZEAKAZE[5];
        double medianYDisplacementAKAZEAKAZE = xyDriftsAKAZEAKAZE[6];
        double minXDisplacementAKAZEAKAZE = xyDriftsAKAZEAKAZE[7];
        double minYDisplacementAKAZEAKAZE = xyDriftsAKAZEAKAZE[8];
        double harmonicMeanXDisplacementAKAZEAKAZE = xyDriftsAKAZEAKAZE[9];
        double harmonicMeanYDisplacementAKAZEAKAZE = xyDriftsAKAZEAKAZE[10];

        FileWriter fw;
        if (!f1.exists()) {
            try {
                f1.createNewFile();
                fw = new FileWriter(f1, true);
                fw.write(String.join(",", headersOfFile) + System.lineSeparator());
                fw.write(xCorrection + "," + yCorrection + "," + acquisitionDuration + ","

                        + meanXdisplacementBRISKORB + "," + meanYdisplacementBRISKORB + "," + meanXdisplacementORBORB + "," + meanYdisplacementORBORB + ","
                        + meanXdisplacementORBBRISK + "," + meanYdisplacementORBBRISK + "," + meanXdisplacementBRISKBRISK + "," + meanYdisplacementBRISKBRISK + ","
                        + meanXdisplacementAKAZEBRISK + "," + meanYdisplacementAKAZEBRISK + "," + meanXdisplacementAKAZEORB + "," + meanYdisplacementAKAZEORB + ","
                        + meanXdisplacementAKAZEAKAZE + "," + meanYdisplacementAKAZEAKAZE + ","

                        + numberOfMatchesBRISKORB + "," + numberOfMatchesORBORB + "," + numberOfMatchesORBBRISK + "," + numberOfMatchesBRISKBRISK + ","
                        + numberOfMatchesAKAZEBRISK + "," + numberOfMatchesAKAZEORB + "," + numberOfMatchesAKAZEAKAZE + ","

                        + numberOfGoodMatchesBRISKORB + "," + numberOfGoodMatchesORBORB + "," + numberOfGoodMatchesORBBRISK + ","
                        + numberOfGoodMatchesBRISKBRISK + "," + numberOfGoodMatchesAKAZEBRISK + "," + numberOfGoodMatchesAKAZEORB + ","
                        + numberOfGoodMatchesAKAZEAKAZE + ","

                        + algorithmDurationBRISKORB + "," + algorithmDurationORBORB + "," + algorithmDurationORBBRISK + ","
                        + algorithmDurationBRISKBRISK + "," + algorithmDurationAKAZEBRISK + "," + algorithmDurationAKAZEORB + ","
                        + algorithmDurationAKAZEAKAZE + ","

                        + medianXDisplacementBRISKORB + "," + medianYDisplacementBRISKORB + "," + medianXDisplacementORBORB + "," + medianYDisplacementORBORB + ","
                        + medianXDisplacementORBBRISK + "," + medianYDisplacementORBBRISK + "," + medianXDisplacementBRISKBRISK + "," + medianYDisplacementBRISKBRISK + ","
                        + medianXDisplacementAKAZEBRISK + "," + medianYDisplacementAKAZEBRISK + "," + medianXDisplacementAKAZEORB + "," + medianYDisplacementAKAZEORB + ","
                        + medianXDisplacementAKAZEAKAZE + "," + medianYDisplacementAKAZEAKAZE + ","

                        + minXDisplacementBRISKORB + "," + minYDisplacementBRISKORB + "," + minXDisplacementORBORB + "," + minYDisplacementORBORB + ","
                        + minXDisplacementORBBRISK + "," + minYDisplacementORBBRISK + "," + minXDisplacementBRISKBRISK + "," + minYDisplacementBRISKBRISK + ","
                        + minXDisplacementAKAZEBRISK + "," + minYDisplacementAKAZEBRISK + "," + minXDisplacementAKAZEORB + "," + minYDisplacementAKAZEORB + ","
                        + minXDisplacementAKAZEAKAZE + "," + minYDisplacementAKAZEAKAZE + ","

                        + harmonicMeanXDisplacementBRISKORB + "," + harmonicMeanYDisplacementBRISKORB + "," + harmonicMeanXDisplacementORBORB + "," + harmonicMeanYDisplacementORBORB + ","
                        + harmonicMeanXDisplacementORBBRISK + "," + harmonicMeanYDisplacementORBBRISK + "," + harmonicMeanXDisplacementBRISKBRISK + "," + harmonicMeanYDisplacementBRISKBRISK + ","
                        + harmonicMeanXDisplacementAKAZEBRISK + "," + harmonicMeanYDisplacementAKAZEBRISK + "," + harmonicMeanXDisplacementAKAZEORB + "," + harmonicMeanYDisplacementAKAZEORB + ","
                        + harmonicMeanXDisplacementAKAZEAKAZE + "," + harmonicMeanYDisplacementAKAZEAKAZE

                        + System.lineSeparator());
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                FileWriter fw1 = new FileWriter(f1, true);
                fw1.write(xCorrection + "," + yCorrection + "," + acquisitionDuration + ","

                        + meanXdisplacementBRISKORB + "," + meanYdisplacementBRISKORB + "," + meanXdisplacementORBORB + "," + meanYdisplacementORBORB + ","
                        + meanXdisplacementORBBRISK + "," + meanYdisplacementORBBRISK + "," + meanXdisplacementBRISKBRISK + "," + meanYdisplacementBRISKBRISK + ","
                        + meanXdisplacementAKAZEBRISK + "," + meanYdisplacementAKAZEBRISK + "," + meanXdisplacementAKAZEORB + "," + meanYdisplacementAKAZEORB + ","
                        + meanXdisplacementAKAZEAKAZE + "," + meanYdisplacementAKAZEAKAZE + ","

                        + numberOfMatchesBRISKORB + "," + numberOfMatchesORBORB + "," + numberOfMatchesORBBRISK + "," + numberOfMatchesBRISKBRISK + ","
                        + numberOfMatchesAKAZEBRISK + "," + numberOfMatchesAKAZEORB + "," + numberOfMatchesAKAZEAKAZE + ","

                        + numberOfGoodMatchesBRISKORB + "," + numberOfGoodMatchesORBORB + "," + numberOfGoodMatchesORBBRISK + ","
                        + numberOfGoodMatchesBRISKBRISK + "," + numberOfGoodMatchesAKAZEBRISK + "," + numberOfGoodMatchesAKAZEORB + ","
                        + numberOfGoodMatchesAKAZEAKAZE + ","

                        + algorithmDurationBRISKORB + "," + algorithmDurationORBORB + "," + algorithmDurationORBBRISK + ","
                        + algorithmDurationBRISKBRISK + "," + algorithmDurationAKAZEBRISK + "," + algorithmDurationAKAZEORB + ","
                        + algorithmDurationAKAZEAKAZE + ","

                        + medianXDisplacementBRISKORB + "," + medianYDisplacementBRISKORB + "," + medianXDisplacementORBORB + "," + medianYDisplacementORBORB + ","
                        + medianXDisplacementORBBRISK + "," + medianYDisplacementORBBRISK + "," + medianXDisplacementBRISKBRISK + "," + medianYDisplacementBRISKBRISK + ","
                        + medianXDisplacementAKAZEBRISK + "," + medianYDisplacementAKAZEBRISK + "," + medianXDisplacementAKAZEORB + "," + medianYDisplacementAKAZEORB + ","
                        + medianXDisplacementAKAZEAKAZE + "," + medianYDisplacementAKAZEAKAZE + ","

                        + minXDisplacementBRISKORB + "," + minYDisplacementBRISKORB + "," + minXDisplacementORBORB + "," + minYDisplacementORBORB + ","
                        + minXDisplacementORBBRISK + "," + minYDisplacementORBBRISK + "," + minXDisplacementBRISKBRISK + "," + minYDisplacementBRISKBRISK + ","
                        + minXDisplacementAKAZEBRISK + "," + minYDisplacementAKAZEBRISK + "," + minXDisplacementAKAZEORB + "," + minYDisplacementAKAZEORB + ","
                        + minXDisplacementAKAZEAKAZE + "," + minYDisplacementAKAZEAKAZE + ","

                        + harmonicMeanXDisplacementBRISKORB + "," + harmonicMeanYDisplacementBRISKORB + "," + harmonicMeanXDisplacementORBORB + "," + harmonicMeanYDisplacementORBORB + ","
                        + harmonicMeanXDisplacementORBBRISK + "," + harmonicMeanYDisplacementORBBRISK + "," + harmonicMeanXDisplacementBRISKBRISK + "," + harmonicMeanYDisplacementBRISKBRISK + ","
                        + harmonicMeanXDisplacementAKAZEBRISK + "," + harmonicMeanYDisplacementAKAZEBRISK + "," + harmonicMeanXDisplacementAKAZEORB + "," + harmonicMeanYDisplacementAKAZEORB + ","
                        + harmonicMeanXDisplacementAKAZEAKAZE + "," + harmonicMeanYDisplacementAKAZEAKAZE

                        + System.lineSeparator());
                fw1.close();
            } catch(IOException e){
                e.printStackTrace();
                System.out.println("Unable to add lines to file");
            }
        }
    }

    //********************************************************************************//
    //*************************** Class for multithreading ***************************//
    //********************************************************************************//
    public static class ThreadAttribution implements Callable<double[]> {

        public Mat img1_;
        public Mat img2_;
        public double calibration_;
        public double intervalInMs_;
        public double umPerStep_;
        public Integer detectorAlgo_;
        public Integer descriptorExtractor_;
        public Integer descriptorMatcher_;

        ThreadAttribution(Mat img1, Mat img2, double calibration, double intervalInMs, double umPerStep,
                          Integer detectorAlgo, Integer descriptorExtractor, Integer descriptorMatcher) {
            img1_ = img1;
            img2_ = img2;
            calibration_ = calibration;
            intervalInMs_ = intervalInMs;
            umPerStep_ = umPerStep;
            detectorAlgo_ = detectorAlgo;
            descriptorExtractor_ = descriptorExtractor;
            descriptorMatcher_ = descriptorMatcher;
        }

        @Override
        public double[] call() throws Exception {
            return DriftCalculation.driftCorrection(img1_, img2_, calibration_, intervalInMs_,
                    umPerStep_, detectorAlgo_, descriptorExtractor_, descriptorMatcher_);
        }
    }
}

