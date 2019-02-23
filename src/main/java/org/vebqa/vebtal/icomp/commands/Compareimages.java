package org.vebqa.vebtal.icomp.commands;

import org.vebqa.vebtal.annotations.Keyword;
import org.vebqa.vebtal.command.AbstractCommand;
import org.vebqa.vebtal.icomprestserver.IcompTestAdaptionPlugin;
import org.vebqa.vebtal.model.CommandType;
import org.vebqa.vebtal.model.Response;

@Keyword(command = "compareImages", module = IcompTestAdaptionPlugin.ID)
public class Compareimages extends AbstractCommand {

	public Compareimages(String aCommand, String aTarget, String aValue) {
		super(aCommand, aTarget, aValue);
		this.type = CommandType.ASSERTION;
	}

	@Override
	public Response executeImpl(Object driver) {
		Response tResp = new Response();
		tResp.setCode(Response.FAILED);
		tResp.setMessage("deprecated keyword compareImages, use compareTo instead.");
		return tResp;
	}

}
