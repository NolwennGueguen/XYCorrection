import IOUtils.IO;
import main.DriftCorrection;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class Correction3dTest {

    @Parameterized.Parameters
    public static Collection<Object[]> prepareFiles(){
        //need this function to load .so of opencv
        nu.pattern.OpenCV.loadShared();
        Mat img1 = null;
        Mat img2 = null;
        String root = System.getProperty("user.dir") + "/src/main/ressources/";
        try {
            img1 = IO.readImage(root + "1-21.tif");
            img2 = IO.readImage(root + "2-21.tif");
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        Integer detectorAlgo = FeatureDetector.BRISK;
        MatOfKeyPoint keypoints1 = DriftCorrection.findKeypoints(img1, detectorAlgo);
        Assert.assertEquals(keypoints1.toList().size(), 3078);
    }

    @Test
    public void assertDescriptors() {
        Integer detectorAlgo = FeatureDetector.BRISK;
        Mat img1_descriptors = DriftCorrection.calculDescriptors(img1,
              DriftCorrection.findKeypoints(img1, detectorAlgo), DescriptorExtractor.BRISK);
        Assert.assertEquals((long)img1_descriptors.get(0,1)[0], (long)122.0);
        Assert.assertEquals((long)img1_descriptors.get(0,10)[0], (long)16.0);
    }

    @Test
    public void assertDescriptorMatching() {
        Integer descriptorMatcher = DescriptorMatcher.BRUTEFORCE;
        Integer detectorAlgo = FeatureDetector.BRISK;
        Mat img1_descriptors = DriftCorrection.calculDescriptors(img1,
              DriftCorrection.findKeypoints(img1, detectorAlgo), DescriptorExtractor.BRISK);
        Mat img2_descriptors = DriftCorrection.calculDescriptors(img2,
              DriftCorrection.findKeypoints(img2, detectorAlgo), DescriptorExtractor.BRISK);
        MatOfDMatch matcher =
              DriftCorrection.matchingDescriptor(img1_descriptors, img2_descriptors, descriptorMatcher);
        Assert.assertEquals((long)matcher.toArray()[0].distance, (long)736.6132);
        Assert.assertEquals((long)matcher.toArray()[10].distance, (long)693.32025);
    }

//    @Test
//    public void assertFiltering(){
//
//    }
}