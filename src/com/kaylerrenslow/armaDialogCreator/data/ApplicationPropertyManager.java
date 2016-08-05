package com.kaylerrenslow.armaDialogCreator.data;

import com.kaylerrenslow.armaDialogCreator.data.io.xml.ApplicationPropertyXmlLoader;
import com.kaylerrenslow.armaDialogCreator.data.io.xml.XmlParseException;
import com.kaylerrenslow.armaDialogCreator.gui.fx.main.popup.SelectSaveLocationPopup;
import com.kaylerrenslow.armaDialogCreator.main.ArmaDialogCreator;
import com.kaylerrenslow.armaDialogCreator.main.ExceptionHandler;
import com.kaylerrenslow.armaDialogCreator.util.DataContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 @author Kayler
 Manages application property values from APPDATA/config.xml<br>
 The only instance is inside {@link ApplicationPropertyManager}
 Created on 07/12/2016. */
class ApplicationPropertyManager {
	static final String SAVE_LOCATION_FILE_NAME = "Arma Dialog Creator";
	/** Appdata folder */
	private final File appdataFolder = new File(System.getenv("APPDATA") + "/" + SAVE_LOCATION_FILE_NAME);
	/** DataContext holds all application properties */
	private DataContext applicationProperties = new DataContext();
	
	private final File appPropertiesFile = new File(appdataFolder.getPath() + "/" + "config.xml");
	
	/** Location for application save data. This is where all projects and their data are saved to. */
	private File appSaveDataDir,
	/** Location of Arma 3 tools. Arma 3 tools has some executables valuable to Arma Dialog Creator, such as .paa converter */
	a3ToolsDir;
	
	/**
	 Loads the AppData properties file and stores properties in application properties.
	 */
	public ApplicationPropertyManager() {
		if (!appdataFolder.exists()) {
			setupAppdataFolder();
		} else {
			if (appPropertiesFile.exists()) {
				loadApplicationProperties();
			} else {
				createApplicationPropertiesFile();
			}
		}
		
		//now verify that the loaded a3Tools directory and appdata save directory are actual files that exist and are directories
		File f = ApplicationProperty.A3_TOOLS_DIR.get(applicationProperties);
		if (f == null) {
			//todo notify user that the previously set a3 tools directory is now invalid
		} else if (f.exists() && f.isDirectory()) {
			a3ToolsDir = f;
		}
		
		appSaveDataDir = ApplicationProperty.APP_SAVE_DATA_DIR.get(applicationProperties);
		if (appSaveDataDir == null || !appSaveDataDir.exists()) {
			ArmaDialogCreator.showAfterMainWindowLoaded(new SelectSaveLocationPopup(null, a3ToolsDir));
		} else if (!appSaveDataDir.isDirectory()) {
			ExceptionHandler.fatal(new IllegalStateException("appSaveDataDir exists and is not a directory"));
		}
	}
	
	private void loadApplicationProperties() {
		try {
			ApplicationPropertyXmlLoader.ApplicationPropertyParseResult result = ApplicationPropertyXmlLoader.parse(appPropertiesFile);
			this.applicationProperties = result.getProperties();
//			System.out.println(Arrays.toString(result.getErrors().toArray()));
		} catch (XmlParseException e) {
			ExceptionHandler.error(e);
			loadDefaultValues();
		}
	}
	
	/** Creates the appdata folder and creates the properties file and with all properties set to their default values */
	private void setupAppdataFolder() {
		try {
			appdataFolder.mkdir();
		} catch (SecurityException e) {
			ExceptionHandler.fatal(e);
			return;
		}
		createApplicationPropertiesFile();
	}
	
	private void createApplicationPropertiesFile() {
		try {
			appPropertiesFile.createNewFile();
		} catch (IOException e) {
			ExceptionHandler.fatal(e);
			return;
		}
		loadDefaultValues();
	}
	
	private void loadDefaultValues() {
		for (ApplicationProperty p : ApplicationProperty.values()) {
			applicationProperties.put(p, p.getDefaultValue());
		}
	}
	
	void saveApplicationProperties() throws IOException {
		final String applicationProperty_f = "<application-property name='%s'>%s</application-property>";
		
		FileOutputStream fos = new FileOutputStream(appPropertiesFile);
		
		fos.write("<?xml version='1.0' encoding='UTF-8' ?>\n<config>".getBytes());
		String value;
		for (ApplicationProperty p : ApplicationProperty.values()) {
			if (applicationProperties.getValue(p) == null) {
				value = "";
			} else {
				value = applicationProperties.getValue(p).toString();
			}
			fos.write(String.format(applicationProperty_f, p.getName(), value).getBytes());
		}
		fos.write("</config>".getBytes());
		fos.flush();
		fos.close();
	}
	
	/** Get where application save files should be saved to. */
	@NotNull
	public File getAppSaveDataDirectory() {
		return appSaveDataDir;
	}
	
	/** Get the directory for where Arma 3 tools is saved. If the directory hasn't been set or doesn't exist or the file that is set isn't a directory, will return null. */
	@Nullable
	public File getArma3ToolsDirectory() {
		return a3ToolsDir;
	}
	
	/** Set the application save data directory to a new one. Automatically updates application properties. */
	public void setAppSaveDataLocation(@NotNull File saveLocation) {
		if (!saveLocation.exists()) {
			throw new IllegalStateException("Save location should exist");
		}
		this.appSaveDataDir = saveLocation;
		applicationProperties.put(ApplicationProperty.APP_SAVE_DATA_DIR, saveLocation);
	}
	
	/** Set the arma 3 tools directory to a new one (can be null). Automatically updates application properties. */
	public void setArma3ToolsLocation(@Nullable File file) {
		this.a3ToolsDir = file;
		applicationProperties.put(ApplicationProperty.A3_TOOLS_DIR, file);
	}
	
	public DataContext getApplicationProperties() {
		return applicationProperties;
	}
}