package org.vebqa.vebtal.icomp.commands;

import org.vebqa.vebtal.annotations.Keyword;
import org.vebqa.vebtal.command.AbstractCommand;
import org.vebqa.vebtal.icomp.ImageDriver;
import org.vebqa.vebtal.icomprestserver.IcompTestAdaptionPlugin;
import org.vebqa.vebtal.model.CommandType;
import org.vebqa.vebtal.model.Response;

@Keyword(module = IcompTestAdaptionPlugin.ID, command = "Close")
public class Close extends AbstractCommand {

	public Close(String aCommand, String aTarget, String aValue) {
		super(aCommand, aTarget, aValue);
		this.type = CommandType.ACTION;
	}

	@Override
	public Response executeImpl(Object driver) {
		ImageDriver imgDriver = (ImageDriver)driver;
		
		Response tResp = new Response();
		
		if (!imgDriver.isLoaded()) {
			tResp.setCode(Response.FAILED);
			tResp.setMessage("No opened file - cannot close.");
			return tResp;
		}
		
		imgDriver.close();

		tResp.setCode(Response.PASSED);
		tResp.setMessage("Image closed.");

		return tResp;
	}
}
