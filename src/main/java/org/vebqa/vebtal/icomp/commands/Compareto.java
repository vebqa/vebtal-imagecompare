package org.vebqa.vebtal.icomp.commands;

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
import org.vebqa.vebtal.annotations.Keyword;
import org.vebqa.vebtal.command.AbstractCommand;
import org.vebqa.vebtal.icomp.ImageDriver;
import org.vebqa.vebtal.icomprestserver.IcompTestAdaptionPlugin;
import org.vebqa.vebtal.model.CommandType;
import org.vebqa.vebtal.model.Response;

@Keyword(module = IcompTestAdaptionPlugin.ID, command = "CompareTo", hintTarget = "path/to/current.png", hintValue = "path/to/difference.png")
public class Compareto extends AbstractCommand {

	private static final Logger logger = LoggerFactory.getLogger(Compareto.class);
	
	public Compareto(String aCommand, String aTarget, String aValue) {
		super(aCommand, aTarget, aValue);
		this.type = CommandType.ASSERTION;
	}

	@Override
	public Response executeImpl(Object driver) {
		ImageDriver imgDriver = (ImageDriver)driver;
		
		String aReferenceImg = this.target;
		String aDifferenceImg = this.value;
		
		Response tResp = new Response();
		
		if (!imgDriver.isInitialized()) {
			tResp.setCode(Response.FAILED);
			tResp.setMessage("OpenCV not initialized. Libraries not loaded yet.");
			return tResp;
		}

		if (!imgDriver.isLoaded()) {
			tResp.setCode(Response.FAILED);
			tResp.setMessage("No opened file.");
			return tResp;
		}
		
		// check, if a current file is existing
		File tReference = new File(aReferenceImg);
		if (!tReference.exists()) {
			tResp.setCode(Response.FAILED);
			tResp.setMessage("Cannot find reference image!");
			return tResp;
		}
		
		Mat reference = Imgcodecs.imread(aReferenceImg, Imgcodecs.IMREAD_COLOR);

		Mat resultImage = new Mat();
		imgDriver.getCurrent().copyTo(resultImage);

		MatOfKeyPoint keypointsRef = new MatOfKeyPoint();
		MatOfKeyPoint keypointsCurrent = new MatOfKeyPoint();

		Mat descriptorRef = new Mat();
		Mat descriptorCurrent = new Mat();

		Imgproc.cvtColor(reference, reference, Imgproc.COLOR_RGB2GRAY);
		Imgproc.cvtColor(imgDriver.getCurrent(), imgDriver.getCurrent(), Imgproc.COLOR_RGB2GRAY);
		Imgproc.cvtColor(resultImage, resultImage, Imgproc.COLOR_RGB2GRAY);

		// detect keypoints
		ORB detector = ORB.create();
		// detector = FeatureDetector.create(FeatureDetector.ORB);
		detector.detect(reference, keypointsRef);
		detector.detect(imgDriver.getCurrent(), keypointsCurrent);

		// extract descriptors
		ORB descriptor = ORB.create();
		// descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
		descriptor.compute(reference, keypointsRef, descriptorRef);
		descriptor.compute(imgDriver.getCurrent(), keypointsCurrent, descriptorCurrent);

		// Definition of descriptor matcher
		DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

		System.out.println(descriptorRef.size() + " | " + descriptorCurrent.size());

		// Match points of two images
		MatOfDMatch matches = new MatOfDMatch();
		matcher.match(descriptorRef, descriptorCurrent, matches);

		// New method of finding best matches
		List<DMatch> matchesList = matches.toList();
		List<DMatch> matchesFinal = new ArrayList<DMatch>();

		MatOfDMatch errorMat = new MatOfDMatch();

		Double tDistance = 1.0;

		for (int i = 0; i < matchesList.size(); i++) {
			if (matchesList.get(i).distance > tDistance) {
				matchesFinal.add(matches.toList().get(i));
			}
		}

		errorMat.fromList(matchesFinal);

		// Differenzmenge merken
		int tDifferenceCount = matchesFinal.size();
		logger.debug(tDifferenceCount + " differences found.");

		// Erzeuge die Differenz von Referenz zu Vergleichststand und speichere
		// in Destination
		Mat destination = new Mat();

		if (reference.cols() == imgDriver.getCurrent().cols()) {
			Core.absdiff(reference, imgDriver.getCurrent(), destination);
			// Core.subtract(reference, compare, destination);
		} else {
			logger.warn("Image dimensions differ! Cannot create difference file!");
			tResp.setCode(Response.FAILED);
			tResp.setMessage("Image dimensions differ!");
			return tResp;
		}

		// Suche die Konturen der Differenzen
		Mat destContour = new Mat();
		destContour = destination.clone();
		// destContour.convertTo(destContour, CvType.CV_32SC1);

		List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
		Mat hierarchy = new Mat();

		Imgproc.findContours(destContour, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);

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

			Imgcodecs.imwrite(aDifferenceImg, resultImage);
			tResp.setCode(Response.FAILED);
			tResp.setMessage(tDifferenceCount + " differences found. Diff-File: " + aDifferenceImg);
			logger.info(tDifferenceCount + " differences found and written to " + aDifferenceImg);
		} else {
			tResp.setCode(Response.PASSED);
			tResp.setMessage("No differences found.");
		}

		return tResp;
	}

}
