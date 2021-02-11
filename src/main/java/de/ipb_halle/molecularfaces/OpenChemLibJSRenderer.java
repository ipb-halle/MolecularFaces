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
 * This {@link javax.faces.render.Renderer} renders a chemical structure editor
 * or viewer using the
 * <a href="https://github.com/cheminfo/openchemlib-js">OpenChemLib JS</a>
 * Javascript plugin.
 * 
 * @author flange
 */
@FacesRenderer(rendererType = OpenChemLibJSRenderer.RENDERER_TYPE, componentFamily = MolPluginCore.COMPONENT_FAMILY)
public class OpenChemLibJSRenderer extends Renderer {
	public static final String RENDERER_TYPE = "molecularfaces.OpenChemLibJSRenderer";

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

		if (plugin.isReadonly()) {
			encodeViewer(writer, plugin);
		} else {
			encodeEditor(writer, plugin);
		}

		// end of surrounding <div>
		writer.endElement("div");
	}

	private void encodeViewer(ResponseWriter writer, MolPluginCore plugin) throws IOException {
		String divId = plugin.getClientId() + "_OpenChemLibJSViewer";

		encodeViewerHTML(writer, plugin, divId);
		encodeViewerJS(writer, plugin, divId);
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
	 * @param writer
	 * @param plugin
	 * @param divId  DOM id of the &lt;div&gt; element
	 */
	private void encodeViewerJS(ResponseWriter writer, MolPluginCore plugin, String divId) throws IOException {
		writer.startElement("script", plugin);
		writer.writeAttribute("type", "text/javascript", null);

		StringBuilder sb = new StringBuilder(128);
		sb.append("new molecularfaces.OpenChemLibJSViewer(\"").append(divId).append("\",\"").append(escape((String) plugin.getValue()))
				.append("\",").append(plugin.getHeight()).append(",").append(plugin.getWidth()).append(");");

		writer.writeText(sb, null);
		writer.endElement("script");
	}

	private void encodeEditor(ResponseWriter writer, MolPluginCore plugin) throws IOException {
		String clientId = plugin.getClientId();
		String hiddenInputId = clientId + "_Input";
		String divId = clientId + "_OpenChemLibJSEditor";

		encodeEditorHTML(writer, plugin, divId, hiddenInputId);
		encodeEditorJS(writer, plugin, divId, hiddenInputId);
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
	private void encodeEditorHTML(ResponseWriter writer, MolPluginCore plugin, String divId, String hiddenInputId)
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
	 * @param writer
	 * @param plugin
	 * @param divId         DOM id of the &lt;div&gt; element
	 * @param hiddenInputId DOM id of the hidden &lt;input&gt; element
	 */
	private void encodeEditorJS(ResponseWriter writer, MolPluginCore plugin, String divId, String hiddenInputId)
			throws IOException {
		writer.startElement("script", plugin);
		writer.writeAttribute("type", "text/javascript", null);

		StringBuilder sb = new StringBuilder(512);

		// Start editor and set the molecule from the hidden <input> element's value.
		sb.append("new molecularfaces.OpenChemLibJSEditor(\"").append(divId).append("\").setMol(document.getElementById(\"")
				.append(hiddenInputId).append("\").getAttribute(\"value\"))");

		/*
		 * Register an on-change callback to fill the value of the hidden <input>
		 * element.
		 */
		sb.append(".addChangeListener(function(mol) { document.getElementById(\"").append(hiddenInputId)
				.append("\").setAttribute(\"value\", mol); });");

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