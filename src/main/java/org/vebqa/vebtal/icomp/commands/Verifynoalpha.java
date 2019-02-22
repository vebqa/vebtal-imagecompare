package org.vebqa.vebtal.icomp.commands;

import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.vebqa.vebtal.annotations.Keyword;
import org.vebqa.vebtal.command.AbstractCommand;
import org.vebqa.vebtal.icomp.ImageDriver;
import org.vebqa.vebtal.icomprestserver.IcompTestAdaptionPlugin;
import org.vebqa.vebtal.model.CommandType;
import org.vebqa.vebtal.model.Response;

@Keyword(module = IcompTestAdaptionPlugin.ID, command = "Close")
public class Verifynoalpha extends AbstractCommand {

	public Verifynoalpha(String aCommand, String aTarget, String aValue) {
		super(aCommand, aTarget, aValue);
		this.type = CommandType.ACTION;
	}

	@Override
	public Response executeImpl(Object driver) {
		ImageDriver imgDriver = (ImageDriver)driver;
		
		Response tResp = new Response();
		
		if (!imgDriver.isLoaded()) {
			tResp.setCode(Response.FAILED);
			tResp.setMessage("No opened file.");
			return tResp;
		}
		
		Vector<Mat> rgba = new Vector<Mat>();
		Core.split(imgDriver.getCurrent(), rgba);
		if (rgba.size() > 3) {
			tResp.setCode(Response.FAILED);
			tResp.setMessage("Image has more than three channels: [" + rgba.size() + "]");
			return tResp;
		}
		
		tResp.setCode(Response.PASSED);
		tResp.setMessage("Number of channels in image: [" + rgba.size() + "]");

		return tResp;
	}
}
