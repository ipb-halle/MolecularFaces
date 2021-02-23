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

import java.util.Formatter;
import java.util.HashSet;
import java.util.Set;

import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.ListenerFor;
import javax.faces.event.PostAddToViewEvent;

/**
 * This class holds the attribute states of the chemical structure plugins.
 * 
 * @author flange
 */
@ListenerFor(systemEventClass = PostAddToViewEvent.class)
public abstract class MolPluginCore extends UIInput implements ComponentSystemEventListener {
	private static final String RESOURCES_LIBRARY_NAME = "molecularfaces";

	public static final String COMPONENT_FAMILY = "molecularfaces.MolPluginFamily";

	public enum PluginType {
		OpenChemLibJS, MolPaintJS, MarvinJS;

		@Override
		public String toString() {
			return this.name();
		}
	}

	protected enum PropertyKeys {
		border, /* format, */ height, readonly, widgetVar, width;
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

	/**
	 * Return the value of the <code>widgetVar</code> property.
	 * <p>
	 * The client-side JavaScript variable of the plugin.
	 * 
	 * @return Returns the value of the attribute.
	 */
	public String getWidgetVar() {
		return (String) getStateHelper().eval(PropertyKeys.widgetVar);
	}

	/**
	 * Set the value of the <code>widgetVar</code> property.
	 * 
	 * @param widgetVar
	 */
	public void setWidgetVar(String widgetVar) {
		getStateHelper().put(PropertyKeys.widgetVar, widgetVar);
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

	private Set<String> scriptResourcesToLoad = new HashSet<>();

	protected void addScriptResource(String resource) {
		scriptResourcesToLoad.add(resource);
	}

	private Set<String> scriptsExtToLoad = new HashSet<>();

	protected void addScriptExt(String src) {
		scriptsExtToLoad.add(src);
	}

	private Set<String> cssResourcesToLoad = new HashSet<>();

	protected void addCssResource(String resource) {
		cssResourcesToLoad.add(resource);
	}

	private Set<String> cssExtToLoad = new HashSet<>();

	protected void addCssExt(String href) {
		cssExtToLoad.add(href);
	}

	@Override
	public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
		if (event instanceof PostAddToViewEvent) {
			for (String resource : scriptResourcesToLoad) {
				/*
				 * Create an UIComponent that includes the file as script from the resource
				 * library.
				 */
				UIOutput jsResource = new UIOutput();
				jsResource.setRendererType("javax.faces.resource.Script");
				jsResource.getAttributes().put("library", RESOURCES_LIBRARY_NAME);
				jsResource.getAttributes().put("name", resource);

				// Add the component to <head>.
				getFacesContext().getViewRoot().addComponentResource(getFacesContext(), jsResource, "head");
			}

			for (String resource : cssResourcesToLoad) {
				/*
				 * Create an UIComponent that includes the file from the resource library.
				 */
				UIOutput cssResource = new UIOutput();
				cssResource.setRendererType("javax.faces.resource.Stylesheet");
				cssResource.getAttributes().put("library", RESOURCES_LIBRARY_NAME);
				cssResource.getAttributes().put("name", resource);

				// Add the component to <head>.
				getFacesContext().getViewRoot().addComponentResource(getFacesContext(), cssResource, "head");
			}
		}

		super.processEvent(event);
	}

	public StringBuilder encodeLoadExtResources(String loaderJSVar) {
		StringBuilder sb = new StringBuilder();
		Formatter fmt = new Formatter(sb);

		sb.append(loaderJSVar);
		for (String script : scriptsExtToLoad) {
			fmt.format(".addScriptToHead(\"%s\")", script);
		}
		for (String href : cssExtToLoad) {
			fmt.format(".addCssToHead(\"%s\")", href);
		}
		sb.append(".loadResources();");

		fmt.close();
		return sb;
	}
}