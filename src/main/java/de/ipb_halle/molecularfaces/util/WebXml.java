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

import jakarta.faces.context.FacesContext;

/**
 * This interface offers methods to obtain information from web.xml.
 * 
 * @author flange
 */
public interface WebXml {
	/**
	 * Obtains the {@code param-value} for the {@code context-param} given by
	 * {@code param-name} via the given {@link FacesContext} instance.
	 * 
	 * @param paramName    {@code param-name} of the {@code context-param} element
	 *                     in web.xml
	 * @param context      {@link FacesContext} instance
	 * @param defaultValue default value to return if the {@code context-param} does
	 *                     not exist
	 * @return {@code param-value} or {@code defaultValue}
	 */
	public String getContextParam(String paramName, FacesContext context, String defaultValue);

	/**
	 * Obtains the {@code param-value} for the {@code context-param} given by
	 * {@code param-name} via the current {@link FacesContext} instance.
	 * 
	 * @param paramName    {@code param-name} of the {@code context-param} element
	 *                     in web.xml
	 * @param defaultValue default value to return if the {@code context-param} does
	 *                     not exist
	 * @return {@code param-value} or {@code defaultValue}
	 */
	default public String getContextParam(String paramName, String defaultValue) {
		return getContextParam(paramName, FacesContext.getCurrentInstance(), defaultValue);
	}

	/**
	 * Parses the {@code param-value} for the {@code context-param} given by
	 * {@code param-name} via the given {@link FacesContext} instance and returns
	 * {@code true} if the {@code param-value} is equal, ignoring case, to the
	 * string "true".
	 * 
	 * @param paramName {@code param-name} of the {@code context-param} element in
	 *                  web.xml
	 * @param context   {@link FacesContext} instance
	 * @return {@code true} if the context-param's value is true
	 */
	default public boolean isContextParamTrue(String paramName, FacesContext context) {
		return Boolean.parseBoolean(getContextParam(paramName, context, "false"));
	}

	/**
	 * Parses the {@code param-value} for the {@code context-param} given by
	 * {@code param-name} via the current {@link FacesContext} instance and returns
	 * {@code true} if the {@code param-value} is equal, ignoring case, to the
	 * string "true".
	 * 
	 * @param paramName {@code param-name} of the {@code context-param} element in
	 *                  web.xml
	 * @return {@code true} if the context-param's value is true
	 */
	default public boolean isContextParamTrue(String paramName) {
		return isContextParamTrue(paramName, FacesContext.getCurrentInstance());
	}
}
