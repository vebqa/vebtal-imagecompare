package org.vebqa.vebtal.icomp.commands;

import static org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.vebqa.vebtal.icomp.ImageDriver;
import org.vebqa.vebtal.model.Response;

public class VerifynoalphaTest {

	@Rule
	public final ImageDriver imageDriver = new ImageDriver()
			.setOpenCVPath("C:\\tools\\VEBTal (Beta Release)\\tools\\opencv-3.4.3-vc14_vc15\\opencv\\build\\java\\x64")
			.loadImage("./src/test/java/resource/splash001.png");

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
