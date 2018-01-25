import ij.IJ;
import ij.ImagePlus;
import ijopencv.ij.ImagePlusMatConverter;
import org.bytedeco.javacpp.opencv_core;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class IJCVCorrectionTest {
   @Parameterized.Parameters
   public static Collection<Object[]> prepareFiles(){
      ImagePlus img1 = IJ.openImage(System.getProperty("user.dir") + "/src/main/ressources/ratBrain-5-S21.tif");
      ImagePlus img2 = IJ.openImage(System.getProperty("user.dir") + "/src/main/ressources/ratBrain-6-S21.tif");
      return Arrays.asList(new Object[][] {{img1,img2}});
   }
   @Parameterized.Parameter
   public ImagePlus img1;

   @Parameterized.Parameter(1)
   public ImagePlus img2;

   @Test
   public void show(){
      System.out.println(ImagePlusMatConverter.toMat(img1.getProcessor().convertToFloatProcessor()));
   }
}
