package org.vebqa.vebtal.icomp.commands;

import static org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.vebqa.vebtal.icomp.ImageDriver;
import org.vebqa.vebtal.icomp.annotations.OpenCVIntegrationTests;
import org.vebqa.vebtal.model.Response;

public class VerifynoalphaTest {

	/**
	 * OpenCV path should be in env with key "OPENCV"
	 */
	@Rule
	public final ImageDriver imageDriver = new ImageDriver().loadImage("./src/test/java/resource/splash001.png");

	@Category(OpenCVIntegrationTests.class)
	@Test
	public void verifyThatImageHasNoAlphaChannel() {
		// create command to test
		Verifynoalpha cmd = new Verifynoalpha("verifyNoAlpha", "", "");
		Response result = cmd.executeImpl(imageDriver);

		// create a green result object
		Response resultCheck = new Response();
		resultCheck.setCode(Response.PASSED);
		resultCheck.setMessage("Number of channels in image: [3]");

		// check
		assertThat(resultCheck, samePropertyValuesAs(result));
	}
}
