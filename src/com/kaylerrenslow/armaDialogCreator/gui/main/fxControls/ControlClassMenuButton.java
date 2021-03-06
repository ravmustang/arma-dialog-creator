package com.kaylerrenslow.armaDialogCreator.gui.main.fxControls;

import com.kaylerrenslow.armaDialogCreator.control.ControlClass;
import com.kaylerrenslow.armaDialogCreator.gui.fxcontrol.ComboBoxMenuButton;
import javafx.scene.Node;

/**
 Created by Kayler on 10/19/2016.
 */
public class ControlClassMenuButton extends ComboBoxMenuButton<ControlClass> {

	public ControlClassMenuButton(boolean allowClear, String placeholderText, Node placeholderGraphic) {
		super(allowClear, placeholderText, placeholderGraphic);
	}

	public ControlClassMenuButton(boolean allowClear, String placeholderText, Node placeholderGraphic, ControlClassGroupMenu... classGroups) {
		super(allowClear, placeholderText, placeholderGraphic, classGroups);
	}

	public ControlClassMenuButton(boolean allowClear, String placeholderText, Node placeholderGraphic, ControlClassMenuItem... items) {
		super(allowClear, placeholderText, placeholderGraphic, items);
	}
}
