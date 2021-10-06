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

import javax.faces.component.FacesComponent;

import de.ipb_halle.molecularfaces.util.WebXml;
import de.ipb_halle.molecularfaces.util.WebXmlImpl;

/**
 * This {@link javax.faces.component.UIComponent} renders a
 * <a href="https://github.com/TeselaGen/openVectorEditor">Teselagen's Open
 * Source Vector/Plasmid Editor Component</a>.
 * 
 * @author flange
 */
@FacesComponent(OpenVectorEditorComponent.COMPONENT_TYPE)
public class OpenVectorEditorComponent extends OpenVectorEditorCore {
	public static final String COMPONENT_TYPE = "molecularfaces.OpenVectorEditor";
	public static final String DEFAULT_RENDERER = OpenVectorEditorRenderer.RENDERER_TYPE;

	private WebXml webXml = new WebXmlImpl();

	/**
	 * Name of the context-param in web.xml that specifies the location of the
	 * OpenVectorEditor UMD installation relative to the application's context root.
	 * There should be at least open-vector-editor.min.js and main.css.
	 */
	public static final String WEBXML_CUSTOM_RESOURCE_BASE_URL = "de.ipb_halle.molecularfaces.OPENVECTOREDITOR_BASE_URL";

	public OpenVectorEditorComponent() {
		super();

		String resourceBaseUrl = webXml.getContextParam(WEBXML_CUSTOM_RESOURCE_BASE_URL, getFacesContext(), null);
		if ((resourceBaseUrl != null) && (!resourceBaseUrl.isEmpty())) {
			getResourceLoader().addScriptExtToBodyAtTop(resourceBaseUrl + "/open-vector-editor.min.js");
			getResourceLoader().addCssExt(resourceBaseUrl + "/main.css");
		} else {
			getResourceLoader().addScriptResourceToBodyAtTop("plugins/openVectorEditor/open-vector-editor.min.js");
			getResourceLoader().addCssResource("plugins/openVectorEditor/main.css");
		}
		getResourceLoader().addScriptResourceToHead("js/MolecularFaces.min.js");

		setRendererType(DEFAULT_RENDERER);
	}
}