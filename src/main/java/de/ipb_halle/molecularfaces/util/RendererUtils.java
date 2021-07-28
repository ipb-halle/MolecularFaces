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
package de.ipb_halle.molecularfaces.util;

import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.render.Renderer;

/**
 * Utility class that supplies convenient functionalities to be used by
 * {@link Renderer}s.
 * 
 * @author flange
 */
public class RendererUtils {
	private RendererUtils() {
	}

	/**
	 * Extracts the value of the given component from the request parameter map and
	 * sets it as submitted value for this component.
	 * 
	 * @param context   {@link FacesContext} for the request we are processing
	 * @param component component to be decoded
	 */
	public static void decodeComponent(FacesContext context, UIInput component) {
		Map<String, String> requestMap = context.getExternalContext().getRequestParameterMap();
		String clientId = component.getClientId(context);
		String value = requestMap.get(clientId);
		component.setSubmittedValue(value);
	}

	/**
	 * Attempt to convert the submitted value using the given component's converter.
	 * 
	 * @param context        {@link FacesContext} for the request we are processing
	 * @param component      component being decoded
	 * @param submittedValue submitted value to be converted
	 * @return converted value
	 * @throws ConverterException   if the submitted value cannot be converted
	 *                              successfully
	 * @throws NullPointerException if {@code context} or {@code component} is
	 *                              {@code null}
	 */
	public static Object convertSubmittedValueToObject(FacesContext context, UIComponent component,
			Object submittedValue) throws ConverterException {
		if ((context == null) || (component == null)) {
			throw new NullPointerException();
		}

		if ((submittedValue instanceof String) && (component instanceof UIInput)) {
			UIInput inputComponent = (UIInput) component;
			Converter converter = inputComponent.getConverter();

			if (converter != null) {
				return converter.getAsObject(context, inputComponent, (String) submittedValue);
			}
		}

		// fallback
		return submittedValue;
	}

	/**
	 * Attempt to convert the value to a String using the given component's
	 * converter.
	 * 
	 * @param context   {@link FacesContext} for the request we are processing
	 * @param component component being encoded
	 * @param value     value to be converted
	 * @return converted value
	 * @throws ConverterException   if the value cannot be converted successfully
	 * @throws NullPointerException if {@code context} or {@code component} is
	 *                              {@code null}
	 */
	public static String convertValueToString(FacesContext context, UIInput component, Object value)
			throws ConverterException {
		if ((context == null) || (component == null)) {
			throw new NullPointerException();
		}

		Converter converter = component.getConverter();

		if (converter != null) {
			return converter.getAsString(context, component, value);
		} else {
			return (String) value;
		}
	}
}