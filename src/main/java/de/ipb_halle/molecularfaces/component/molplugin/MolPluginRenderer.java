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

import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.render.Renderer;

/**
 * This {@link Renderer} offers functionalities for the specific renderers of
 * {@link MolPluginCore}-derived molecular structure plugin components.
 * 
 * @author flange
 */
public abstract class MolPluginRenderer extends Renderer {
	@Override
	public void decode(FacesContext context, UIComponent component) {
		Map<String, String> requestMap = context.getExternalContext().getRequestParameterMap();
		MolPluginCore plugin = (MolPluginCore) component;

		if (!plugin.isReadonly()) {
			String clientId = plugin.getClientId(context);

			String value = requestMap.get(clientId);

			plugin.setSubmittedValue(value);
		}
	}

	@Override
	public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue)
			throws ConverterException {
		if ((context == null) || (component == null)) {
			throw new NullPointerException();
		}

		if (submittedValue instanceof String) {
			if (component instanceof MolPluginCore) {
				Converter converter = ((MolPluginCore) component).getConverter();

				if (converter != null) {
					return converter.getAsObject(context, component, (String) submittedValue);
				}
			}
		}

		return submittedValue;
	}

	/**
	 * Converts a component's value to a string by invoking the component's
	 * converter's {@link Converter#getAsString(FacesContext, UIComponent, Object)}
	 * method. If no converter is defined, it just returns the value.
	 * 
	 * @param context {@link FacesContext} for the request we are processing
	 * @param component component of the molecular structure plugin to be encoded
	 * @return converted value
	 */
	protected String getValueAsString(FacesContext context, MolPluginCore component) {
		if ((context == null) || (component == null)) {
			throw new NullPointerException();
		}

		Converter converter = component.getConverter();

		if (converter != null) {
			return converter.getAsString(context, component, component.getValue());
		}

		return (String) component.getValue();
	}

	/**
	 * Generate the inline css style for the &lt;div&gt; element of the rendered
	 * editor or viewer.
	 * 
	 * @param plugin component of the molecular structure plugin
	 * @return inline css style
	 */
	protected static String generateDivStyle(MolPluginCore plugin) {
		StringBuilder sb = new StringBuilder(128);

		// width attribute
		sb.append("width:").append(plugin.getWidth()).append("px;");

		// height attribute
		sb.append("height:").append(plugin.getHeight()).append("px;");

		// border attribute
		if (plugin.isBorder()) {
			sb.append("border:solid;border-width:1px;");
		}

		return sb.toString();
	}
}