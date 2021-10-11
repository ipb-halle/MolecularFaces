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
import java.io.StringWriter;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.ConverterException;
import javax.faces.render.FacesRenderer;
import javax.faces.render.Renderer;

import de.ipb_halle.molecularfaces.util.RendererUtils;
import de.ipb_halle.molecularfaces.util.ResourceLoader;

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
		String iframeId = clientId + "_Iframe";
		String editorTargetDivId = clientId + "_OpenVectorEditor";

		ResponseWriter writer = context.getResponseWriter();

		// surrounding <div>
		writer.startElement("div", plugin);
		writer.writeAttribute("id", clientId, null);

		encodeHiddenInput(context, writer, plugin, hiddenInputId);
		encodeIframe(context, writer, plugin, editorTargetDivId, iframeId, hiddenInputId);

		// end of surrounding <div>
		writer.endElement("div");
	}

	private void encodeIframe(FacesContext context, ResponseWriter writer, OpenVectorEditorCore plugin, String editorTargetDivId,
			String iframeId, String hiddenInputId) throws IOException {
		writer.startElement("iframe", plugin);
		writer.writeAttribute("id", iframeId, null);
		writer.writeAttribute("style", "border:none;", null);
		writer.writeAttribute("onload", generateOnloadJSCode(plugin, editorTargetDivId, iframeId, hiddenInputId), null);
		writer.writeAttribute("srcdoc", generateIframeSrcdocHTML(context, plugin, editorTargetDivId), null);
		writer.writeText("Your browser does not support iframes.", null);
		writer.endElement("iframe");
	}

	private String generateIframeSrcdocHTML(FacesContext context, OpenVectorEditorCore plugin, String editorTargetDivId) throws IOException {
		StringBuilder sb = new StringBuilder(256);

		sb.append("<html><head>");
		sb.append(renderResourceComponentsFromResourcesFacet(context, plugin));
		sb.append("</head><body>");

		// inner <div> is used for the plugin rendering (aka the JavaScript target)
		sb.append("<div id=\"").append(editorTargetDivId).append("\"></div>");

		sb.append("</body></html>");

		return sb.toString();
	}

	private String renderResourceComponentsFromResourcesFacet(FacesContext context, OpenVectorEditorCore plugin) throws IOException {
		List<UIComponent> childrenFromFacet = getChildrenFromFacet(ResourceLoader.FACET_NAME, plugin);
		if (childrenFromFacet.isEmpty()) {
			return "";
		}

		ResponseWriter originalResponseWriter = context.getResponseWriter();
		StringWriter stringWriter = new StringWriter();
		/*
		 * Where to get a ResponseWriter implementation without inclusion of a specific
		 * JSF implementation? I think, someone anticipated my weird use case. THIS IS
		 * GREAT!
		 */
		ResponseWriter newWriter = originalResponseWriter.cloneWithWriter(stringWriter);

		// Exchange ResponseWriter while encoding the children.
		context.setResponseWriter(newWriter);

		renderChildren(context, childrenFromFacet);

		// Switch back to original ResponseWriter.
		context.setResponseWriter(originalResponseWriter);

		return stringWriter.toString();
	}

	private List<UIComponent> getChildrenFromFacet(String facetName, UIComponent component) {
		UIComponent facet = component.getFacet(facetName);
		if (facet == null) {
			return Collections.emptyList();
		} else {
			return facet.getChildren();
		}
	}

	private void renderChildren(FacesContext context, List<UIComponent> children) throws IOException {
		for (UIComponent comp : children) {
			comp.encodeAll(context);
		}
	}

	private void encodeHiddenInput(FacesContext context, ResponseWriter writer, OpenVectorEditorCore plugin,
			String hiddenInputId) throws IOException {
		String value = RendererUtils.convertValueToString(context, plugin, plugin.getValue());

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

	private String generateOnloadJSCode(OpenVectorEditorCore plugin, String editorTargetDivId, String iframeId, String hiddenInputId) {
		StringBuilder sb = new StringBuilder(512);

		// resource loading
		sb.append(plugin.getResourceLoader().encodeLoadExtResources(loaderJSVar));

		Formatter fmt = new Formatter(sb);

		// Register a JS variable if required.
		String widgetVar = plugin.getWidgetVar();
		if ((widgetVar != null) && (!widgetVar.isEmpty())) {
			fmt.format("var %s = ", widgetVar);
		}

		// after resource loading finished
		fmt.format("%s.status().then(() => {", loaderJSVar);

		/*
		 * Start the editor, set the sequence value after a String-to-JSON
		 * transformation..
		 */
		fmt.format("let valueAsText = document.getElementById(\"%s\").getAttribute(\"value\");", hiddenInputId);
		fmt.format("let valueAsJSON = {};");
		fmt.format("if (!(!valueAsText || valueAsText.length === 0)) {");
		fmt.format("try { valueAsJSON = JSON.parse(valueAsText); }");
		fmt.format("catch(e) { console.error(\"%s\" + e); }", "Could not parse JSON input: ");
		fmt.format("}");
		fmt.format("let editorPromise = molecularfaces.OpenVectorEditor"
				+ ".newEditor(\"%s\", \"%s\", valueAsJSON, %b);",
				editorTargetDivId, iframeId, plugin.isReadonly());

		/*
		 * Register an on-change callback to fill the value of the hidden <input>
		 * element. Perform a JSON-to-String transformation on the sequence data before.
		 */
		fmt.format("editorPromise.then(editor => "
				+ "editor.getOnChangeSubject().addChangeCallback((sequence) => { "
				+ "document.getElementById(\"%s\").setAttribute(\"value\", JSON.stringify(sequence)); }));",
				hiddenInputId);

		fmt.close();

		// Return the editor object embedded in another Promise that is written into widgetVar.
		sb.append("return editorPromise;");

		// end of then() block of the ResourcesLoader's Promise
		sb.append("});");

		return sb.toString();
	}
}