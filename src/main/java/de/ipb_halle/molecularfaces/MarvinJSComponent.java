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

/**
 * This {@link javax.faces.component.UIComponent} renders a chemical structure
 * editor or viewer using the
 * <a href="https://chemaxon.com/products/marvin-js">Marvin JS</a> JavaScript
 * plugin.
 * 
 * @author flange
 */
@FacesComponent(MarvinJSComponent.COMPONENT_TYPE)
public class MarvinJSComponent extends MolPluginCore {
	public static final String COMPONENT_TYPE = "molecularfaces.MarvinJS";
	public static final String DEFAULT_RENDERER = MarvinJSRenderer.RENDERER_TYPE;

	/**
	 * Name of the context-param in web.xml that specifies the location of the
	 * extracted Marvin JS archive (marvinjs-version-core.zip) relative to the
	 * application's context root.
	 */
	public static final String WEBXML_MARVINJS_BASE_URL = "de.ipb_halle.molecularfaces.MARVINJS_BASE_URL";

	/**
	 * Name of the context-param in web.xml that specifies the location of Marvin
	 * JS' license file (marvin4js-license.cxl) relative to
	 * {@link WEBXML_MARVINJS_BASE_URL}.
	 */
	public static final String WEBXML_MARVINJS_LICENSE_URL = "de.ipb_halle.molecularfaces.MARVINJS_LICENSE_URL";

	public MarvinJSComponent() {
		super();

		/*
		 * Always include Marvin JS via external resources defined by
		 * WEBXML_MARVINJS_BASE_URL.
		 */
		String baseDir = getFacesContext().getExternalContext().getInitParameter(WEBXML_MARVINJS_BASE_URL);
		AddResourcesListener.addScriptExt(baseDir + "/gui/lib/promise-1.0.0.min.js");
		AddResourcesListener.addScriptExt(baseDir + "/js/marvinjslauncher.js");

		setRendererType(DEFAULT_RENDERER);
	}

	@Override
	public String getFamily() {
		return COMPONENT_FAMILY;
	}
}