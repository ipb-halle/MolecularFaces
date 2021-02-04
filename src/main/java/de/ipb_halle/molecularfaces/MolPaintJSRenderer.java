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
 * <a href="https://github.com/ipb-halle/MolPaintJS">MolPaintJS</a> Javascript
 * plugin.
 * 
 * @author flange
 */
@FacesRenderer(rendererType = MolPaintJSRenderer.RENDERER_TYPE, componentFamily = UIMolPlugin.COMPONENT_FAMILY)
public class MolPaintJSRenderer extends Renderer {
	public static final String RENDERER_TYPE = "molecularfaces.MolPaintJSRenderer";
	public static final String WEBXML_CUSTOM_RESOURCE_URL = "de.ipb_halle.molecularfaces.MOLPAINTJS_URL";

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

		if (useCustomResourceUrl(context)) {
			encodeIncludeCustomResourceUrl(writer, context, plugin);
		}

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
		writer.startElement("link", plugin);
		writer.writeAttribute("type", "text/css", null);
		writer.writeAttribute("rel", "stylesheet", null);
		writer.writeAttribute("href",
				context.getExternalContext().getInitParameter(WEBXML_CUSTOM_RESOURCE_URL) + "/css/styles.css", null);
		writer.endElement("link");

		writer.startElement("script", plugin);
		writer.writeAttribute("type", "text/javascript", null);
		writer.writeAttribute("src",
				context.getExternalContext().getInitParameter(WEBXML_CUSTOM_RESOURCE_URL) + "/js/molpaint.js", null);
		writer.endElement("script");
	}

	private void encodeViewer(FacesContext context, ResponseWriter writer, UIMolPlugin plugin) throws IOException {
		String divId = plugin.getClientId() + "_MolPaintJSViewer";

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
	private void encodeViewerHTML(ResponseWriter writer, UIMolPlugin plugin, String divId) throws IOException {
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
	private void encodeViewerJS(FacesContext context, ResponseWriter writer, UIMolPlugin plugin, String divId)
			throws IOException {
		writer.startElement("script", plugin);
		writer.writeAttribute("type", "text/javascript", null);

		StringBuilder sb = new StringBuilder(512);

		sb.append("new MolPaintJS()").append(".newContext(\"").append(divId).append("\", {installPath:\"")
				.append(context.getExternalContext().getInitParameter(WEBXML_CUSTOM_RESOURCE_URL))
				.append("/\" ,iconSize: 32, sizeX:").append(plugin.getWidth()).append(", sizeY:")
				.append(plugin.getHeight()).append(", viewer:1})");

		// set the molecule
		sb.append(".setMolecule(\"").append(escape((String) plugin.getValue())).append("\")");

		// draw the plugin
		sb.append(".init();");

		writer.writeText(sb, null);
		writer.endElement("script");
	}

	private void encodeEditor(FacesContext context, ResponseWriter writer, UIMolPlugin plugin) throws IOException {
		String clientId = plugin.getClientId();
		String hiddenInputId = clientId + "_Input";
		String divId = clientId + "_MolPaintJSEditor";

		encodeEditorHTML(writer, plugin, divId, hiddenInputId);
		encodeEditorJS(context, writer, plugin, divId, hiddenInputId);
	}

	/**
	 * Encodes the HTML part of the plugin editor into the writer. It consists of a
	 * &lt;div&gt; and a hidden &lt;input&gt; element.
	 * 
	 * @param writer
	 * @param plugin
	 * @param divId         DOM id of the embedded &lt;div&gt; element
	 * @param hiddenInputId DOM id of the embedded hidden &lt;input&gt; element
	 */
	private void encodeEditorHTML(ResponseWriter writer, UIMolPlugin plugin, String divId, String hiddenInputId)
			throws IOException {
		// inner <div> used for the plugin's rendering (aka the Javascript target)
		writer.startElement("div", plugin);
		writer.writeAttribute("id", divId, null);
		writer.writeAttribute("style", generateDivStyle(plugin), null);
		writer.endElement("div");

		// hidden <input>
		writer.startElement("input", plugin);
		writer.writeAttribute("type", "hidden", null);
		writer.writeAttribute("id", hiddenInputId, null);
		writer.writeAttribute("name", plugin.getClientId(), null);
		writer.writeAttribute("value", plugin.getValue(), "value");
		writer.endElement("input");
	}

	/**
	 * Encodes the Javascript part of the plugin editor into the writer.
	 * <p>
	 * Note: Different components of this plugin type will use one and the same
	 * Javascript variable, which will be overwritten in case it already exists.
	 * 
	 * @param context
	 * @param writer
	 * @param plugin
	 * @param divId         DOM id of the &lt;div&gt; element
	 * @param hiddenInputId DOM id of the hidden &lt;input&gt; element
	 */
	private void encodeEditorJS(FacesContext context, ResponseWriter writer, UIMolPlugin plugin, String divId,
			String hiddenInputId) throws IOException {
		String jsPluginVariableName = "molPaintJSPlugin";
		String jsEditorVariableName = "molPaintJSEditor";

		writer.startElement("script", plugin);
		writer.writeAttribute("type", "text/javascript", null);

		StringBuilder sb = new StringBuilder(1024);

		// plugin and editor variables
		sb.append("var ").append(jsPluginVariableName).append(" = new MolPaintJS();");
		sb.append("var ").append(jsEditorVariableName).append(" = ").append(jsPluginVariableName)
				.append(".newContext(\"").append(divId).append("\", {installPath:\"")
				.append(context.getExternalContext().getInitParameter(WEBXML_CUSTOM_RESOURCE_URL))
				.append("/\" ,iconSize: 32, sizeX:").append(plugin.getWidth()).append(", sizeY:")
				.append(plugin.getHeight()).append("})");

		// set the molecule from the hidden <input> element's value
		sb.append(".setMolecule(document.getElementById(\"").append(hiddenInputId)
				.append("\").getAttribute(\"value\"));");

		/*
		 * Register an on-change callback to fill the value of the hidden <input>
		 * element.
		 */
		sb.append("var changeListener = function () { document.getElementById(\"").append(hiddenInputId)
				.append("\").setAttribute(\"value\", ").append(jsPluginVariableName).append(".getMDLv2000(\"")
				.append(divId).append("\")); };");
		sb.append(jsEditorVariableName).append(".setChangeListener(changeListener)");

		// finally, draw the plugin
		sb.append(".init();");

		writer.writeText(sb, null);
		writer.endElement("script");
	}

	private String generateDivStyle(UIMolPlugin plugin) {
		StringBuilder sb = new StringBuilder(128);

		// width attribute
		// sb.append("width:").append(plugin.getWidth()).append("px;");

		// height attribute
		// sb.append("height:").append(plugin.getHeight()).append("px;");

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

	// not yet implemented
	/*
	 * @Override public void addResources(FacesContext context) { if
	 * (!useCustomResourceUrl(context)) { UIOutput js = new UIOutput();
	 * js.setRendererType("javax.faces.resource.Script");
	 * js.getAttributes().put("library", "molecularfaces");
	 * js.getAttributes().put("name", "js/molpaint.js");
	 * context.getViewRoot().addComponentResource(context, js, "head");
	 * 
	 * UIOutput css = new UIOutput();
	 * css.setRendererType("javax.faces.resource.Stylesheet");
	 * css.getAttributes().put("library", "molecularfaces");
	 * css.getAttributes().put("name", "css/styles.css");
	 * context.getViewRoot().addComponentResource(context, css, "head"); } }
	 */

	private boolean useCustomResourceUrl(FacesContext context) {
		String resourceUrl = context.getExternalContext().getInitParameter(WEBXML_CUSTOM_RESOURCE_URL);
		if ((resourceUrl != null) && (!resourceUrl.isEmpty())) {
			return true;
		} else {
			return false;
		}
	}
}