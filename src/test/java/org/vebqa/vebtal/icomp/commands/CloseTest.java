package org.vebqa.vebtal.icomp.commands;

import static org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.vebqa.vebtal.icomp.ImageDriver;
import org.vebqa.vebtal.model.Response;

public class CloseTest {

	@Rule
	public final ImageDriver imageDriver = new ImageDriver()
			.setOpenCVPath("C:\\tools\\VEBTal (Beta Release)\\tools\\opencv-3.4.3-vc14_vc15\\opencv\\build\\java\\x64")
			.loadImage("./src/test/java/resource/splash001.png");

	@Test
	public void closeImage() {
		// create command to test
		Close cmd = new Close("close", "", "");
		Response result = cmd.executeImpl(imageDriver);

		// create a green result object
		Response resultCheck = new Response();
		resultCheck.setCode(Response.PASSED);
		resultCheck.setMessage("Image closed.");

		// check
		assertThat(resultCheck, samePropertyValuesAs(result));
	}
}
