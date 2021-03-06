package com.kaylerrenslow.armaDialogCreator.arma.control;

import com.kaylerrenslow.armaDialogCreator.arma.control.impl.ArmaControlLookup;
import com.kaylerrenslow.armaDialogCreator.arma.util.ArmaResolution;
import com.kaylerrenslow.armaDialogCreator.control.*;
import com.kaylerrenslow.armaDialogCreator.expression.Env;
import com.kaylerrenslow.armaDialogCreator.gui.uicanvas.CanvasControlGroup;
import com.kaylerrenslow.armaDialogCreator.gui.uicanvas.ControlList;
import com.kaylerrenslow.armaDialogCreator.gui.uicanvas.Resolution;
import com.kaylerrenslow.armaDialogCreator.util.ArrayUtil;
import com.kaylerrenslow.armaDialogCreator.util.ReadOnlyList;
import org.jetbrains.annotations.NotNull;

/**
 Generic implementation of a control that can house many controls. This is not the implementation for control type 15 (CT_CONTROLS_GROUP).

 @author Kayler
 @since 06/08/2016. */
public class ArmaControlGroup extends ArmaControl implements CanvasControlGroup<ArmaControl> {
	private final ControlList<ArmaControl> controlsList = new ControlList<>(this);

	public static final ArmaControlSpecRequirement SPEC_PROVIDER = new SpecReq();

	public ArmaControlGroup(@NotNull ControlClassSpecification specification, @NotNull ArmaControlLookup lookup,
							@NotNull ArmaResolution resolution, @NotNull Env env, @NotNull SpecificationRegistry registry) {
		super(specification, lookup, resolution, env, registry);
	}

	public ArmaControlGroup(@NotNull String name, @NotNull ArmaControlLookup lookup, @NotNull ArmaResolution resolution,
							@NotNull Env env, @NotNull SpecificationRegistry registry) {
		super(name, lookup, resolution, env, registry);
	}

	@Override
	public ControlList<ArmaControl> getControls() {
		return controlsList;
	}

	@Override
	public void resolutionUpdate(@NotNull Resolution newResolution) {
		super.resolutionUpdate(newResolution);
		for (ArmaControl control : controlsList) {
			control.resolutionUpdate(newResolution);
		}
	}

	private static class SpecReq implements ArmaControlSpecRequirement, AllowedStyleProvider {

		@NotNull
		@Override
		public ReadOnlyList<ControlPropertyLookupConstant> getRequiredProperties() {
			return new ReadOnlyList<>(
					ArrayUtil.mergeArrays(ControlPropertyLookupConstant.class,
							defaultRequiredProperties, new ControlPropertyLookup[]{
							}));
		}

		@NotNull
		@Override
		public ReadOnlyList<ControlPropertyLookupConstant> getOptionalProperties() {
			return new ReadOnlyList<>(
					ArrayUtil.mergeArrays(ControlPropertyLookupConstant.class,
							defaultOptionalProperties, new ControlPropertyLookup[]{
							}));

		}

		@NotNull
		@Override
		public ControlStyle[] getAllowedStyles() {
			return ControlStyle.NONE.getStyleGroup().getStyleArray();
		}
	}
}
