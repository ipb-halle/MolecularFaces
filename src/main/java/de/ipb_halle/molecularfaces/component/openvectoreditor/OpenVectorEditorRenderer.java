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

import java.io.IOException;
import java.util.Formatter;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.ConverterException;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;

import de.ipb_halle.molecularfaces.util.RendererUtils;

/**
 * This {@link javax.faces.render.Renderer} renders the HTML and JavaScript code
 * of a {@link OpenVectorEditorComponent} using the
 * <a href="https://github.com/TeselaGen/openVectorEditor">Teselagen's Open
 * Source Vector/Plasmid Editor Component</a>.
 * 
 * @author flange
 */
@FacesRenderer(rendererType = OpenVectorEditorRenderer.RENDERER_TYPE, componentFamily = OpenVectorEditorCore.COMPONENT_FAMILY)
public class OpenVectorEditorRenderer extends Renderer {
	public static final String RENDERER_TYPE = "molecularfaces.OpenVectorEditorRenderer";

	/**
	 * Name of the JavaScript global variable that represents a common
	 * ResourcesLoader instance for all rendered components of this plugin type.
	 * This variable is defined in MolecularFaces.js.
	 */
	private String loaderJSVar = "molecularfaces.openVectorEditorLoaderInstance";

	@Override
	public void decode(FacesContext context, UIComponent component) {
		OpenVectorEditorCore plugin = (OpenVectorEditorCore) component;

		if (!plugin.isReadonly()) {
			RendererUtils.decodeComponent(context, plugin);
		}
	}

	@Override
	public Object getConvertedValue(FacesContext context, UIComponent component, Object submittedValue)
			throws ConverterException {
		return RendererUtils.convertSubmittedValueToObject(context, component, submittedValue);
	}

	@Override
	public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
		OpenVectorEditorCore plugin = (OpenVectorEditorCore) component;

		if (!plugin.isRendered()) {
			return;
		}
		
		String clientId = plugin.getClientId();
		String hiddenInputId = clientId + "_Input";
		String divId = clientId + "_OpenVectorEditor";

		ResponseWriter writer = context.getResponseWriter();

		// surrounding <div>
		writer.startElement("div", plugin);
		writer.writeAttribute("id", clientId, null);

		encodeHTML(context, writer, plugin, divId, hiddenInputId);
		encodeJS(writer, plugin, divId, hiddenInputId);

		// end of surrounding <div>
		writer.endElement("div");
	}
	
	private void encodeHTML(FacesContext context, ResponseWriter writer, OpenVectorEditorCore plugin, String divId,
			String hiddenInputId) throws IOException {
		// inner <div> is used for the plugin rendering (aka the JavaScript target)
		writer.startElement("div", plugin);
		writer.writeAttribute("id", divId, null);
		writer.endElement("div");

		String value = RendererUtils.convertValueToString(context, plugin, plugin.getValue());

		// hidden <input>
		writer.startElement("input", plugin);
		writer.writeAttribute("type", "hidden", null);
		writer.writeAttribute("id", hiddenInputId, null);

		if (plugin.isReadonly()) {
			// "name" attribute is not rendered (prevents submission).
		} else {
			writer.writeAttribute("name", plugin.getClientId(), null);
		}

		writer.writeAttribute("value", value, "value");
		writer.endElement("input");
	}

	private void encodeJS(ResponseWriter writer, OpenVectorEditorCore plugin, String divId, String hiddenInputId) throws IOException {
		String jsCode = generateJSCode(plugin, divId, hiddenInputId);
		writeScriptTag(jsCode, plugin, writer);
	}
	
	private void writeScriptTag(String jsCode, OpenVectorEditorCore plugin, ResponseWriter writer) throws IOException {
		writer.startElement("script", plugin);
		writer.writeAttribute("type", "text/javascript", null);
		writer.writeText(jsCode, null);
		writer.endElement("script");
	}

	private String generateJSCode(OpenVectorEditorCore plugin, String divId, String hiddenInputId) {
		StringBuilder sb = new StringBuilder(512);

		// resource loading
		sb.append(plugin.getResourceLoader().encodeLoadExtResources(loaderJSVar));

		Formatter fmt = new Formatter(sb);

		// Register a JS variable if required.
		String widgetVar = plugin.getWidgetVar();
		if ((widgetVar != null) && (!widgetVar.isEmpty())) {
			fmt.format("var %s = ", widgetVar);
		}

		/*
		 * Start the editor, set the sequence value after a String-to-JSON
		 * transformation and return the editor object embedded in a Promise.
		 */
		fmt.format("%s.status().then(() => {", loaderJSVar);
		fmt.format("let valueAsText = document.getElementById(\"%s\").getAttribute(\"value\");", hiddenInputId);
		fmt.format("let valueAsJSON = {};");
		fmt.format("if (!(!valueAsText || valueAsText.length === 0)) {");
		fmt.format("try { valueAsJSON = JSON.parse(valueAsText); }");
		fmt.format("catch(e) { console.error(\"%s\" + e); }", "Could not parse JSON input: ");
		fmt.format("}");
		fmt.format(
				"return molecularfaces.OpenVectorEditor.newEditor(\"%s\", valueAsJSON, %b)",
				divId, plugin.isReadonly());

		/*
		 * Register an on-change callback to fill the value of the hidden <input>
		 * element. Perform a JSON-to-String transformation on the sequence data before.
		 */
		fmt.format(
				".then((editor) => editor.getOnChangeSubject().addChangeCallback("
						+ "(sequence) => { document.getElementById(\"%s\").setAttribute(\"value\", JSON.stringify(sequence)); }));",
				hiddenInputId);

		fmt.close();

		// end of then()
		sb.append("});");

		return sb.toString();
	}
}