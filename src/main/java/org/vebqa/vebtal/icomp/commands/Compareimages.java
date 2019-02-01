package org.vebqa.vebtal.icomp.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
import org.vebqa.vebtal.command.AbstractCommand;
import org.vebqa.vebtal.icomp.ImageCompareResult;
import org.vebqa.vebtal.model.CommandType;
import org.vebqa.vebtal.model.Response;

import ar.com.hjg.pngj.PngReader;

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
		
		Mat reference = new Mat();
		Mat compare = new Mat();
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
		if (tCurrReader.imgInfo.channels == 4) {
			ConvertCmd tCmd = new ConvertCmd();
			tCmd.setAsyncMode(false);
			tCmd.setSearchPath(GuiManager.getinstance().getConfig().getString("im.path"));
			IMOperation tOp = new IMOperation();
			tOp.addImage(aCurrentImg);
			tOp.flatten();
			try {
				tCmd.run(tOp);
			} catch (IM4JavaException | IOException | InterruptedException e) {
				tResp.setCode(Response.FAILED);
				tResp.setMessage("Cannot flatten current image: " + e.getMessage());
				return tResp;
			}
			
			return tResp;
		}
		
		// check, weather image is three or four channel, containing alpha channel.
		PngReader tRefReader = new PngReader(new File(aReferenceImg));
		if (tRefReader.imgInfo.channels == 4) {
			ConvertCmd tCmd = new ConvertCmd();
			tCmd.setAsyncMode(false);
			tCmd.setSearchPath(GuiManager.getinstance().getConfig().getString("im.path"));
			IMOperation tOp = new IMOperation();
			tOp.addImage(aReferenceImg);
			tOp.flatten();
			try {
				tCmd.run(tOp);
			} catch (IM4JavaException | IOException | InterruptedException e) {
				tResp.setCode(Response.FAILED);
				tResp.setMessage("Cannot flatten reference image: " + e.getMessage());
				return tResp;
			}
			return tResp;
		}
		
		reference = Imgcodecs.imread(aReferenceImg, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		compare = Imgcodecs.imread(aCurrentImg, Imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);
		compare.copyTo(resultImage);

		MatOfKeyPoint keypointsSource = new MatOfKeyPoint();
		MatOfKeyPoint keypointsCompare = new MatOfKeyPoint();

		Mat descriptorSource = new Mat();
		Mat descriptorCompare = new Mat();

		// detect keypoints
		ORB detector = ORB.create();
		detector.detect(reference, keypointsSource);
		detector.detect(compare, keypointsCompare);

		// extract descriptors
		ORB extractor = ORB.create();
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

		Integer tDistance = 1;

		for (int i = 0; i < matchesList.size(); i++) {
			if (matchesList.get(i).distance > tDistance) {
				matchesFinal.add(matches.toList().get(i));
			}
		}

		errorMat.fromList(matchesFinal);

		// Differenzmenge merken
		int tDifferenceCount = matchesFinal.size();
		logger.debug(tDifferenceCount + " differences found.");
		ImageCompareResult tResult = new ImageCompareResult();

		// Erzeuge die Differenz von Referenz zu Vergleichststand und speichere
		// in Destination
		Mat destination = new Mat();

		if (reference.cols() == compare.cols()) {
			Core.absdiff(reference, compare, destination);
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
			tResp.setCode(Response.FAILED);
			tResp.setMessage("Images have differences..." + tDifferenceCount);
			logger.info(tDifferenceCount + " differences found an written to " + fnDifference);
		} else {
			tResp.setCode(Response.PASSED);
			tResp.setMessage("No differences found.");
		}		
		
		return tResp;
	}

}
