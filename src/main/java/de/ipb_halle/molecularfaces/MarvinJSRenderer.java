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
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;

/**
 * This renderer renders a chemical structure editor or viewer using the
 * <a href="https://chemaxon.com/products/marvin-js">Marvin JS</a> Javascript
 * plugin.
 * 
 * @author flange
 */
@FacesRenderer(rendererType = MarvinJSRenderer.RENDERER_TYPE, componentFamily = UIMolPlugin.COMPONENT_FAMILY)
public class MarvinJSRenderer extends Renderer {
	public static final String RENDERER_TYPE = "molecularfaces.MarvinJSRenderer";
	/**
	 * Location of the extracted Marvin JS archive (marvinjs-version-core.zip).
	 */
	public static final String WEBXML_MARVINJS_BASE_URL = "de.ipb_halle.molecularfaces.MARVINJS_BASE_URL";

	/**
	 * Location of Marvin JS' license file (marvin4js-license.cxl) relative to
	 * WEBXML_MARVINJS_BASE_URL.
	 */
	public static final String WEBXML_MARVINJS_LICENSE_URL = "de.ipb_halle.molecularfaces.MARVINJS_LICENSE_URL";

	@Override
	public void decode(FacesContext context, UIComponent component) {
		Map<String, String> requestMap = context.getExternalContext().getRequestParameterMap();
		UIMolPlugin plugin = (UIMolPlugin) component;

		if (!plugin.isReadonly()) {
			String clientId = plugin.getClientId(context);

			String value = requestMap.get(clientId);

			plugin.setSubmittedValue(value);
			plugin.setValid(true);
		}
	}

	@Override
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
		UIMolPlugin plugin = (UIMolPlugin) component;

		if (!plugin.isRendered()) {
			return;
		}

		ResponseWriter writer = context.getResponseWriter();

		// surrounding <div>
		writer.startElement("div", plugin);
		writer.writeAttribute("id", plugin.getClientId(), null);
		writer.writeAttribute("style", generateDivStyle(plugin), null);

		/*
		 * Always include Marvin JS via external resources defined by
		 * WEBXML_MARVINJS_BASE_URL.
		 */
		encodeIncludeCustomResourceUrl(writer, context, plugin);

		if (plugin.isReadonly()) {
			encodeViewer(context, writer, plugin);
		} else {
			encodeEditor(context, writer, plugin);
		}

		// end of surrounding <div>
		writer.endElement("div");
	}

	private void encodeIncludeCustomResourceUrl(ResponseWriter writer, FacesContext context, UIMolPlugin plugin)
			throws IOException {
		String baseDir = context.getExternalContext().getInitParameter(WEBXML_MARVINJS_BASE_URL);

		// include gui/lib/promise-1.0.0.min.js
		writer.startElement("script", plugin);
		writer.writeAttribute("type", "text/javascript", null);
		writer.writeAttribute("src", baseDir + "/gui/lib/promise-1.0.0.min.js", null);
		writer.endElement("script");

		// include js/marvinjslauncher.js
		writer.startElement("script", plugin);
		writer.writeAttribute("type", "text/javascript", null);
		writer.writeAttribute("src", baseDir + "/js/marvinjslauncher.js", null);
		writer.endElement("script");
	}

	private void encodeViewer(FacesContext context, ResponseWriter writer, UIMolPlugin plugin) throws IOException {
		String iframeId = plugin.getClientId() + "_MarvinJSViewer";

		encodeViewerJS(writer, plugin, iframeId);
		encodeViewerHTML(context, writer, plugin, iframeId);
	}

	/**
	 * Encodes the HTML part of the plugin viewer into the writer. It consists of a
	 * hidden &lt;iframe&gt; element that the JavaScript plugin uses as target. The
	 * JavaScript execution dynamically attaches SVG content to the surrounding
	 * &lt;div&gt;.
	 * 
	 * @param context
	 * @param writer
	 * @param plugin
	 * @param iframeId DOM id of the embedded &lt;iframe&gt; element
	 */
	private void encodeViewerHTML(FacesContext context, ResponseWriter writer, UIMolPlugin plugin, String iframeId)
			throws IOException {
		// inner <iframe> is used for the plugin's rendering (aka the JavaScript target)
		writer.startElement("iframe", plugin);
		writer.writeAttribute("id", iframeId, null);
		writer.writeAttribute("src",
				context.getExternalContext().getInitParameter(WEBXML_MARVINJS_BASE_URL) + "/marvinpack.html", null);
		// hide the <iframe>
		writer.writeAttribute("style", "width:0;height:0;display:initial;position:absolute;left:0;"
				+ "top:0;margin:0;padding:0;border:0;border:none;", null);
		writer.endElement("iframe");
	}

	/**
	 * Encodes the JavaScript part of the plugin viewer into the writer.
	 * <p>
	 * Note: Different components of this plugin type will use one and the same
	 * JavaScript variable, which will be overwritten in case it already exists.
	 * 
	 * @param writer
	 * @param plugin
	 * @param iframeId DOM id of the &lt;iframe&gt; element
	 */
	private void encodeViewerJS(ResponseWriter writer, UIMolPlugin plugin, String iframeId) throws IOException {
		String jsVariableName = "marvinJSViewer";

		writer.startElement("script", plugin);
		writer.writeAttribute("type", "text/javascript", null);

		StringBuilder sb = new StringBuilder(1024);

		sb.append(jsOnDocumentReady());
		sb.append("var ").append(jsVariableName).append(";");

		/*
		 * This is mostly JavaScript code from
		 * https://marvinjs-demo.chemaxon.com/latest/examples/example-create-image.html.
		 */
		sb.append("onDocumentReady(function (e) {" + "MarvinJSUtil.getPackage(\"#").append(iframeId)
				.append("\").then(function (marvinNameSpace) {" + "marvinNameSpace.onReady(function() {");

		// set JS variable
		sb.append(jsVariableName).append(" = marvinNameSpace;");

		// delegate image creation to another function
		sb.append("exportImage();");

		sb.append("});" + "},function (error) {"
				+ "alert(\"Cannot retrieve MarvinJS viewer instance from iframe:\"+error);" + "});" + "});");

		// exportImage function
		sb.append("function exportImage() {");

		// settings for image rendering
		sb.append("var settings = {'width':").append(plugin.getWidth()).append(",").append("'height':")
				.append(plugin.getHeight()).append(",").append("'zoomMode':\"autoshrink\"").append("};");

		// create SVG data from the plugin's value
		sb.append("var imgData = ").append(jsVariableName).append(".ImageExporter.molToDataUrl(");

		String value = (String) plugin.getValue();
		if (value.isEmpty()) {
			sb.append("null");
		} else {
			sb.append("\"").append(escape(value)).append("\"");
		}

		sb.append(", \"image/svg\", settings);");

		// add SVG data as html element to the outer <div>
		sb.append("document.getElementById(\"").append(plugin.getClientId())
				.append("\").insertAdjacentHTML('beforeend', imgData);" + "}");

		writer.writeText(sb, null);
		writer.endElement("script");
	}

	private String jsOnDocumentReady() {
		// vanilla JavaScript onDocumentReady
		return "function onDocumentReady(fn) {"
				+ "if (document.readyState === \"complete\" || document.readyState === \"interactive\") {"
				+ "setTimeout(fn, 1);" + "} else {" + "document.addEventListener(\"DOMContentLoaded\", fn);" + "}"
				+ "}";
	}

	private void encodeEditor(FacesContext context, ResponseWriter writer, UIMolPlugin plugin) throws IOException {
		String clientId = plugin.getClientId();
		String hiddenInputId = clientId + "_Input";
		String iframeId = clientId + "_MarvinJSEditor";

		encodeEditorJS(context, writer, plugin, iframeId, hiddenInputId);
		encodeEditorHTML(context, writer, plugin, iframeId, hiddenInputId);
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
	private void encodeEditorHTML(FacesContext context, ResponseWriter writer, UIMolPlugin plugin, String iframeId,
			String hiddenInputId) throws IOException {
		// inner <iframe> used for the plugin's rendering (aka the JavaScript target)
		writer.startElement("iframe", plugin);
		writer.writeAttribute("id", iframeId, null);
		writer.writeAttribute("src",
				context.getExternalContext().getInitParameter(WEBXML_MARVINJS_BASE_URL) + "/editor.html", null);
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
	private void encodeEditorJS(FacesContext context, ResponseWriter writer, UIMolPlugin plugin, String iframeId,
			String hiddenInputId) throws IOException {
		String jsVariableName = "marvinJSEditor";

		writer.startElement("script", plugin);
		writer.writeAttribute("type", "text/javascript", null);

		StringBuilder sb = new StringBuilder(1024);

		sb.append(jsOnDocumentReady());
		sb.append("var ").append(jsVariableName).append(";");

		sb.append("onDocumentReady(function (e) {");

		// set location of the license file
		sb.append("MarvinJSUtil.getPackage(\"#").append(iframeId).append("\").then(function (marvinNameSpace) {");
		sb.append("marvinNameSpace.onReady(function() {");
		sb.append("marvinNameSpace.Sketch.license(\"")
				.append(context.getExternalContext().getInitParameter(WEBXML_MARVINJS_LICENSE_URL)).append("\");");
		sb.append("});").append("},function (error) {")
				.append("alert(\"Cannot retrieve Marvin JS instance from iframe:\"+error);").append("});");

		/*
		 * This is mostly JavaScripe code from
		 * https://marvinjs-demo.chemaxon.com/latest/examples/example-setmol.html
		 */
		sb.append("MarvinJSUtil.getEditor(\"#").append(iframeId).append("\").then(function (sketcherInstance) {");

		// set JS variable
		sb.append(jsVariableName).append(" = sketcherInstance;");

		// check if value from hidden <input> element is empty
		sb.append("var value = document.getElementById(\"").append(hiddenInputId)
				.append("\").getAttribute(\"value\");");
		sb.append("if (value === \"\") { value = null; }");

		// set the molecule
		sb.append(jsVariableName)
				.append(".importStructure(\"mol\", value)" + ".catch(function(error) {" + "alert(error);" + "});");

		/*
		 * Register an on-change callback to fill the value of the hidden <input>
		 * element
		 */
		sb.append(jsVariableName).append(".on(\"molchange\", function() { document.getElementById(\"")
				.append(hiddenInputId).append("\").setAttribute(\"value\", ").append(jsVariableName)
				.append(".exportAsMol())});");

		sb.append("},function (error) {" + "alert(\"Cannot retrieve MarvinJS sketcher instance from iframe:\"+error);"
				+ "});" + "});");

		writer.writeText(sb, null);
		writer.endElement("script");
	}

	private String generateDivStyle(UIMolPlugin plugin) {
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