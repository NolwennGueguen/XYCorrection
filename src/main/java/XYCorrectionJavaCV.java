//
//import java.io.File;
//import java.io.IOException;
//import java.lang.reflect.Array;
//import java.util.ArrayList;
//
//
//import IOUtils.OpenCVUtilsJava;
//import org.bytedeco.javacpp.BytePointer;
//import org.bytedeco.javacpp.indexer.FloatIndexer;
//import org.bytedeco.javacpp.opencv_calib3d;
//import org.bytedeco.javacpp.opencv_calib3d.*;
//import org.bytedeco.javacpp.opencv_core;
//import org.bytedeco.javacpp.opencv_core.*;
//import org.bytedeco.javacpp.opencv_features2d;
//import org.bytedeco.javacpp.opencv_features2d.*;
//import org.bytedeco.javacpp.opencv_xfeatures2d.SURF;
//import org.bytedeco.javacv.Java2DFrameUtils;
//
//
///** The example for section "Matching images using random sample consensus" in Chapter 10, p. 299  (2nd edition)
// * and Chapter 9, page 233 (1st edition).
// *
// * Most of the computations are done by `RobustMatcher` helper class.
// */
//public class XYCorrectionJavaCV {
//
//   Mat img1;
//   Mat img2;
//
//   public XYCorrectionJavaCV(String path2Img1, String path2Img2){
//      img1 = OpenCVUtilsJava.loadOrExit(new File(path2Img1));
//      img2 = OpenCVUtilsJava.loadOrExit(new File(path2Img2));
//      SURF surf = SURF.create();
//
//      KeyPointVector keypoints1 = new KeyPointVector();
//      KeyPointVector keypoints2 = new KeyPointVector();
//      surf.detect(img1, keypoints1, new Mat());
//      surf.detect(img2, keypoints2, new Mat());
//      System.out.println("Number of feature points (1): " + keypoints1.size());
//      System.out.println("Number of feature points (2): " + keypoints2.size());
//
//      Mat descriptors1 = new Mat();
//      Mat descriptors2 = new Mat();
//
//      surf.compute(img1, keypoints1, descriptors1);
//      surf.compute(img2, keypoints2, descriptors2);
//      System.out.println("descriptor matrix size: " + descriptors1.rows() + " by " + descriptors1.cols());
//
//      BFMatcher matcher = new BFMatcher(opencv_core.NORM_L2);
//
//      // vectors of matches
//      DMatchVectorVector matches1 = new DMatchVectorVector();
//      DMatchVectorVector matches2 = new DMatchVectorVector();
//
//      matcher.knnMatch(descriptors1, descriptors2,
//            matches1, // vector of matches (up to 2 per entry)
//            2);
//
//      matcher.knnMatch(descriptors2, descriptors1,
//            matches2, // vector of matches (up to 2 per entry)
//            2); // return 2 nearest neighbours
//
//      System.out.println("Number of matched points 1->2: " + matches1.size());
//
//      System.out.println("Number of matched points 2->1: " + matches2.size());
//
//      DMatchVector r = new DMatchVector();
//      matcher.match(descriptors1, descriptors2, r);
//      System.out.println("Number of matched points 1->2 (after cross-check): " + r.size());
//
//      ArrayList<Point2fVector> arrayP2fVec= toPoint2fVectorPair(r, keypoints1, keypoints2);
//
//      // Compute F matrix using RANSAC
//      Mat pointStatus = new Mat();
//      Mat fundamentalMatrix = opencv_calib3d.findFundamentalMat(
//            toMat(arrayP2fVec.get(0)) /*  points in first image */ ,
//            toMat(arrayP2fVec.get(1)) /*  points in second image */ ,
//            pointStatus /* match status (inlier or outlier) */ ,
//            opencv_calib3d.FM_RANSAC /* RANSAC method */ ,
//            3.0, /* distance to epipolar plane */
//            0.99 /* confidence probability */
//      );
//
////      val (refinedMatches, fundamentalMatrix) = ransacTest(r, keypoints1, keypoints2);
////      System.out.println("Number of matched points (after RANSAC): " + refinedMatches.length());
//
//
////      opencv_features2d.drawMatches(img1, matches.keyPoints1, // 1st image and its keypoints
////            image2, matches.keyPoints2, // 2nd image and its keypoints
////            toDMatchVector(matches.matches), // the matches
////            imageMatches, // the image produced
////            new Scalar(255, 255, 255, 0), // color of the lines
////            new Scalar(255, 255, 255, 0), // color of the keypoints
////            new BytePointer(0),
////            2);
//   }
//
//
////      // Prepare the matcher
////      val rMatcher = new RobustMatcher(SURF.create())
////
////      //
////      // Match two images
////      //
////      val matches = rMatcher.matchImages(image1, image2, RobustMatcher.BothCheck)
////
////      // draw the matches
////      val imageMatches = new Mat()
////      drawMatches(image1, matches.keyPoints1, // 1st image and its keypoints
////      image2, matches.keyPoints2, // 2nd image and its keypoints
////      toDMatchVector(matches.matches), // the matches
////      imageMatches, // the image produced
////      new Scalar(255, 255, 255, 0), // color of the lines
////      new Scalar(255, 255, 255, 0), // color of the keypoints
////      new BytePointer(0),
////      2)
////
////      show(imageMatches, "Matches")
////
////
////      // Draw the epipolar lines
////      val (points1, points2) = toPoint2fVectorPair(toDMatchVector(matches.matches), matches.keyPoints1, matches.keyPoints2)
////
////      val lines1 = new Mat()
////      computeCorrespondEpilines(toMat(points1), 1, matches.fundamentalMatrix, lines1)
////      show(drawEpiLines(image2, lines1, points2), "Left Image Epilines (RANSAC)")
////      val lines2 = new Mat()
////      computeCorrespondEpilines(toMat(points2), 2, matches.fundamentalMatrix, lines2)
////      show(drawEpiLines(image1, lines2, points1), "Right Image Epilines (RANSAC)")
//
//   ArrayList<Point2fVector> toPoint2fVectorPair (DMatchVector match, KeyPointVector keypoints1, KeyPointVector keypoints2){
//      int size = (int) match.size();
//      int[] pointIndexes1 = new int[size];
//      int[] pointIndexes2 = new int[size];
//      for (int i=0; i < size; i++) {
//         pointIndexes1[i]= match.get(i).queryIdx();
//         pointIndexes2[i]= match.get(i).trainIdx();
//      }
//
//      // Convert keypoints into Point2f
//      Point2fVector points1 = new Point2fVector();
//      Point2fVector points2 = new Point2fVector();
//      opencv_core.KeyPoint.convert(keypoints1, points1, pointIndexes1);
//      opencv_core.KeyPoint.convert(keypoints2, points2, pointIndexes2);
//      ArrayList<Point2fVector> res = new ArrayList<>();
//      res.add(points1);
//      res.add(points2);
//      return res;
//
//   }
//
//   Mat toMat(Point2fVector points){
//      // Create Mat representing a vector of Points3f
//      int size = (int) points.size();
//      // Argument to Mat constructor must be `Int` to mean sizes, otherwise it may be interpreted as content.
//      Mat dest = new Mat(1, size, opencv_core.CV_32FC2);
//      Mat indx = FloatIndexer.create(dest.);
//      for (int i =0; i< size ;i ++) {
//         Point2f p = points.get(i);
////         indx.put(0, i, 0, p.x());
////         indx.put(0, i, 1, p.y());
//      }
//      return dest;
//   }
//
//
//   public static void main (String[] args){
//      new XYCorrectionJavaCV("/Volumes/Macintosh/curioData/cell_tracking/1-21.tif",
//            "/Volumes/Macintosh/curioData/cell_tracking/2-21.tif");
//   }
//}
