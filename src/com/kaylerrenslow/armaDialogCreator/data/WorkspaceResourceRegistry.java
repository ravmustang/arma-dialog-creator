package com.kaylerrenslow.armaDialogCreator.data;

import com.kaylerrenslow.armaDialogCreator.data.xml.ResourceRegistryXmlWriter;
import com.kaylerrenslow.armaDialogCreator.main.ExceptionHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 A {@link WorkspaceResourceRegistry} is a workspace level {@link ResourceRegistry}.
 This {@link ResourceRegistry} has the option to share resources between projects.

 @author Kayler
 @since 11/23/2016 */
public class WorkspaceResourceRegistry extends ResourceRegistry {

	protected WorkspaceResourceRegistry(@NotNull Workspace workspace) {
		super(workspace.getFileInAdcDirectory("resources/global-resources.xml"));
		if (!getResourcesFile().exists()) {
			getResourcesFile().getParentFile().mkdirs();
			try {
				getResourcesFile().createNewFile();
				ResourceRegistryXmlWriter.WorkspaceResourceRegistryXmlWriter.writeAndClose(this);
			} catch (IOException e) {
				ExceptionHandler.error(e);
			}
		}
	}

	/**
	 Get the {@link WorkspaceResourceRegistry} for the instance returned by {@link Workspace#getWorkspace()}

	 @return registry
	 */
	@NotNull
	public static WorkspaceResourceRegistry getInstance() {
		return Workspace.getWorkspace().getGlobalResourceRegistry();
	}

}
