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
package de.ipb_halle.molecularfaces.component.molplugin;

import jakarta.faces.component.UIInput;
import de.ipb_halle.molecularfaces.util.ResourceLoader;

/**
 * This class holds the attribute states of the chemical structure plugins.
 * 
 * @author flange
 */
public abstract class MolPluginCore extends UIInput {
	/**
	 * Component family returned by {@link #getFamily()}
	 */
	public static final String COMPONENT_FAMILY = "molecularfaces.MolPluginFamily";

	private ResourceLoader resourceLoader = new ResourceLoader(this);

	protected MolPluginCore() {
		resourceLoader.addScriptResourceToHead("js/MolecularFaces.min.js");
	}

	protected ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	@Override
	public String getFamily() {
		return COMPONENT_FAMILY;
	}

	/**
	 * Supported chemical structure plugin types.
	 * 
	 * @author flange
	 */
	public enum PluginType {
		OpenChemLibJS, MolPaintJS, MarvinJS;

		@Override
		public String toString() {
			return this.name();
		}
	}

	/**
	 * Possible values for the <code>format</code> property.
	 */
	public enum Format {
		MDLV2000, MDLV3000;

		@Override
		public String toString() {
			return this.name();
		}
	};

	protected enum PropertyKeys {
		border, format, height, readonly, widgetVar, width;
	}

	/**
	 * Return the value of the <code>border</code> property.
	 * <p>
	 * Flag indicating that this element is rendered with a border.
	 * 
	 * @return the value of the attribute or <code>false</code> if it has not been
	 *         set in the JSF view.
	 */
	public boolean isBorder() {
		return (boolean) getStateHelper().eval(PropertyKeys.border, false);
	}

	/**
	 * Set the value of the <code>border</code> property.
	 * 
	 * @param border boolean value which indicates that the plugin component renders
	 *               surrounded by a border
	 */
	public void setBorder(boolean border) {
		getStateHelper().put(PropertyKeys.border, border);
	}

	public static final String DEFAULT_FORMAT = Format.MDLV2000.toString();

	/**
	 * Return the value of the <code>format</code> property.
	 * <p>
	 * Controls the chemical file format to be used in the <code>value</code>
	 * property.
	 * <p>
	 * Possible values are provided by the {@link Format} enumeration.
	 * 
	 * @return the value of the attribute or "MDLV2000" if it has not been set in
	 *         the JSF view.
	 */
	public String getFormat() {
		return (String) getStateHelper().eval(PropertyKeys.format, DEFAULT_FORMAT);
	}

	/**
	 * Set the value of the <code>format</code> property.
	 * 
	 * @param format chemical file format
	 */
	public void setFormat(String format) {
		getStateHelper().put(PropertyKeys.format, format);
	}

	public static final int DEFAULT_HEIGHT = 400;

	/**
	 * Return the value of the <code>height</code> property.
	 * <p>
	 * The height of the plugin in pixels.
	 * 
	 * @return the value of the attribute or 400 if it has not been set in the JSF
	 *         view.
	 */
	public int getHeight() {
		return (int) getStateHelper().eval(PropertyKeys.height, DEFAULT_HEIGHT);
	}

	/**
	 * Set the value of the <code>height</code> property.
	 * 
	 * @param height height of the rendered plugin
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
	 * @return the value of the attribute or <code>false</code> if it has not been
	 *         set in the JSF view.
	 */
	public boolean isReadonly() {
		return (boolean) getStateHelper().eval(PropertyKeys.readonly, false);
	}

	/**
	 * Set the value of the <code>readonly</code> property.
	 * 
	 * @param readonly boolean value which indicates if the plugin component renders
	 *                 an editor or a viewer
	 */
	public void setReadonly(boolean readonly) {
		getStateHelper().put(PropertyKeys.readonly, readonly);
	}

	/**
	 * Return the value of the <code>widgetVar</code> property.
	 * <p>
	 * The client-side variable name of a Promise object that embeds the plugin's
	 * JavaScript instance.
	 * 
	 * @return the value of the attribute.
	 */
	public String getWidgetVar() {
		return (String) getStateHelper().eval(PropertyKeys.widgetVar);
	}

	/**
	 * Set the value of the <code>widgetVar</code> property.
	 * 
	 * @param widgetVar name of the client-side Promise object that embeds the
	 *                  plugin's JavaScript instance
	 */
	public void setWidgetVar(String widgetVar) {
		getStateHelper().put(PropertyKeys.widgetVar, widgetVar);
	}

	public static final int DEFAULT_WIDTH = 400;

	/**
	 * Return the value of the <code>width</code> property.
	 * <p>
	 * The width of the plugin in pixels.
	 * 
	 * @return the value of the attribute or 400 if it has not been set in the JSF
	 *         view.
	 */
	public int getWidth() {
		return (int) getStateHelper().eval(PropertyKeys.width, DEFAULT_WIDTH);
	}

	/**
	 * Set the value of the <code>width</code> property.
	 * 
	 * @param width width of the rendered plugin
	 */
	public void setWidth(int width) {
		getStateHelper().put(PropertyKeys.width, width);
	}
}
