import main.DriftCalculation;
import main.DriftCorrection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opencv.core.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class DriftCalculationTest {

    @Parameterized.Parameters
    public static Collection<Object[]> prepareFiles(){
        //need this function to load .so of opencv
        nu.pattern.OpenCV.loadShared();
        Mat img1;
        Mat img2 = null;
        String root = System.getProperty("user.dir") + "/src/main/ressources/";
        img1 = DriftCalculation.readImage(root + "1-21.tif");
        img2 = DriftCalculation.readImage(root + "2-21.tif");
        return Arrays.asList(new Object[][] {{img1,img2}});
    }
    @Parameterized.Parameter
    public Mat img1;

    @Parameterized.Parameter(1)
    public Mat img2;

    @Test
    public void assertImagesDims() {
        Assert.assertEquals(img1.cols(), 2560);
        Assert.assertEquals(img1.rows(), 2160);
        Assert.assertEquals(img1.type(), CvType.CV_8UC1);
        Assert.assertEquals(img1.channels(), 1);

        Assert.assertEquals(img2.cols(), 2560);
        Assert.assertEquals(img2.rows(), 2160);
        Assert.assertEquals(img2.type(), CvType.CV_8UC1);
        Assert.assertEquals(img2.channels(), 1);
    }

    @Test
    public void assertImagesFeaturePoints() {
        Integer detectorAlgo = DriftCalculation.DETECTORALGO;
        MatOfKeyPoint keypoints1 = DriftCalculation.findKeypoints(img1, detectorAlgo);
        Assert.assertEquals(keypoints1.toList().size(), 500);
    }

    @Test
    public void assertDescriptors() {
        Integer detectorAlgo = DriftCalculation.DETECTORALGO;
        Mat img1_descriptors = DriftCalculation.calculDescriptors(img1,
                DriftCalculation.findKeypoints(img1, detectorAlgo), DriftCalculation.DESCRIPTOREXTRACTOR);
        Assert.assertEquals((long)img1_descriptors.get(0,1)[0], (long)186);
        Assert.assertEquals((long)img1_descriptors.get(0,10)[0], (long)194);
    }

    @Test
    public void assertDescriptorMatching() {
        Integer descriptorMatcher = DriftCalculation.DESCRIPTORMATCHER;
        Integer detectorAlgo = DriftCalculation.DETECTORALGO;
        Mat img1_descriptors = DriftCalculation.calculDescriptors(img1,
                DriftCalculation.findKeypoints(img1, detectorAlgo), DriftCalculation.DESCRIPTOREXTRACTOR);
        Mat img2_descriptors = DriftCalculation.calculDescriptors(img2,
                DriftCalculation.findKeypoints(img2, detectorAlgo), DriftCalculation.DESCRIPTOREXTRACTOR);
        MatOfDMatch matcher =
                DriftCalculation.matchingDescriptor(img1_descriptors, img2_descriptors, descriptorMatcher);
        Assert.assertEquals((long)matcher.toArray()[0].distance, (long)390.6132);
        Assert.assertEquals((long)matcher.toArray()[10].distance, (long)262.6132);
    }

    @Test
    public void assertGoodMatchesIndex() {
        Integer descriptorMatcher = DriftCalculation.DESCRIPTORMATCHER;
        Integer detectorAlgo = DriftCalculation.DETECTORALGO;
        MatOfKeyPoint keypoint1 = DriftCalculation.findKeypoints(img1, detectorAlgo);
        MatOfKeyPoint keypoint2 = DriftCalculation.findKeypoints(img2, detectorAlgo);
        Mat img1_descriptors = DriftCalculation.calculDescriptors(img1, keypoint1, DriftCalculation.DESCRIPTOREXTRACTOR);
        Mat img2_descriptors = DriftCalculation.calculDescriptors(img2, keypoint2, DriftCalculation.DESCRIPTOREXTRACTOR);
        MatOfDMatch matcher = DriftCalculation.matchingDescriptor(img1_descriptors, img2_descriptors, descriptorMatcher);

        ArrayList<Integer> indexOfGoodMatches = DriftCalculation.getGoodMatchesIndex(matcher,
                keypoint1, keypoint2, DriftCorrection.umPerStep, DriftCorrection.calibration,
                DriftCorrection.intervalInMin);

        Assert.assertEquals(113, indexOfGoodMatches.size());
        Assert.assertEquals((long) 341.40356, indexOfGoodMatches.get(0).longValue());
        Assert.assertEquals((long) 289.40356, indexOfGoodMatches.get(15).longValue());
        Assert.assertEquals((long) 306.40356, indexOfGoodMatches.get(55).longValue());
    }

    @Test
    public void assertGoodMatchesValues() {
        Integer descriptorMatcher = DriftCalculation.DESCRIPTORMATCHER;
        Integer detectorAlgo = DriftCalculation.DETECTORALGO;
        MatOfKeyPoint keypoint1 = DriftCalculation.findKeypoints(img1, detectorAlgo);
        MatOfKeyPoint keypoint2 = DriftCalculation.findKeypoints(img2, detectorAlgo);
        Mat img1_descriptors = DriftCalculation.calculDescriptors(img1, keypoint1, DriftCalculation.DESCRIPTOREXTRACTOR);
        Mat img2_descriptors = DriftCalculation.calculDescriptors(img2, keypoint2, DriftCalculation.DESCRIPTOREXTRACTOR);
        MatOfDMatch matcher = DriftCalculation.matchingDescriptor(img1_descriptors, img2_descriptors, descriptorMatcher);

        ArrayList<DMatch> listOfGoodMatches = DriftCalculation.getGoodMatchesValues(matcher,
                keypoint1, keypoint2, DriftCorrection.umPerStep, DriftCorrection.calibration,
                DriftCorrection.intervalInMin);

        Assert.assertEquals(113, listOfGoodMatches.size());
        Assert.assertEquals((long) 341.40356,(long) listOfGoodMatches.get(0).distance);
        Assert.assertEquals((long) 289.40356,(long) listOfGoodMatches.get(15).distance);
        Assert.assertEquals((long) 306.40356,(long) listOfGoodMatches.get(55).distance);
    }

    @Test
    public void assertGoodMatchesXDistances() {
        Integer descriptorMatcher = DriftCalculation.DESCRIPTORMATCHER;
        Integer detectorAlgo = DriftCalculation.DETECTORALGO;
        MatOfKeyPoint keypoint1 = DriftCalculation.findKeypoints(img1, detectorAlgo);
        MatOfKeyPoint keypoint2 = DriftCalculation.findKeypoints(img2, detectorAlgo);
        Mat img1_descriptors = DriftCalculation.calculDescriptors(img1, keypoint1, DriftCalculation.DESCRIPTOREXTRACTOR);
        Mat img2_descriptors = DriftCalculation.calculDescriptors(img2, keypoint2, DriftCalculation.DESCRIPTOREXTRACTOR);
        MatOfDMatch matcher = DriftCalculation.matchingDescriptor(img1_descriptors, img2_descriptors, descriptorMatcher);

        ArrayList<Integer> indexOfGoodMatches = DriftCalculation.getGoodMatchesIndex(matcher,
                keypoint1, keypoint2, DriftCorrection.umPerStep, DriftCorrection.calibration,
                DriftCorrection.intervalInMin);

        ArrayList<Double> goodMatchesXDistances = DriftCalculation.getGoodMatchesDistances("xDistances",
                indexOfGoodMatches, matcher, keypoint1, keypoint2, DriftCorrection.calibration);
        Assert.assertEquals(113, goodMatchesXDistances.size());
        Assert.assertEquals((long) 341.40356, goodMatchesXDistances.get(0).longValue());
        Assert.assertEquals((long) 289.40356, goodMatchesXDistances.get(15).longValue());
        Assert.assertEquals((long) 306.40356, goodMatchesXDistances.get(55).longValue());
    }

    @Test
    public void assertGoodMatchesYDistances() {
        Integer descriptorMatcher = DriftCalculation.DESCRIPTORMATCHER;
        Integer detectorAlgo = DriftCalculation.DETECTORALGO;
        MatOfKeyPoint keypoint1 = DriftCalculation.findKeypoints(img1, detectorAlgo);
        MatOfKeyPoint keypoint2 = DriftCalculation.findKeypoints(img2, detectorAlgo);
        Mat img1_descriptors = DriftCalculation.calculDescriptors(img1, keypoint1, DriftCalculation.DESCRIPTOREXTRACTOR);
        Mat img2_descriptors = DriftCalculation.calculDescriptors(img2, keypoint2, DriftCalculation.DESCRIPTOREXTRACTOR);
        MatOfDMatch matcher = DriftCalculation.matchingDescriptor(img1_descriptors, img2_descriptors, descriptorMatcher);

        ArrayList<Integer> indexOfGoodMatches = DriftCalculation.getGoodMatchesIndex(matcher,
                keypoint1, keypoint2, DriftCorrection.umPerStep, DriftCorrection.calibration,
                DriftCorrection.intervalInMin);

        ArrayList<Double> goodMatchesYDistances = DriftCalculation.getGoodMatchesDistances("yDistances",
                indexOfGoodMatches, matcher, keypoint1, keypoint2, DriftCorrection.calibration);
        Assert.assertEquals(113, goodMatchesYDistances.size());
        Assert.assertEquals((long) 341.40356, goodMatchesYDistances.get(0).longValue());
        Assert.assertEquals((long) 289.40356, goodMatchesYDistances.get(15).longValue());
        Assert.assertEquals((long) 306.40356, goodMatchesYDistances.get(55).longValue());
    }
}