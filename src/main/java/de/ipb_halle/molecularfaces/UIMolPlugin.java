/*
 * MolecularFaces
 * Copyright 2021 Leibniz-Institut für Pflanzenbiochemie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package de.ipb_halle.molecularfaces;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.ListenerFor;
import javax.faces.event.PhaseId;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.render.Renderer;

import org.omnifaces.util.Events;

/**
 * This {@link javax.faces.component.UIComponent} renders a chemical structure
 * editor or viewer. Rendering is delegated to different {@link Renderer}s
 * depending on the choice of the <code>pluginType</code> property.
 * 
 * @author flange
 */
@ListenerFor(systemEventClass = PostAddToViewEvent.class)
@FacesComponent(UIMolPlugin.COMPONENT_TYPE)
public class UIMolPlugin extends UIInput implements ComponentSystemEventListener {
	public static final String COMPONENT_TYPE = "molecularfaces.UIMolPlugin";
	public static final String COMPONENT_FAMILY = "molecularfaces.UIMolPluginFamily";
	public static final String DEFAULT_RENDERER_TYPE = OpenChemLibJSRenderer.RENDERER_TYPE;

	public UIMolPlugin() {
		super();
		setRendererType(DEFAULT_RENDERER_TYPE);

		Events.subscribeToViewBeforePhase(PhaseId.RENDER_RESPONSE, () -> {
			updateRenderer();
			addResources();
		});
	}

	@Override
	public String getFamily() {
		return COMPONENT_FAMILY;
	}

	protected enum PropertyKeys {
		border, /* format, */ height, pluginType, readonly, width;

		String toString;

		PropertyKeys(String toString) {
			this.toString = toString;
		}

		PropertyKeys() {
		}

		public String toString() {
			return ((toString != null) ? toString : super.toString());
		}
	}

	/**
	 * Return the value of the <code>border</code> property.
	 * <p>
	 * Flag indicating that this element is rendered with a border.
	 * 
	 * @return Returns the value of the attribute or <code>false</code> if it has
	 *         not been set in the JSF view.
	 */
	public boolean isBorder() {
		return (boolean) getStateHelper().eval(PropertyKeys.border, false);
	}

	/**
	 * Set the value of the <code>border</code> property.
	 * 
	 * @param border
	 */
	public void setBorder(boolean border) {
		getStateHelper().put(PropertyKeys.border, border);
	}

	// format is not yet implemented in the renderers!
	/*
	 * Possible values for the <code>format</code> property.
	 */
	// public static final String[] FORMATS = { "molFile", "molFileV3", "smiles" };

	/*
	 * Return the value of the <code>format</code> property. <p> Controls the
	 * chemical file format to be used in the <code>value</code> property. <p>
	 * Possible values: "molFile" (MDL Molfile V2000), "molFileV3" (MDL Molfile
	 * V3000) and "smiles" (SMILES).
	 * 
	 * @return Returns the value of the attribute.
	 */
	/*
	 * public String getFormat() { return (String)
	 * getStateHelper().eval(PropertyKeys.format); }
	 */

	/*
	 * Set the value of the <code>format</code> property.
	 */
	/*
	 * public void setFormat(String format) {
	 * getStateHelper().put(PropertyKeys.format, format); }
	 */

	public static final int DEFAULT_HEIGHT = 400;

	/**
	 * Return the value of the <code>height</code> property.
	 * <p>
	 * The height of the structure editor plugin in pixels.
	 * 
	 * @return Returns the value of the attribute or 400 if it has not been set in
	 *         the JSF view.
	 */
	public int getHeight() {
		return (int) getStateHelper().eval(PropertyKeys.height, DEFAULT_HEIGHT);
	}

	/**
	 * Set the value of the <code>height</code> property.
	 * 
	 * @param height
	 */
	public void setHeight(int height) {
		getStateHelper().put(PropertyKeys.height, height);
	}

	/**
	 * Possible values for the <code>pluginType</code> property.
	 */
	public static final String[] PLUGIN_TYPES = { "OpenChemLibJS", "MarvinJS" };

	/**
	 * Return the value of the <code>pluginType</code> property.
	 * <p>
	 * Controls the type of the structure editor plugin.
	 * <p>
	 * Possible values: "OpenChemLibJS", "MarvinJS"
	 * 
	 * @return Returns the value of the attribute.
	 */
	public String getPluginType() {
		return (String) getStateHelper().eval(PropertyKeys.pluginType);
	}

	/**
	 * Set the value of the <code>pluginType</code> property.
	 * 
	 * @param pluginType
	 */
	public void setPluginType(String pluginType) {
		getStateHelper().put(PropertyKeys.pluginType, pluginType);
	}

	/**
	 * Return the value of the <code>readonly</code> property.
	 * <p>
	 * Flag indicating that this element is in editable (full structure editor) or
	 * in view-only mode.
	 * 
	 * @return Returns the value of the attribute or <code>false</code> if it has
	 *         not been set in the JSF view.
	 */
	public boolean isReadonly() {
		return (boolean) getStateHelper().eval(PropertyKeys.readonly, false);
	}

	/**
	 * Set the value of the <code>readonly</code> property.
	 * 
	 * @param readonly
	 */
	public void setReadonly(boolean readonly) {
		getStateHelper().put(PropertyKeys.readonly, readonly);
	}

	public static final int DEFAULT_WIDTH = 400;

	/**
	 * Return the value of the <code>width</code> property.
	 * <p>
	 * The width of the structure editor plugin in pixels.
	 * 
	 * @return Returns the value of the attribute or 400 if it has not been set in
	 *         the JSF view.
	 */
	public int getWidth() {
		return (int) getStateHelper().eval(PropertyKeys.width, DEFAULT_WIDTH);
	}

	/**
	 * Set the value of the <code>width</code> property.
	 * 
	 * @param width
	 */
	public void setWidth(int width) {
		getStateHelper().put(PropertyKeys.width, width);
	}

	@Override
	public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
		if (event instanceof PostAddToViewEvent) {
			addResources();
		}
		super.processEvent(event);
	}

	/*
	 * We would like to load resources programmatically (not via
	 * the @ResourceDependencies annotation). This has to be done before the render
	 * response by calling this method in the following events:
	 * 
	 * (1) in the PostAddToViewEvent of this component (see BalusC's comment in
	 * https://stackoverflow.com/a/12451778),
	 * 
	 * (2) before the "Render Response" phase with the help of an OmniFaces hook.
	 * This solves issues when an EL value expression is used for the "pluginType"
	 * attribute (see comment of {@link updateRenderer}).
	 * 
	 * The actual logic that adds resources is implemented in the specific renderer.
	 */
	private void addResources() {
		FacesContext context = getFacesContext();

		/*
		 * Our Renderer should implement the AddResourceRenderer interface. Adding the
		 * resources is delegated there.
		 */
		Renderer renderer = getRenderer(context);
		if (renderer instanceof AddResourceRenderer) {
			((AddResourceRenderer) renderer).addResources(context);
		}
	}

	/*
	 * It is not sufficient to track changes of the attribute "pluginType" via its
	 * setter, because this method is not called if an EL value expression is
	 * involved. Thus, the renderer delegation depending on the plugin type is set
	 * up before the "Render Response" phase using an OmniFaces hook.
	 */
	private void updateRenderer() {
		switch (getPluginType()) {
		case "OpenChemLibJS":
			setRendererType(OpenChemLibJSRenderer.RENDERER_TYPE);
			break;
		case "MarvinJS":
			setRendererType(MarvinJSRenderer.RENDERER_TYPE);
			break;
		default:
			throw new IllegalArgumentException("Unknown plugin type: " + getPluginType());
		}
	}
}