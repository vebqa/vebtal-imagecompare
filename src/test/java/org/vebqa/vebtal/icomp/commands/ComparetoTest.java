package org.vebqa.vebtal.icomp.commands;

import static org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.vebqa.vebtal.icomp.ImageDriver;
import org.vebqa.vebtal.model.Response;

public class ComparetoTest {

	/**
	 * OpenCV path should be in env with key "OPENCV"
	 */
	@Rule
	public final ImageDriver imageDriver = new ImageDriver()
			.loadImage("./src/test/java/resource/splash001.png");

	@Test
	public void verifyThatCurrentImageHas145DifferencesToReferenceImage() {
		// create command to test
		Compareto cmd = new Compareto("compareTo", "./src/test/java/resource/splash001_reference.png", "c:/temp/diff.png");
		Response result = cmd.executeImpl(imageDriver);

		// create a green result object
		Response resultCheck = new Response();
		resultCheck.setCode(Response.FAILED);
		resultCheck.setMessage("145 differences found. Diff-File: c:/temp/diff.png");

		// check
		assertThat(resultCheck, samePropertyValuesAs(result));
	}
	
	@Test
	public void verifyThatCurrentImageHasNoDifferencesToReferenceImage() {
		// create command to test
		Compareto cmd = new Compareto("compareTo", "./src/test/java/resource/splash001.png", "c:/temp/diff.png");
		Response result = cmd.executeImpl(imageDriver);

		// create a green result object
		Response resultCheck = new Response();
		resultCheck.setCode(Response.PASSED);
		resultCheck.setMessage("No differences found.");

		// check
		assertThat(resultCheck, samePropertyValuesAs(result));
	}
}
