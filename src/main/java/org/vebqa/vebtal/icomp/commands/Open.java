package org.vebqa.vebtal.icomp.commands;

import java.io.File;

import org.vebqa.vebtal.annotations.Keyword;
import org.vebqa.vebtal.command.AbstractCommand;
import org.vebqa.vebtal.icomp.ImageDriver;
import org.vebqa.vebtal.icomprestserver.IcompTestAdaptionPlugin;
import org.vebqa.vebtal.model.CommandType;
import org.vebqa.vebtal.model.Response;

@Keyword(module = IcompTestAdaptionPlugin.ID, command = "Open", hintTarget = "path/to/current.png")
public class Open extends AbstractCommand {

	public Open(String aCommand, String aTarget, String aValue) {
		super(aCommand, aTarget, aValue);
		this.type = CommandType.ACTION;
	}

	@Override
	public Response executeImpl(Object driver) {
		ImageDriver imgDriver = (ImageDriver) driver;

		String aCurrentImg = this.target;

		Response tResp = new Response();
		tResp.setCode(Response.FAILED);
		tResp.setMessage("not processed yet.");

		// check, if a current file is existing
		File tCurrent = new File(aCurrentImg);
		if (!tCurrent.exists()) {
			tResp.setCode(Response.FAILED);
			tResp.setMessage("Cannot find current image!");
			return tResp;
		}

		imgDriver.load(tCurrent);
		tResp.setCode(Response.PASSED);
		tResp.setMessage("Image file successfully read.");

		return tResp;
	}

}
