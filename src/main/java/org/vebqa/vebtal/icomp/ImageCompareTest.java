package org.vebqa.vebtal.icomp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.ORB;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Erstellt einen Testfall zum Vergleichen zweier Images.
 * 
 * @author kdoerges
 */
public class ImageCompareTest {

	private final static Logger logger = LoggerFactory.getLogger(ImageCompareTest.class);

	/**
	 * Referenz-Image
	 */
	private String sourceFile;

	/**
	 * aktuelles Vergleich-Image
	 */
	private String currentFile;

	private int distance;

	private String opencv_path;

	private String DIFFERENCEFILENAME = "_difference.jpg";

	/**
	 * Constructor
	 */
	public ImageCompareTest() {
		// nothing to do..
	}

	/**
	 * Constructor
	 * 
	 * @param aSourceFile
	 * @param aTargetFile
	 */
	public ImageCompareTest(String aSourceFile, String aTargetFile) {
		sourceFile = aSourceFile;
		currentFile = aTargetFile;

		opencv_path = System.getProperty("OPENCV_DIR");
		distance = 0;
	}

	/**
	 * Fuehrt den Testfall aus.
	 */
	public ImageCompareResult execute() throws Exception {
		// C Erweiterung laden, Plattform abhaengig!
		ImageCompareResult result = new ImageCompareResult();

		try {
			System.load(this.opencv_path + "\\" + Core.NATIVE_LIBRARY_NAME + ".dll");
		} catch (UnsatisfiedLinkError e) {
			String tError = "Native code library failed to load!";
			logger.error(tError, e);
			throw e;
		}

		Mat reference = new Mat();
		Mat compare = new Mat();
		final Mat resultImage = new Mat();

		// check, if reference file is existing
		File tReference = new File(sourceFile);
		if (!tReference.exists()) {
			return result;
		}

		// check, if compare file is existing
		File tCurrent = new File(currentFile);
		if (!tCurrent.exists()) {
			return result;
		}

		reference = Imgcodecs.imread(sourceFile, Imgcodecs.IMREAD_COLOR);
		compare = Imgcodecs.imread(currentFile, Imgcodecs.IMREAD_COLOR);
		compare.copyTo(resultImage);

		MatOfKeyPoint keypointsSource = new MatOfKeyPoint();
		MatOfKeyPoint keypointsCompare = new MatOfKeyPoint();

		Mat descriptorSource = new Mat();
		Mat descriptorCompare = new Mat();

		// FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
		ORB detector = ORB.create();

		// detect keypoints
		detector.detect(reference, keypointsSource);
		detector.detect(compare, keypointsCompare);

		// extract descriptors
		ORB extractor;
		extractor = ORB.create();
		extractor.compute(reference, keypointsSource, descriptorSource);
		extractor.compute(compare, keypointsCompare, descriptorCompare);

		// Definition of descriptor matcher
		DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

		// Match points of two images
		MatOfDMatch matches = new MatOfDMatch();
		matcher.match(descriptorSource, descriptorCompare, matches);

		// New method of finding best matches
		List<DMatch> matchesList = matches.toList();
		List<DMatch> matchesFinal = new ArrayList<DMatch>();

		MatOfDMatch errorMat = new MatOfDMatch();

		Integer tDistance = this.distance;

		for (int i = 0; i < matchesList.size(); i++) {
			if (matchesList.get(i).distance > tDistance) {
				matchesFinal.add(matches.toList().get(i));
			}
		}

		errorMat.fromList(matchesFinal);

		// Differenzmenge merken
		int tDifferenceCount = matchesFinal.size();
		logger.info(tDifferenceCount + " differences found.");
		ImageCompareResult tResult = new ImageCompareResult();

		// Erzeuge die Differenz von Referenz zu Vergleichststand und speichere
		// in Destination
		Mat destination = new Mat();

		if (reference.cols() == compare.cols()) {
			Core.absdiff(reference, compare, destination);
			// Core.subtract(reference, compare, destination);
		} else {
			logger.warn("Image dimensions differ! Cannot create difference file!");
			return result;
		}

		// Suche die Konturen der Differenzen
		Mat destContour = new Mat();
		destContour = destination.clone();

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();

		Imgproc.findContours(destContour, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

		if (!destContour.empty() && !matchesFinal.isEmpty()) {
			Imgproc.cvtColor(resultImage, resultImage, Imgproc.COLOR_GRAY2BGR);

			// Schreibe die Konturen in den geklonten Ist-Stand zurueck
			// Imgproc.drawContours(resultImage, contours, -1, new Scalar(0, 0, 255), 1);
			MatOfPoint2f approxCurve = new MatOfPoint2f();
			for (int i = 0; i < contours.size(); i++) {
				// Convert contours(i) from MatOfPoint to MatOfPoint2f
				MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(i).toArray());
				// Processing on mMOP2f1 which is in type MatOfPoint2f
				double approxDistance = Imgproc.arcLength(contour2f, true) * 0.02;
				Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);

				// Convert back to MatOfPoint
				MatOfPoint points = new MatOfPoint(approxCurve.toArray());

				// Get bounding rect of contour
				Rect rect = Imgproc.boundingRect(points);

				// draw enclosing rectangle (all same color, but you could use variable i to
				// make them unique)
				Imgproc.rectangle(resultImage, new Point(rect.x, rect.y),
						new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255, 255), 2);
			}

			String fnDifference = null;

			Imgcodecs.imwrite(fnDifference, resultImage);
			tResult.setResultFile(DIFFERENCEFILENAME);
			tResult.setResultType(ImageCompareResult.IMAGESDIFFER);
			tResult.setDifferences(tDifferenceCount);
			tResult.setMessage("Images have differences..." + tDifferenceCount);
			logger.info(tDifferenceCount + " differences found an written to " + fnDifference);
		} else {
			tResult.setResultType(ImageCompareResult.CHECKOK);
			tResult.setMessage("No differences found.");
		}
		return result;
	}
}
