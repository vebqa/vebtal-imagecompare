package org.vebqa.vebtal.icomp;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.rules.ExternalResource;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageDriver extends ExternalResource {

	private String pathToResource;
	
	private String opencv_path;
	
	private boolean isLoaded;
	
	private boolean isInitialized;
	
	private Mat current;

	private static final Logger logger = LoggerFactory.getLogger(ImageDriver.class);
	
	public ImageDriver() {
		this.isLoaded = false;
		this.isInitialized = false;
		
		// /get opencv path from environment, if available and initialize automatically
		this.opencv_path = System.getenv("OPENCV");
		if (this.opencv_path != null && this.opencv_path != "") {
			init();
		}
	}

	public void init() {
		
		opencv_path = opencv_path + "\\" + Core.NATIVE_LIBRARY_NAME + ".dll";
		try {
			System.load(opencv_path);
			this.isInitialized = true;
		} catch (Exception e) {
			String tError = "Native code library failed to load from: " + opencv_path;
			logger.error(tError, e);
		}
		this.current = new Mat();
	}
	
	public ImageDriver loadImage(String aPathToDoc) {
		this.pathToResource = aPathToDoc;
		load();
		return this;
	}

	public void load(File anImageFile) {
		this.pathToResource = anImageFile.getAbsolutePath();
		load();
	}
	
	public void load() {
		assertTrue("OpenCV not initialized - provide an OpenCV binary path before usage.", this.isInitialized);
		this.current = Imgcodecs.imread(this.pathToResource, Imgcodecs.IMREAD_COLOR);
		this.isLoaded = true;
	}
	
	public void close() {
		this.current = new Mat();
		this.isLoaded = false;
	}
	
	public boolean isLoaded() {
		return this.isLoaded;
	}
	
	public boolean isInitialized() {
		return this.isInitialized;
	}
	
	/**
	 * Usage via fluent api in external resource context
	 * @param aPath
	 */
	public ImageDriver setOpenCVPath(String aPath) {
		this.opencv_path = aPath;
		init();
		return this;
	}
	
	public Mat getCurrent() {
		return this.current;
	}
}