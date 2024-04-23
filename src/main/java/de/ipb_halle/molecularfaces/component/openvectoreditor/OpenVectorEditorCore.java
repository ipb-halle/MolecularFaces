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
package de.ipb_halle.molecularfaces.component.openvectoreditor;

import jakarta.faces.component.UIInput;
import de.ipb_halle.molecularfaces.util.ResourceLoader;

/**
 * This class holds the attribute states of the &lt;mol:openVectorEditor&gt; component.
 * 
 * @author flange
 */
public abstract class OpenVectorEditorCore extends UIInput {
	/**
	 * Component family returned by {@link #getFamily()}
	 */
	public static final String COMPONENT_FAMILY = "molecularfaces.OpenVectorEditorFamily";

	/**
	 * An empty protein sequence as JSON string that may be used to initiate the
	 * OpenVectorEditor in protein sequence mode.
	 */
	public static final String EMPTY_PROTEIN_SEQUENCE_JSON = "{\"isProtein\":true}";

	private ResourceLoader resourceLoader = new ResourceLoader(this);

	protected ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	@Override
	public String getFamily() {
		return COMPONENT_FAMILY;
	}

	protected enum PropertyKeys {
		readonly, widgetVar;
	}

	/**
	 * Return the value of the <code>readonly</code> property.
	 * <p>
	 * Flag indicating that this element is in editable or
	 * in read-only mode.
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
	 * @param readonly boolean value which indicates the rendering of the component in read-only mode
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
}
