package org.vebqa.vebtal.icomprestserver;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.opencv.core.Core;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vebqa.vebtal.AbstractTestAdaptionPlugin;
import org.vebqa.vebtal.GuiManager;
import org.vebqa.vebtal.TestAdaptionType;
import org.vebqa.vebtal.model.Command;
import org.vebqa.vebtal.model.CommandResult;
import org.vebqa.vebtal.model.CommandType;
import org.vebqa.vebtal.sut.SutStatus;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

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
		String opencv_path = GuiManager.getinstance().getConfig().getString("opencv.path");
		opencv_path = opencv_path + "\\" + Core.NATIVE_LIBRARY_NAME + ".dll";
		try {
			System.load(opencv_path);
		} catch (Exception e) {
			String tError = "Native code library failed to load from: " + opencv_path;
			logger.error(tError, e);
		}

		Tab icompTab = createTab(ID, commandList, clData);
		
		// Add
		final TextField addCommand = new TextField();
        addCommand.setPromptText("Command");
        addCommand.setMaxWidth(200);
        final TextField addTarget = new TextField();
        addTarget.setMaxWidth(200);
        addTarget.setPromptText("Target");
        final TextField addValue = new TextField();
        addValue.setMaxWidth(200);
        addValue.setPromptText("Value");
 
        final Button addButton = new Button("Go");
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
            	Command newCmd = new Command(addCommand.getText(), addTarget.getText(), addValue.getText());
            	
                addCommand.clear();
                addTarget.clear();
                addValue.clear();
                
                IcompResource aResource = new IcompResource();
                GuiManager.getinstance().setTabStatus(IcompTestAdaptionPlugin.ID, SutStatus.CONNECTED);
                aResource.execute(newCmd);
                GuiManager.getinstance().setTabStatus(IcompTestAdaptionPlugin.ID, SutStatus.DISCONNECTED);
            }
        });
 
        HBox hbox = new HBox();
         
        hbox.getChildren().addAll(addCommand, addTarget, addValue, addButton);
        hbox.setSpacing(3);

		BorderPane pane = (BorderPane)icompTab.getContent();
		pane.setTop(hbox);        
        
        return icompTab;
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
		return loadConfig(ID);
	}
}
