package com.kaylerrenslow.armaDialogCreator.arma.control.impl;

import com.kaylerrenslow.armaDialogCreator.arma.control.ArmaControl;
import com.kaylerrenslow.armaDialogCreator.arma.control.ArmaControlRenderer;
import com.kaylerrenslow.armaDialogCreator.arma.control.impl.utility.*;
import com.kaylerrenslow.armaDialogCreator.arma.util.ArmaResolution;
import com.kaylerrenslow.armaDialogCreator.control.ControlPropertyLookup;
import com.kaylerrenslow.armaDialogCreator.control.sv.*;
import com.kaylerrenslow.armaDialogCreator.expression.Env;
import com.kaylerrenslow.armaDialogCreator.gui.uicanvas.CanvasContext;
import com.kaylerrenslow.armaDialogCreator.gui.uicanvas.Region;
import com.kaylerrenslow.armaDialogCreator.util.ValueListener;
import com.kaylerrenslow.armaDialogCreator.util.ValueObserver;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 @author Kayler
 @since 11/21/2016 */
public class ButtonRenderer extends ArmaControlRenderer {
	private BasicTextRenderer textRenderer;
	private BlinkControlHandler blinkControlHandler;
	private TooltipRenderer tooltipRenderer;
	private Function<GraphicsContext, Void> tooltipRenderFunc = gc -> {
		tooltipRenderer.paint(gc, this.mouseOverX, this.mouseOverY);
		return null;
	};

	private double offsetX, offsetY;

	/** color of drop shadow behind button */
	private Color colorShadow = Color.BLACK;
	/** bg color when mouse is over control */
	private Color colorBackgroundActive = Color.BLACK;
	/** bg color when control is disabled */
	private Color colorBackgroundDisabled = Color.BLACK;
	/** color of left border */
	private Color colorBorder = Color.BLACK;
	/** text color if control is disabled */
	private Color colorDisabled = Color.BLACK;
	private Color colorFocused = Color.BLACK, colorFocused2 = Color.BLACK;

	/**
	 alternating bg color helper. if control has focus (but mouse isn't over control), colorFocused and
	 colorFocused2 will alternate
	 */
	private final AlternatorHelper<Color> focusedColorAlternator = new AlternatorHelper<>(500);

	public ButtonRenderer(ArmaControl control, ArmaResolution resolution, Env env) {
		super(control, resolution, env);
		textRenderer = new BasicTextRenderer(control, this, ControlPropertyLookup.TEXT,
				ControlPropertyLookup.COLOR_TEXT, ControlPropertyLookup.STYLE, ControlPropertyLookup.SIZE_EX,
				ControlPropertyLookup.SHADOW
		);

		myControl.findProperty(ControlPropertyLookup.COLOR_BACKGROUND).getValueObserver().addListener(new ValueListener<SerializableValue>() {
			@Override
			public void valueUpdated(@NotNull ValueObserver<SerializableValue> observer, SerializableValue oldValue, SerializableValue newValue) {
				getBackgroundColorObserver().updateValue((SVColor) newValue);
			}
		});
		myControl.findProperty(ControlPropertyLookup.COLOR_SHADOW).getValueObserver().addListener((observer,
																								   oldValue, newValue) -> {
			if (newValue instanceof SVColor) {
				colorShadow = ((SVColor) newValue).toJavaFXColor();
				requestRender();
			}
		});

		myControl.findProperty(ControlPropertyLookup.OFFSET_X).getValueObserver().addListener((observer, oldValue,
																							   newValue) -> {
			if (newValue instanceof SVNumericValue) {
				offsetX = ((SVNumericValue) newValue).toDouble();
				requestRender();
			}
		});
		myControl.findProperty(ControlPropertyLookup.OFFSET_Y).getValueObserver().addListener(
				(observer, oldValue, newValue) -> {
					if (newValue instanceof SVNumericValue) {
						offsetY = ((SVNumericValue) newValue).toDouble();
						requestRender();
					}
				}
		);
		myControl.findProperty(ControlPropertyLookup.COLOR_BACKGROUND_ACTIVE).getValueObserver().addListener(
				(observer, oldValue, newValue) -> {
					if (newValue instanceof SVColor) {
						colorBackgroundActive = ((SVColor) newValue).toJavaFXColor();
						requestRender();
					}
				}
		);
		myControl.findProperty(ControlPropertyLookup.COLOR_BACKGROUND_DISABLED).getValueObserver().addListener(
				(observer, oldValue, newValue) -> {
					if (newValue instanceof SVColor) {
						colorBackgroundDisabled = ((SVColor) newValue).toJavaFXColor();
						requestRender();
					}
				}
		);
		myControl.findProperty(ControlPropertyLookup.COLOR_FOCUSED).getValueObserver().addListener(
				(observer, oldValue, newValue) -> {
					if (newValue instanceof SVColor) {
						colorFocused = ((SVColor) newValue).toJavaFXColor();
						requestRender();
					}
				}
		);
		myControl.findProperty(ControlPropertyLookup.COLOR_FOCUSED2).getValueObserver().addListener(
				(observer, oldValue, newValue) -> {
					if (newValue == null) {
						colorFocused2 = null;
						requestRender();
						return;
					}
					if (newValue instanceof SVColor) {
						colorFocused2 = ((SVColor) newValue).toJavaFXColor();
						requestRender();
					}
				}
		);
		myControl.findProperty(ControlPropertyLookup.DEFAULT).getValueObserver().addListener(
				(observer, oldValue, newValue) -> {
					if (newValue == null && focusedControlRenderer == this) {
						focusedControlRenderer = null;
						requestRender();
						return;
					}
					if (newValue instanceof SVBoolean) {
						if (((SVBoolean) newValue).isTrue()) {
							focusedControlRenderer = this;
						} else {
							focusedControlRenderer = null;
						}
						requestRender();
					}
				}
		);
		blinkControlHandler = new BlinkControlHandler(myControl.findProperty(ControlPropertyLookup.BLINKING_PERIOD));

		myControl.findProperty(ControlPropertyLookup.COLOR_BACKGROUND).setValueIfAbsent(true, new SVColorArray(getBackgroundColor()));
		myControl.findProperty(ControlPropertyLookup.COLOR_TEXT).setValueIfAbsent(true, new SVColorArray(getTextColor()));
		myControl.findProperty(ControlPropertyLookup.TEXT).setValueIfAbsent(true, SVString.newEmptyString());


		tooltipRenderer = new TooltipRenderer(
				this.myControl,
				ControlPropertyLookup.TOOLTIP_COLOR_SHADE,
				ControlPropertyLookup.TOOLTIP_COLOR_TEXT,
				ControlPropertyLookup.TOOLTIP_COLOR_BOX,
				ControlPropertyLookup.TOOLTIP
		);

		requestRender();
	}

	@Override
	public void paint(@NotNull GraphicsContext gc, CanvasContext canvasContext) {
		boolean preview = paintPreview(canvasContext);
		if (preview) {
			blinkControlHandler.paint(gc);
		}

		if (isEnabled()) {
			//won't draw shadow is not enabled
			Paint old = gc.getStroke();
			gc.setStroke(colorShadow);
			int w = (int) (getWidth() * offsetX);
			int h = (int) (getHeight() * offsetY);
			Region.fillRectangle(gc, x1 + w, y1 + h, x2 + w, y2 + h);
			gc.setStroke(old);
		}

		if (preview) {
			Color oldBgColor = this.backgroundColor;
			Color oldTextColor = textRenderer.getTextColor();
			if (!this.isEnabled()) {
				//set background color to the disabled color
				setBackgroundColor(colorBackgroundDisabled);
				textRenderer.setTextColor(colorDisabled);
			} else {
				if (this.mouseOver) {
					//if the mouse is over this control, set the background color to backgroundColorActive
					setBackgroundColor(colorBackgroundActive);
				} else if (focusedControlRenderer == this) {
					double ratio = focusedColorAlternator.updateAndGetRatio();
					setBackgroundColor(
							ColorHelper.transition(ratio, colorFocused, colorFocused2 == null ? backgroundColor : colorFocused2)
					);
				}
			}

			super.paint(gc, canvasContext);
			textRenderer.paint(gc);

			//reset the colors again
			setBackgroundColor(oldBgColor);
			textRenderer.setTextColor(oldTextColor);
		} else {
			super.paint(gc, canvasContext);
			textRenderer.paint(gc);
		}

		if (preview && this.mouseOver)

		{
			canvasContext.paintLast(tooltipRenderFunc);
		}

	}

	@NotNull
	public Color getTextColor() {
		return textRenderer.getTextColor();
	}

}
