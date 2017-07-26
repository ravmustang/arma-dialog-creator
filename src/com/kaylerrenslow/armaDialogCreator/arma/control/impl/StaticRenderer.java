package com.kaylerrenslow.armaDialogCreator.arma.control.impl;

import com.kaylerrenslow.armaDialogCreator.arma.control.ArmaControl;
import com.kaylerrenslow.armaDialogCreator.arma.control.ArmaControlRenderer;
import com.kaylerrenslow.armaDialogCreator.arma.control.impl.utility.*;
import com.kaylerrenslow.armaDialogCreator.arma.util.ArmaResolution;
import com.kaylerrenslow.armaDialogCreator.arma.util.Texture;
import com.kaylerrenslow.armaDialogCreator.control.ControlProperty;
import com.kaylerrenslow.armaDialogCreator.control.ControlPropertyLookup;
import com.kaylerrenslow.armaDialogCreator.control.ControlStyle;
import com.kaylerrenslow.armaDialogCreator.control.sv.*;
import com.kaylerrenslow.armaDialogCreator.expression.Env;
import com.kaylerrenslow.armaDialogCreator.gui.uicanvas.CanvasContext;
import com.kaylerrenslow.armaDialogCreator.gui.uicanvas.Region;
import com.kaylerrenslow.armaDialogCreator.util.ValueListener;
import com.kaylerrenslow.armaDialogCreator.util.ValueObserver;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 A renderer for {@link StaticControl}

 @author Kayler
 @since 05/25/2016 */
public class StaticRenderer extends ArmaControlRenderer {

	private enum RenderType {
		Text, ImageOrTexture, Line, Frame, Error
	}

	private BlinkControlHandler blinkControlHandler;
	private ControlProperty styleProperty;
	private ControlProperty textProperty;

	private BasicTextRenderer textRenderer;
	private TooltipRenderer tooltipRenderer;

	private final Function<GraphicsContext, Void> tooltipRenderFunc = gc -> {
		tooltipRenderer.paint(gc, this.mouseOverX, this.mouseOverY);
		return null;
	};

	private ImageOrTextureHelper pictureOrTextureHelper = new ImageOrTextureHelper(this);
	private RenderType renderType = RenderType.Text;
	private SerializableValue styleValue = null;
	private RenderType renderTypeForStyle = RenderType.Error;
	private boolean keepImageAspectRatio = false, tileImage = false;
	private int tileW = 0, tileH = 0;

	public StaticRenderer(ArmaControl control, ArmaResolution resolution, Env env) {
		super(control, resolution, env);
		textRenderer = new BasicTextRenderer(control, this,
				ControlPropertyLookup.TEXT, ControlPropertyLookup.COLOR_TEXT,
				ControlPropertyLookup.STYLE, ControlPropertyLookup.SIZE_EX,
				ControlPropertyLookup.SHADOW
		);

		textRenderer.setAllowMultiLine(true);

		{
			ControlProperty colorBackground = myControl.findProperty(ControlPropertyLookup.COLOR_BACKGROUND);
			addValueListener(colorBackground.getPropertyLookup(), (observer, oldValue, newValue) -> {
				if (newValue instanceof SVColor) {
					getBackgroundColorObserver().updateValue((SVColor) newValue);
				}
			});
			colorBackground.setValueIfAbsent(true, new SVColorArray(getBackgroundColor()));
		}

		myControl.findProperty(ControlPropertyLookup.COLOR_TEXT).setValueIfAbsent(true, new SVColorArray(getTextColor()));

		myControl.findProperty(ControlPropertyLookup.TILE_H).getValueObserver().addListener((observer, oldValue,
																							 newValue) -> {
			if (newValue instanceof SVExpression) {
				SVExpression expr = (SVExpression) newValue;
				tileH = (int) expr.getNumVal();
				requestRender();
			}
		});
		myControl.findProperty(ControlPropertyLookup.TILE_W).getValueObserver().addListener((observer, oldValue,
																							 newValue) -> {
			if (newValue instanceof SVExpression) {
				SVExpression expr = (SVExpression) newValue;
				tileW = (int) expr.getNumVal();
				requestRender();
			}
		});

		styleProperty = myControl.findProperty(ControlPropertyLookup.STYLE);
		styleProperty.getValueObserver().addListener((observer, oldValue, newValue) -> {
			if (newValue instanceof SVControlStyleGroup) {
				SVControlStyleGroup group = (SVControlStyleGroup) newValue;
				keepImageAspectRatio = group.hasStyle(ControlStyle.KEEP_ASPECT_RATIO);
				tileImage = group.hasStyle(ControlStyle.TILE_PICTURE);
			}
			renderTypeForStyle = getRenderTypeFromStyle();
			styleValue = newValue;
			checkAndSetRenderType();
		});

		textProperty = myControl.findProperty(ControlPropertyLookup.TEXT);

		textProperty.setValueIfAbsent(true, SVString.newEmptyString());
		textProperty.getValueObserver().addListener(new ValueListener<SerializableValue>() {
			@Override
			public void valueUpdated(@NotNull ValueObserver<SerializableValue> observer, @Nullable SerializableValue oldValue, @Nullable SerializableValue newValue) {
				checkAndSetRenderType();
			}
		});

		myControl.findProperty(ControlPropertyLookup.FONT).setValueIfAbsent(true, SVFont.DEFAULT);
		blinkControlHandler = new BlinkControlHandler(this, ControlPropertyLookup.BLINKING_PERIOD);

		tooltipRenderer = new TooltipRenderer(
				this.myControl, this,
				ControlPropertyLookup.TOOLTIP_COLOR_SHADE,
				ControlPropertyLookup.TOOLTIP_COLOR_TEXT,
				ControlPropertyLookup.TOOLTIP_COLOR_BOX,
				ControlPropertyLookup.TOOLTIP
		);

		renderTypeForStyle = getRenderTypeFromStyle();
		checkAndSetRenderType();
	}

	private void checkAndSetRenderType() {
		switch (renderTypeForStyle) {
			case ImageOrTexture: {
				pictureOrTextureHelper.updateAsync(textProperty.getValue());
			}
		}
		renderType = renderTypeForStyle;
		requestRender();
	}

	public void paint(@NotNull GraphicsContext gc, CanvasContext canvasContext) {
		boolean preview = paintPreview(canvasContext);
		if (preview) {
			blinkControlHandler.paint(gc);
		}
		switch (renderType) {
			case Text: {
				super.paint(gc, canvasContext);
				textRenderer.paint(gc);
				break;
			}
			case Line: {
				//draw line from top left of control to bottom right of control
				//the text color is the color of the line
				gc.setStroke(getTextColor());
				gc.strokeLine(getLeftX(), getTopY(), getRightX(), getBottomY());
				break;
			}
			case Frame: {
				gc.setStroke(getTextColor());

				int controlWidth = getWidth();

				int textWidth = 0;
				double padding = controlWidth * .02;
				int xLeftOfText = (int) Math.round(x1 + padding);

				//draw the text, if the length is > 0
				if (textRenderer.getText().length() > 0) {
					textWidth = textRenderer.getTextWidth();
					if (textWidth < controlWidth - (2 * padding)) {
						//text will paint within the bounds of the frame
						textRenderer.paint(gc, xLeftOfText, y1 - textRenderer.getTextLineHeight() / 2);
					} else {
						//don't paint any text if the text is longer than the frame's width
						textWidth = 0;
					}
				}


				//draw the frame itself

				//draw top line
				if (textWidth > 0) {
					//in Arma 3, the top line is crisp, while the other lines are blurred and 2 pixels in width
					// and the top line is crisp only when there is text
					Region.strokeLine(gc, x1, y1, xLeftOfText, y1);
					Region.strokeLine(gc, xLeftOfText + textWidth, y1, x2, y1);
				} else {
					gc.strokeLine(x1, y1, x2, y1);
				}
				//+0.5 to make the line start crisp
				gc.strokeLine(x2, y1 + 0.5, x2, y2); //right line
				gc.strokeLine(x1, y2, x2, y2); //bottom line
				gc.strokeLine(x1, y1 + 0.5, x1, y2); //left line

				break;
			}
			case ImageOrTexture: {
				switch (pictureOrTextureHelper.getMode()) {
					case Image: {
						Image imageToPaint = pictureOrTextureHelper.getImage();
						if (imageToPaint == null) {
							throw new IllegalStateException("imageToPaint is null");
						}
						int imageDrawX1, imageDrawY1, imageDrawX2, imageDrawY2;
						if (keepImageAspectRatio && !tileImage) {
							int imgWidth = (int) imageToPaint.getWidth();
							int imgHeight = (int) imageToPaint.getHeight();
							double aspectRatio = imgWidth * 1.0 / imgHeight;

							//We want to make sure that the image doesn't surpass the bounds of the control
							//while also maintaining the aspect ratio. In arma 3, the height of the image will
							//never surpass the height of the control. The width is allowed to surpass the bounds though.

							int drawHeight = getHeight();
							int drawWidth = (int) Math.round(drawHeight * aspectRatio);

							//after the image as been resized to aspect ratio, center the image
							int centerX = getX1() + (getWidth() - drawWidth) / 2;

							imageDrawX1 = centerX;
							imageDrawY1 = y1;
							imageDrawX2 = centerX + drawWidth;
							imageDrawY2 = y1 + drawHeight;
							paintMultiplyColor(gc, imageDrawX1, imageDrawY1, imageDrawX2, imageDrawY2, getTextColor());

							gc.drawImage(imageToPaint, imageDrawX1, imageDrawY1, drawWidth, drawHeight);
						} else {
							imageDrawX1 = x1;
							imageDrawY1 = y1;
							imageDrawX2 = x2;
							imageDrawY2 = y2;

							paintMultiplyColor(gc, imageDrawX1, imageDrawY1, imageDrawX2, imageDrawY2, getTextColor());

							if (tileImage) {
								int tileW = Math.max(1, this.tileW);
								int tileH = Math.max(1, this.tileH);
								int controlWidth = getWidth();
								int controlHeight = getHeight();
								int tileWidth = controlWidth / tileW;
								int tileHeight = controlHeight / tileH;

								for (int y = 0; y < tileH; y++) {
									for (int x = 0; x < tileW; x++) {
										gc.drawImage(imageToPaint, x1 + x * tileWidth, y1 + y * tileHeight, tileWidth, tileHeight);
									}
								}
							} else {
								gc.drawImage(imageToPaint, imageDrawX1, imageDrawY1, getWidth(), getHeight());
							}
						}

						break;
					}
					case Texture: {
						Texture texture = pictureOrTextureHelper.getTexture();
						if (texture == null) {
							throw new IllegalStateException("texture is null");
						}
						TexturePainter.paint(gc, texture, getTextColor(), x1, y1, x2, y2);
						break;
					}
					case ImageError: {
						paintImageError(gc, x1, y1, getWidth(), getHeight());
						break;
					}
					case TextureError: {
						paintTextureError(gc, x1, y1, getWidth(), getHeight());
						break;
					}
					case LoadingImage: {
						super.paint(gc, canvasContext);
						break;
					}
				}

				break;
			}
			case Error: {
				paintBackgroundColorError(gc, x1, y1, getWidth(), getHeight());
				break;
			}
			default: {
				throw new IllegalStateException("unhandled renderType:" + renderType);
			}
		}
		if (preview) {
			if (this.mouseOver) {
				canvasContext.paintLast(tooltipRenderFunc);
			}
		}
	}


	@NotNull
	public Color getTextColor() {
		return textRenderer.getTextColor();
	}

	/**
	 @return the {@link RenderType} to use, or {@link #renderTypeForStyle}
	 if the style value didn't change
	 */
	@NotNull
	private RenderType getRenderTypeFromStyle() {
		SerializableValue value = styleProperty.getValue();
		if (value == null) {
			return RenderType.Error;
		}
		if (value == styleValue) {
			return renderTypeForStyle;
		}
		if (value instanceof SVControlStyleGroup) {
			SVControlStyleGroup group = (SVControlStyleGroup) value;
			for (ControlStyle style : group.getStyleArray()) {
				if (style == ControlStyle.TILE_PICTURE) {
					return RenderType.ImageOrTexture;
				}
				if (style == ControlStyle.PICTURE) {
					return RenderType.ImageOrTexture;
				}
				if (style == ControlStyle.LINE) {
					return RenderType.Line;
				}
				if (style == ControlStyle.FRAME) {
					return RenderType.Frame;
				}
			}
		}
		return RenderType.Text;
	}

}
