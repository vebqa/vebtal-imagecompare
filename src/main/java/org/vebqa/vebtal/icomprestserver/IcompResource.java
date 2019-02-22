package org.vebqa.vebtal.icomprestserver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vebqa.vebtal.AbstractTestAdaptionResource;
import org.vebqa.vebtal.TestAdaptionResource;
import org.vebqa.vebtal.icomp.ImageStore;
import org.vebqa.vebtal.model.Command;
import org.vebqa.vebtal.model.CommandType;
import org.vebqa.vebtal.model.Response;

public class IcompResource extends AbstractTestAdaptionResource implements TestAdaptionResource {

	private static final Logger logger = LoggerFactory.getLogger(IcompResource.class);

	public IcompResource() {
	}
	
	public Response execute(Command cmd) {
		
		IcompTestAdaptionPlugin.setDisableUserActions(true);
		
		Response result = new Response();
		boolean hasException = false;
		String failedReason = null;
		
		try {
			Class<?> cmdClass = Class.forName("org.vebqa.vebtal.icomp.commands." + getCommandClassName(cmd));
			Constructor<?> cons = cmdClass.getConstructor(String.class, String.class, String.class);
			Object cmdObj = cons.newInstance(cmd.getCommand(), cmd.getTarget(), cmd.getValue());
			
			// get type
			Method mType = cmdClass.getMethod("getType");
			CommandType cmdType = (CommandType)mType.invoke(cmdObj);
			IcompTestAdaptionPlugin.addCommandToList(cmd, cmdType);
			
			// execute
			Method m = cmdClass.getDeclaredMethod("executeImpl", Object.class);
						
			setStart();
			result = (Response) m.invoke(cmdObj, ImageStore.getStore().getDriver());
			setFinished();
			
		} catch (ClassNotFoundException e) {
			logger.error("Command implementation class not found!", e);
			hasException=true;
			failedReason = "Command implementation class not found: " + e.getMessage(); 
		} catch (NoSuchMethodException e) {
			logger.error("Execution method in command implementation class not found!", e);
			hasException=true;
			failedReason = "Execution method in command implementation class not found: " + e.getMessage();
		} catch (SecurityException e) {
			logger.error("Security exception", e);
			hasException=true;
			failedReason = "Security exception: " + e.getMessage();
		} catch (InstantiationException e) {
			logger.error("Cannot instantiate command implementation class!", e);
			hasException=true;
			failedReason = "Cannot instantiate command implementation class: " + e.getMessage();
		} catch (IllegalAccessException e) {
			logger.error("Cannot access implementation class!", e);
			hasException=true;
			failedReason = "Cannot access implementation class: " + e.getMessage();
		} catch (IllegalArgumentException e) {
			logger.error("Wrong arguments!", e);
			hasException=true;
			failedReason = "Wrong arguments: " + e.getMessage();
		} catch (InvocationTargetException e) {
			logger.error("Error while invoking class!", e);
			hasException=true;
			failedReason = "Error while invoking class: " + e.getMessage();
		} catch (Exception e) {
			logger.error("an exception occurred!", e);
			hasException=true;
			failedReason = "An error occured: " + e.getMessage();
		}

		if (hasException) {
			result.setCode(Response.FAILED);
			result.setMessage(failedReason);
			IcompTestAdaptionPlugin.setDisableUserActions(true);
			return result;
		}
		if (result.getCode() != Response.PASSED) {
			IcompTestAdaptionPlugin.setLatestResult(false, result.getMessage());
		} else {
			IcompTestAdaptionPlugin.setLatestResult(true, result.getMessage());
		}
		
		IcompTestAdaptionPlugin.setDisableUserActions(true);
		return result;
	}

}
