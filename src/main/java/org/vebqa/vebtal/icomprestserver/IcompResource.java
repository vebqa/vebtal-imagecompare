package org.vebqa.vebtal.icomprestserver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vebqa.vebtal.AbstractTestAdaptionResource;
import org.vebqa.vebtal.TestAdaptionResource;
import org.vebqa.vebtal.icomp.DummyDriver;
import org.vebqa.vebtal.model.Command;
import org.vebqa.vebtal.model.CommandType;
import org.vebqa.vebtal.model.Response;

public class IcompResource extends AbstractTestAdaptionResource implements TestAdaptionResource {

	private static final Logger logger = LoggerFactory.getLogger(IcompResource.class);

	public IcompResource() {
	}
	
	public Response execute(Command cmd) {
		
		IcompTestAdaptionPlugin.setDisableUserActions(true);
		
		Response tResponse = new Response();

		Response result = null;
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
			
			// API compliance - dummy driver needed as there is no seprate driver
			DummyDriver dummyDriver = new DummyDriver();
			
			setStart();
			result = (Response) m.invoke(cmdObj, dummyDriver);
			setFinished();
			
		} catch (ClassNotFoundException e) {
			logger.error("Command implementation class not found!", e);
		} catch (NoSuchMethodException e) {
			logger.error("Execution method in command implementation class not found!", e);
		} catch (SecurityException e) {
			logger.error("Security exception", e);
		} catch (InstantiationException e) {
			logger.error("Cannot instantiate command implementation class!", e);
		} catch (IllegalAccessException e) {
			logger.error("Cannot access implementation class!", e);
		} catch (IllegalArgumentException e) {
			logger.error("Wrong arguments!", e);
		} catch (InvocationTargetException e) {
			logger.error("Error while invoking class!", e);
		}

		if (result == null) {
			tResponse.setCode(Response.FAILED);
			tResponse.setMessage("Cannot resolve command.");
			IcompTestAdaptionPlugin.setDisableUserActions(true);
			return tResponse;
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
