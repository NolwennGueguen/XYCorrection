import org.bytedeco.javacpp.opencv_core;
import org.junit.Assert;
import org.junit.Test;
import IOUtils.FileUtils;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class XYCorrectionTest {

    @Parameterized.Parameters
    public static Collection<Object[]> prepareFiles(){
        opencv_core.IplImage img1 = null;
        opencv_core.IplImage img2 = null;
        try {
            img1 = FileUtils.loadImage(new File(System.getProperty("user.dir") + "/src/main/ressources/ratBrain-5-S21.tif"));
            img2 = FileUtils.loadImage(new File(System.getProperty("user.dir") + "/src/main/ressources/ratBrain-6-S21.tif"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Arrays.asList(new Object[][] {{img1,img2}});
    }
    @Parameterized.Parameter
    public opencv_core.IplImage img1;

    @Parameterized.Parameter(1)
    public opencv_core.IplImage img2;

    @Test
    public void assertImagesDims() {
        Assert.assertEquals(opencv_core.cvGetSize(img1).width(), 170);
        Assert.assertEquals(opencv_core.cvGetSize(img1).height(), 170);
        Assert.assertEquals(opencv_core.cvGetSize(img2).width(), 170);
        Assert.assertEquals(opencv_core.cvGetSize(img2).height(), 170);
    }

    @Test
    public void assertImagesFeaturePoints() {
        Assert.fail();
    }

    @Test
    public void assertHomographyMatrix() {
        Assert.fail();
    }

    @Test
    public void assertWrapPerspectives() {
        Assert.fail();
    }
}