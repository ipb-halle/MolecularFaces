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

import jakarta.faces.component.FacesComponent;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.event.ListenerFor;
import jakarta.faces.event.PostAddToViewEvent;

import de.ipb_halle.molecularfaces.util.WebXml;
import de.ipb_halle.molecularfaces.util.WebXmlImpl;

/**
 * This {@link jakarta.faces.component.UIComponent} renders a chemical structure
 * editor or viewer using the
 * <a href="https://chemaxon.com/products/marvin-js">Marvin JS</a> JavaScript
 * plugin.
 * 
 * @author flange
 */
@ListenerFor(systemEventClass = PostAddToViewEvent.class)
@FacesComponent(MarvinJSComponent.COMPONENT_TYPE)
public class MarvinJSComponent extends MolPluginCore implements ComponentSystemEventListener {
	public static final String COMPONENT_TYPE = "molecularfaces.MarvinJS";
	public static final String DEFAULT_RENDERER = MarvinJSRenderer.RENDERER_TYPE;

	/**
	 * Name of the context-param in web.xml that specifies the location of the
	 * extracted Marvin JS archive (marvinjs-version-core.zip) relative to the
	 * application's context root.
	 */
	public static final String WEBXML_MARVINJS_BASE_URL = "de.ipb_halle.molecularfaces.MARVINJS_BASE_URL";

	/**
	 * Name of the context-param in web.xml that specifies if Marvin JS should use
	 * its webservices. If "true", the &lt;iframe&gt; will embed editorws.html, else
	 * it will embed editor.html. In readonly mode, the component will load
	 * /js/webservices.js relative to {@link #WEBXML_MARVINJS_BASE_URL}.
	 * 
	 * @see <a href=
	 *      "https://marvinjs-demo.chemaxon.com/latest/docs/dev/embed.html">https://marvinjs-demo.chemaxon.com/latest/docs/dev/embed.html</a>
	 */
	public static final String WEBXML_MARVINJS_WEBSERVICES = "de.ipb_halle.molecularfaces.MARVINJS_WEBSERVICES";

	/**
	 * Name of the context-param in web.xml that specifies the location of Marvin
	 * JS' license file (marvin4js-license.cxl) relative to
	 * {@link #WEBXML_MARVINJS_BASE_URL}.
	 */
	public static final String WEBXML_MARVINJS_LICENSE_URL = "de.ipb_halle.molecularfaces.MARVINJS_LICENSE_URL";

	private WebXml webXml = new WebXmlImpl();

	private final String baseDir = webXml.getContextParam(WEBXML_MARVINJS_BASE_URL, getFacesContext(), "");

	public MarvinJSComponent() {
		super();

		/*
		 * Always include Marvin JS via external resources defined by
		 * WEBXML_MARVINJS_BASE_URL.
		 */
		getResourceLoader().addScriptExtToHead(baseDir + "/gui/lib/promise-1.0.0.min.js");
		getResourceLoader().addScriptExtToHead(baseDir + "/js/marvinjslauncher.js");

		setRendererType(DEFAULT_RENDERER);
	}

	@Override
	public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
		if (event instanceof PostAddToViewEvent) {
			processPostAddToViewEvent();
		}

		super.processEvent(event);
	}

	/*
	 * webservices.js needs to be included if we use the viewer.
	 * 
	 * The usual trouble with loading resources dynamically: We cannot use
	 * isReadonly() in the constructor of the component, because the attribute from
	 * the view has not been applied to this component yet. That is why it is done
	 * in the {@link PostAddToViewEvent}.
	 */
	private void processPostAddToViewEvent() {
		boolean useWebServices = webXml.isContextParamTrue(WEBXML_MARVINJS_WEBSERVICES, getFacesContext());
		if (isReadonly() && useWebServices) {
			getResourceLoader().addScriptExtToHead(baseDir + "/js/webservices.js");
		}
	}
}
