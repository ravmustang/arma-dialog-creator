/*
 * Copyright (c) 2016 Kayler Renslow
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * The software is provided "as is", without warranty of any kind, express or implied, including but not limited to the warranties of merchantability, fitness for a particular purpose and noninfringement. in no event shall the authors or copyright holders be liable for any claim, damages or other liability, whether in an action of contract, tort or otherwise, arising from, out of or in connection with the software or the use or other dealings in the software.
 */

package com.kaylerrenslow.armaDialogCreator.gui.fx.main.actions.mainMenu.file;

import com.kaylerrenslow.armaDialogCreator.data.ApplicationDataManager;
import com.kaylerrenslow.armaDialogCreator.gui.fx.notification.Notification;
import com.kaylerrenslow.armaDialogCreator.gui.fx.notification.Notifications;
import com.kaylerrenslow.armaDialogCreator.main.Lang;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.io.File;
import java.io.IOException;

/**
 Created by Kayler on 05/20/2016.
 */
public class FileSaveAction implements EventHandler<ActionEvent> {
	@Override
	public void handle(ActionEvent event) {
		Notification resultNotification;

		try {
			ApplicationDataManager.getInstance().saveProject();
			File saveFile = ApplicationDataManager.getInstance().getCurrentProject().getProjectSaveDirectory();
			resultNotification = new Notification(
					Lang.ApplicationBundle().getString("Notifications.ProjectSave.Success.notification_title"),
					String.format(Lang.ApplicationBundle().getString("Notifications.ProjectSave.Success.notification_body_f"), saveFile.getAbsolutePath())
			);
		} catch (IOException e) {
			e.printStackTrace();
			String reason = e.getMessage() != null && e.getMessage().length() > 0 ? e.getMessage() : Lang.ApplicationBundle().getString("Notifications.ProjectSave.Fail.unknown_reason");
			resultNotification = new Notification(
					Lang.ApplicationBundle().getString("Notifications.ProjectSave.Fail.notification_title"),
					String.format(Lang.ApplicationBundle().getString("Notifications.ProjectSave.Fail.notification_body_f"), reason),
					10 * 1000, true
			);
		}
		Notifications.showNotification(resultNotification);
	}
}
