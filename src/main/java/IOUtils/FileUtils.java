package IOUtils;

import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameConverter;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_core.*;

public final class FileUtils {

   public static IplImage loadImage(File file) throws IOException {
      // Verify file
      if (!file.exists()) {
         throw new FileNotFoundException("Image file does not exist: " + file.getAbsolutePath());
      }
      // Read input image
      IplImage image = cvLoadImage(file.getAbsolutePath());
      if (image == null) {
         throw new IOException("Couldn't load image: " + file.getAbsolutePath());
      }
      return image;
   }

   public static void show(final IplImage image, final String title) {
      final IplImage image1 = cvCreateImage(cvGetSize(image), IPL_DEPTH_8U, image.nChannels());
      cvCopy(image, image1);
      CanvasFrame canvas = new CanvasFrame(title, 1);
      canvas.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
      final OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();

      canvas.showImage(converter.convert(image1));
   }
}