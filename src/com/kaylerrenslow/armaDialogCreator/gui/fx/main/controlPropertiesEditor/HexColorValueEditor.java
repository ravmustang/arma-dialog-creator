package com.kaylerrenslow.armaDialogCreator.gui.fx.main.controlPropertiesEditor;

import com.kaylerrenslow.armaDialogCreator.control.sv.AHexColor;
import com.kaylerrenslow.armaDialogCreator.gui.fx.control.inputfield.ArmaStringChecker;
import com.kaylerrenslow.armaDialogCreator.gui.fx.control.inputfield.InputField;
import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.StackPane;
import org.jetbrains.annotations.NotNull;

/**
 Created by Kayler on 07/13/2016.
 */
public class HexColorValueEditor implements ValueEditor<AHexColor> {
	protected final ColorPicker colorPicker = new ColorPicker();

	private final InputField<ArmaStringChecker, String> overrideField = new InputField<>(new ArmaStringChecker());
	private final StackPane masterPane = new StackPane(colorPicker);

	@Override
	public AHexColor getValue() {
		return new AHexColor(colorPicker.getValue());
	}

	@Override
	public void setValue(AHexColor val) {
		colorPicker.setValue(val.toJavaFXColor());
	}

	@Override
	public @NotNull Node getRootNode() {
		return masterPane;
	}

	@Override
	public void setToOverride(boolean override) {
		masterPane.getChildren().clear();
		if (override) {
			masterPane.getChildren().add(overrideField);
		} else {
			masterPane.getChildren().add(colorPicker);
		}
	}

	@Override
	public InputField<ArmaStringChecker, String> getOverrideTextField() {
		return overrideField;
	}
}