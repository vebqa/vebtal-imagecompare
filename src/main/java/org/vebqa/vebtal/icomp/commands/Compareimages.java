package org.vebqa.vebtal.icomp.commands;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
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
import org.vebqa.vebtal.GuiManager;
import org.vebqa.vebtal.annotations.Keyword;
import org.vebqa.vebtal.command.AbstractCommand;
import org.vebqa.vebtal.icomprestserver.IcompTestAdaptionPlugin;
import org.vebqa.vebtal.model.CommandType;
import org.vebqa.vebtal.model.Response;

import ar.com.hjg.pngj.PngReader;

@Keyword(module = IcompTestAdaptionPlugin.ID, command = "CompareImages", hintTarget = "path/to/current.png", hintValue = "path/to/reference.png")
public class Compareimages extends AbstractCommand {

	Logger logger = LoggerFactory.getLogger(Compareimages.class);

	public Compareimages(String aCommand, String aTarget, String aValue) {
		super(aCommand, aTarget, aValue);
		this.type = CommandType.ASSERTION;
	}

	@Override
	public Response executeImpl(Object driver) {
		String aCurrentImg = this.target;
		String aReferenceImg = this.value;

		Response tResp = new Response();
		tResp.setCode(Response.FAILED);
		tResp.setMessage("not processed yet.");

		Mat reference = new Mat();
		Mat current = new Mat();
		final Mat resultImage = new Mat();

		// check, if reference file is existing
		File tReference = new File(aReferenceImg);
		if (!tReference.exists()) {
			tResp.setCode(Response.FAILED);
			tResp.setMessage("Cannot find reference image!");
			return tResp;
		}

		// check, if compare file is existing
		File tCurrent = new File(aCurrentImg);
		if (!tCurrent.exists()) {
			tResp.setCode(Response.FAILED);
			tResp.setMessage("Cannot find current image!");
			return tResp;
		}

		// check, weather image is three or four channel, containing alpha channel.
		PngReader tCurrReader = new PngReader(new File(aCurrentImg));
		boolean currentFlattened = false;
		if (tCurrReader.imgInfo.channels == 4) {
			currentFlattened = true;
			logger.info("current is 4 channel - flattening");
			ConvertCmd tCmd = new ConvertCmd();
			tCmd.setAsyncMode(false);
			tCmd.setSearchPath(GuiManager.getinstance().getConfig().getString("im.path"));
			IMOperation tOp = new IMOperation();
			tOp.addImage(aCurrentImg);
			tOp.flatten();
			tOp.addImage(aCurrentImg + ".test.png");
			try {
				tCmd.run(tOp);
			} catch (IM4JavaException | IOException | InterruptedException e) {
				tResp.setCode(Response.FAILED);
				tResp.setMessage("Cannot flatten current image: " + e.getMessage());
				return tResp;
			}
			aCurrentImg = aCurrentImg + ".test.png";
		} else {
			logger.info("current is less than 4 channel - nothing to do!");
		}

		// check, weather image is three or four channel, containing alpha channel.
		PngReader tRefReader = new PngReader(new File(aReferenceImg));
		boolean referenceFlattened = false;
		if (tRefReader.imgInfo.channels == 4) {
			referenceFlattened = true;
			logger.info("reference is 4 channel - flattening");
			ConvertCmd tCmd = new ConvertCmd();
			tCmd.setAsyncMode(false);
			tCmd.setSearchPath(GuiManager.getinstance().getConfig().getString("im.path"));
			IMOperation tOp = new IMOperation();
			tOp.addImage(aReferenceImg);
			tOp.flatten();
			tOp.addImage(aReferenceImg + ".test.png");
			try {
				tCmd.run(tOp);
			} catch (IM4JavaException | IOException | InterruptedException e) {
				tResp.setCode(Response.FAILED);
				tResp.setMessage("Cannot flatten reference image: " + e.getMessage());
				return tResp;
			}
			aReferenceImg = aReferenceImg + ".test.png";
		} else {
			logger.info("reference is less than 4 channel - nothing to do!");
		}

		reference = Imgcodecs.imread(aReferenceImg, Imgcodecs.CV_LOAD_IMAGE_COLOR);
		// reference.convertTo(reference, CvType.CV_8UC3);
		current = Imgcodecs.imread(aCurrentImg, Imgcodecs.CV_LOAD_IMAGE_COLOR);
		// current.convertTo(current, CvType.CV_8UC3);
		current.copyTo(resultImage);

		// clean up
		if (currentFlattened) {
			File curFile = new File(aCurrentImg);
			curFile.delete();
		}

		if (referenceFlattened) {
			File refFile = new File(aReferenceImg);
			refFile.delete();
		}

		MatOfKeyPoint keypointsRef = new MatOfKeyPoint();
		MatOfKeyPoint keypointsCurrent = new MatOfKeyPoint();

		Mat descriptorRef = new Mat();
		Mat descriptorCurrent = new Mat();

		Imgproc.cvtColor(reference, reference, Imgproc.COLOR_RGB2GRAY);
		Imgproc.cvtColor(current, current, Imgproc.COLOR_RGB2GRAY);
		Imgproc.cvtColor(resultImage, resultImage, Imgproc.COLOR_RGB2GRAY);

		// detect keypoints
		ORB detector = ORB.create();
		// detector = FeatureDetector.create(FeatureDetector.ORB);
		detector.detect(reference, keypointsRef);
		detector.detect(current, keypointsCurrent);

		// extract descriptors
		ORB descriptor = ORB.create();
		// descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
		descriptor.compute(reference, keypointsRef, descriptorRef);
		descriptor.compute(current, keypointsCurrent, descriptorCurrent);

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

		if (reference.cols() == current.cols()) {
			Core.absdiff(reference, current, destination);
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

			String fnDifference = GuiManager.getinstance().getConfig().getString("diff.path");
			String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
			fnDifference = fnDifference + "\\" + timeStamp + "-diff.png";

			Imgcodecs.imwrite(fnDifference, resultImage);
			tResp.setCode(Response.FAILED);
			tResp.setMessage(tDifferenceCount + " differences found. Diff-File: " + fnDifference);
			logger.info(tDifferenceCount + " differences found and written to " + fnDifference);
		} else {
			tResp.setCode(Response.PASSED);
			tResp.setMessage("No differences found: " + matchesFinal.size() + " | " + matchesList.size());
		}

		return tResp;
	}

}
