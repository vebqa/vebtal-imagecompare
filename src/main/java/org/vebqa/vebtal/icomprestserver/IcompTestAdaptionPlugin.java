package org.vebqa.vebtal.icomprestserver;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vebqa.vebtal.AbstractTestAdaptionPlugin;
import org.vebqa.vebtal.TestAdaptionType;
import org.vebqa.vebtal.model.Command;
import org.vebqa.vebtal.model.CommandResult;
import org.vebqa.vebtal.model.CommandType;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;

@SuppressWarnings("restriction")
public class IcompTestAdaptionPlugin extends AbstractTestAdaptionPlugin {

	private static final Logger logger = LoggerFactory.getLogger(IcompTestAdaptionPlugin.class);
	
	/**
	 * unique id of the test adapter
	 */
	public static final String ID = "icomp";

	/**
	 * tableview with commands
	 */
	protected static final TableView<CommandResult> commandList = new TableView<>();

	/**
	 * results after execution
	 */
	protected static final ObservableList<CommandResult> clData = FXCollections.observableArrayList();

	public IcompTestAdaptionPlugin() {
		super(TestAdaptionType.ADAPTER);
	}

	public String getName() {
		return "ImageCompare Plugin for RoboManager";
	}

	@Override
	public Tab startup() {
		String opencv_path = "C:\\Tools\\OpenCV\\opencv341\\build\\java\\x86" + "\\" + Core.NATIVE_LIBRARY_NAME + ".dll";
		try {
			System.load(opencv_path);
		} catch (Exception e) {
			String tError = "Native code library failed to load from: " + opencv_path;
			logger.error(tError, e);
		}
		
		return createTab(ID, commandList, clData);
	}

	public static void addCommandToList(Command aCmd, CommandType aType) {
		String aValue = aCmd.getValue();
		CommandResult tCR = new CommandResult(aCmd.getCommand(), aCmd.getTarget(), aValue, aType);
		Platform.runLater(() -> clData.add(tCR));
	}

	public static void setLatestResult(Boolean success, final String aResult) {
		Platform.runLater(() -> clData.get(clData.size() - 1).setLogInfo(aResult));
		Platform.runLater(() -> clData.get(clData.size() - 1).setResult(success));

		commandList.refresh();
		Platform.runLater(() -> commandList.scrollTo(clData.size() - 1));
	}

	@Override
	public boolean shutdown() {
		return true;
	}

	/**
	 * this is the new service provider implementation.
	 */
	public Class<?> getImplementation() {
		return null;
	}

	@Override
	public String getAdaptionID() {
		return ID;
	}

	@Override
	public CombinedConfiguration loadConfig() {
		// TODO Auto-generated method stub
		return null;
	}
}
