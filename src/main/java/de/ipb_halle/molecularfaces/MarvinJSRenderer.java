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

import java.io.IOException;
import java.util.Formatter;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;

/**
 * This {@link javax.faces.render.Renderer} renders a chemical structure editor
 * or viewer using the <a href="https://chemaxon.com/products/marvin-js">Marvin
 * JS</a> Javascript plugin.
 * 
 * @author flange
 */
@FacesRenderer(rendererType = MarvinJSRenderer.RENDERER_TYPE, componentFamily = MolPluginCore.COMPONENT_FAMILY)
public class MarvinJSRenderer extends Renderer {
	public static final String RENDERER_TYPE = "molecularfaces.MarvinJSRenderer";

	/**
	 * Name of the JavaScript global variable that represents a common
	 * ResourcesLoader instance for all rendered components of this plugin type.
	 * This variable is defined in MolecularFaces.js.
	 */
	private String loaderJSVar = "molecularfaces.marvinJSLoaderInstance";

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
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
		MolPluginCore plugin = (MolPluginCore) component;

		if (!plugin.isRendered()) {
			return;
		}

		ResponseWriter writer = context.getResponseWriter();

		// surrounding <div>
		writer.startElement("div", plugin);
		writer.writeAttribute("id", plugin.getClientId(), null);
		writer.writeAttribute("style", generateDivStyle(plugin), null);

		if (plugin.isReadonly()) {
			encodeViewer(context, writer, plugin);
		} else {
			encodeEditor(context, writer, plugin);
		}

		// end of surrounding <div>
		writer.endElement("div");
	}

	private void encodeViewer(FacesContext context, ResponseWriter writer, MolPluginCore plugin) throws IOException {
		String divId = plugin.getClientId() + "_MarvinJSViewer";

		encodeViewerHTML(writer, plugin, divId);
		encodeViewerJS(context, writer, plugin, divId);
	}

	/**
	 * Encodes the HTML part of the plugin viewer into the writer. It consists of a
	 * &lt;div&gt; element that the Javascript plugin uses as rendering target.
	 * 
	 * @param writer
	 * @param plugin
	 * @param divId  DOM id of the embedded &lt;div&gt; element
	 */
	private void encodeViewerHTML(ResponseWriter writer, MolPluginCore plugin, String divId) throws IOException {
		// inner <div> is used for the plugin's rendering (aka the Javascript target)
		writer.startElement("div", plugin);
		writer.writeAttribute("id", divId, null);
		writer.writeAttribute("style", generateDivStyle(plugin), null);
		writer.endElement("div");
	}

	/**
	 * Encodes the Javascript part of the plugin viewer into the writer.
	 * 
	 * @param context
	 * @param writer
	 * @param plugin
	 * @param divId   DOM id of the &lt;div&gt; element
	 */
	private void encodeViewerJS(FacesContext context, ResponseWriter writer, MolPluginCore plugin, String divId)
			throws IOException {
		String escapedMolecule = escape((String) plugin.getValue());

		writer.startElement("script", plugin);
		writer.writeAttribute("type", "text/javascript", null);

		String installPath = context.getExternalContext().getInitParameter(MarvinJSComponent.WEBXML_MARVINJS_BASE_URL);
		if (installPath == null) {
			installPath = "";
		}

		StringBuilder sb = new StringBuilder(512 + installPath.length() + escapedMolecule.length());
		sb.append(plugin.encodeLoadExtResources(loaderJSVar));

		Formatter fmt = new Formatter(sb);

		// Register a JS variable if required.
		String widgetVar = plugin.getWidgetVar();
		if ((widgetVar != null) && (!widgetVar.isEmpty())) {
			fmt.format("var %s = null;", widgetVar);
		}

		fmt.format("%s.onLoad(function () {", loaderJSVar);

		if ((widgetVar != null) && (!widgetVar.isEmpty())) {
			fmt.format("%s = ", widgetVar);
		}
		fmt.format("new molecularfaces.MarvinJSViewer(\"%s\", \"%s\", \"%s\", %d, %d);", divId, escapedMolecule,
				installPath, plugin.getHeight(), plugin.getWidth());

		fmt.close();

		// end of onLoad
		sb.append("});");

		writer.writeText(sb, null);
		writer.endElement("script");
	}

	private void encodeEditor(FacesContext context, ResponseWriter writer, MolPluginCore plugin) throws IOException {
		String clientId = plugin.getClientId();
		String hiddenInputId = clientId + "_Input";
		String iframeId = clientId + "_MarvinJSEditor";

		encodeEditorHTML(context, writer, plugin, iframeId, hiddenInputId);
		encodeEditorJS(context, writer, plugin, iframeId, hiddenInputId);
	}

	/**
	 * Encodes the HTML part of the plugin editor into the writer. It consists of an
	 * &lt;iframe&gt; and a hidden &lt;input&gt; element.
	 * 
	 * @param context
	 * @param writer
	 * @param plugin
	 * @param iframeId      DOM id of the embedded &lt;iframe&gt; element
	 * @param hiddenInputId DOM id of the embedded hidden &lt;input&gt; element
	 */
	private void encodeEditorHTML(FacesContext context, ResponseWriter writer, MolPluginCore plugin, String iframeId,
			String hiddenInputId) throws IOException {
		// inner <iframe> used for the plugin's rendering (aka the JavaScript target)
		writer.startElement("iframe", plugin);
		writer.writeAttribute("id", iframeId, null);
		writer.writeAttribute("src",
				context.getExternalContext().getInitParameter(MarvinJSComponent.WEBXML_MARVINJS_BASE_URL)
						+ "/editor.html",
				null);
		writer.writeAttribute("style", "height:" + plugin.getHeight() + "px;width:" + plugin.getWidth() + "px;", null);
		writer.endElement("iframe");

		// hidden <input>
		writer.startElement("input", plugin);
		writer.writeAttribute("type", "hidden", null);
		writer.writeAttribute("id", hiddenInputId, null);
		writer.writeAttribute("name", plugin.getClientId(), null);
		writer.writeAttribute("value", plugin.getValue(), "value");
		writer.endElement("input");
	}

	/**
	 * Encodes the JavaScript part of the plugin editor into the writer.
	 * <p>
	 * Note: Different components of this plugin type will use one and the same
	 * JavaScript variable, which will be overwritten in case it already exists.
	 * 
	 * @param context
	 * @param writer
	 * @param plugin
	 * @param iframeId      DOM id of the &lt;iframe&gt; element
	 * @param hiddenInputId DOM id of the hidden &lt;input&gt; element
	 */
	private void encodeEditorJS(FacesContext context, ResponseWriter writer, MolPluginCore plugin, String iframeId,
			String hiddenInputId) throws IOException {
		writer.startElement("script", plugin);
		writer.writeAttribute("type", "text/javascript", null);

		String installPath = context.getExternalContext().getInitParameter(MarvinJSComponent.WEBXML_MARVINJS_BASE_URL);
		if (installPath == null) {
			installPath = "";
		}
		String license = context.getExternalContext().getInitParameter(MarvinJSComponent.WEBXML_MARVINJS_LICENSE_URL);
		if (license == null) {
			license = "";
		}

		StringBuilder sb = new StringBuilder(512 + installPath.length() + license.length());
		sb.append(plugin.encodeLoadExtResources(loaderJSVar));

		Formatter fmt = new Formatter(sb);

		// Register a JS variable if required.
		String widgetVar = plugin.getWidgetVar();
		if ((widgetVar != null) && (!widgetVar.isEmpty())) {
			fmt.format("var %s = null;", widgetVar);
		}

		fmt.format("%s.onLoad(function () {", loaderJSVar);

		if ((widgetVar != null) && (!widgetVar.isEmpty())) {
			fmt.format("%s = ", widgetVar);
		}

		// Start editor and set the molecule from the hidden <input> element's value.
		fmt.format(
				"new molecularfaces.MarvinJSEditor(\"%s\", document.getElementById(\"%s\").getAttribute(\"value\"), \"%s\", \"%s\", %d, %d)",
				iframeId, hiddenInputId, installPath, license, plugin.getHeight(), plugin.getWidth());

		/*
		 * Register an on-change callback to fill the value of the hidden <input>
		 * element.
		 */
		fmt.format(
				".addChangeListener(function(mol) { document.getElementById(\"%s\").setAttribute(\"value\", mol); });",
				hiddenInputId);

		fmt.close();

		// end of onLoad
		sb.append("});");

		writer.writeText(sb, null);
		writer.endElement("script");
	}

	private String generateDivStyle(MolPluginCore plugin) {
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

	/**
	 * Escape string for HTML / XML. Used mainly for molecules in MDL MOL format.
	 * 
	 * @param s string to escape
	 * @return escaped string
	 */
	private String escape(String s) {
		if (s == null) {
			return "";
		}

		return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
	}
}