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

import static de.ipb_halle.molecularfaces.util.ResourceLoader.JAVASCRIPT;
import static de.ipb_halle.molecularfaces.util.ResourceLoader.JAVASCRIPT_FACET_NAME;
import static de.ipb_halle.molecularfaces.util.ResourceLoader.STYLESHEET;
import static de.ipb_halle.molecularfaces.util.ResourceLoader.STYLESHEET_FACET_NAME;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.faces.application.Resource;
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
	private static final Logger LOGGER = Logger.getLogger(OpenVectorEditorRenderer.class.getName());

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
		encodeJS(writer, plugin, editorTargetDivId, iframeId, hiddenInputId);
		encodeIframe(context, writer, plugin, editorTargetDivId, iframeId, hiddenInputId);

		// end of surrounding <div>
		writer.endElement("div");
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

	private void encodeJS(ResponseWriter writer, OpenVectorEditorCore plugin, String editorTargetDivId, String iframeId, String hiddenInputId) throws IOException {
		String jsCode = generateJSCode(plugin, editorTargetDivId, iframeId, hiddenInputId);
		writeScriptTag(jsCode, plugin, writer);
	}

	private void writeScriptTag(String jsCode, OpenVectorEditorCore plugin, ResponseWriter writer) throws IOException {
		writer.startElement("script", plugin);
		writer.writeAttribute("type", "text/javascript", null);
		writer.writeText(jsCode, null);
		writer.endElement("script");
	}

	private void encodeIframe(FacesContext context, ResponseWriter writer, OpenVectorEditorCore plugin, String editorTargetDivId,
			String iframeId, String hiddenInputId) throws IOException {
		writer.startElement("iframe", plugin);
		writer.writeAttribute("id", iframeId, null);
		writer.writeAttribute("style", "border:none;", null);
		writer.writeAttribute("srcdoc", generateIframeSrcdocHTML(context, plugin, editorTargetDivId), null);
		writer.writeText("Your browser does not support iframes.", null);
		writer.endElement("iframe");
	}

	private String generateIframeSrcdocHTML(FacesContext context, OpenVectorEditorCore plugin, String editorTargetDivId) throws IOException {
		StringBuilder sb = new StringBuilder(256);

		sb.append("<html><head>");
		sb.append(renderCssResourcesFromFacet(context, plugin));
		sb.append("</head><body>");
		sb.append(renderScriptResourcesFromFacet(context,plugin));

		// <div> is used for the plugin rendering
		sb.append("<div id=\"").append(editorTargetDivId).append("\"></div>");

		sb.append("</body></html>");

		return sb.toString();
	}

	private String renderCssResourcesFromFacet(FacesContext context, OpenVectorEditorCore plugin) throws IOException {
		return encodeResourceComponents(context, getChildrenFromFacet(STYLESHEET_FACET_NAME, plugin));
	}

	private String renderScriptResourcesFromFacet(FacesContext context, OpenVectorEditorCore plugin) throws IOException {
		return encodeResourceComponents(context, getChildrenFromFacet(JAVASCRIPT_FACET_NAME, plugin));
	}

	private List<UIComponent> getChildrenFromFacet(String facetName, UIComponent component) {
		UIComponent facet = component.getFacet(facetName);
		if (facet == null) {
			return Collections.emptyList();
		} else {
			return facet.getChildren();
		}
	}

	private String encodeResourceComponents(FacesContext context, List<UIComponent> components) throws IOException {
		if (components.isEmpty()) {
			return "";
		}

		StringWriter stringWriter = new StringWriter();
		/*
		 * Where to get a ResponseWriter implementation without inclusion of a specific
		 * JSF implementation? I think, someone anticipated my weird use case. THIS IS
		 * GREAT!
		 */
		ResponseWriter newWriter = context.getResponseWriter().cloneWithWriter(stringWriter);

		for (UIComponent comp : components) {
			encodeResourceComponent(context, newWriter, comp);
		}

		return stringWriter.toString();
	}

	/*
	 * This is a bit tricky: The JSF implementations optimize resource rendering by
	 * rendering a specific resource (library name + resource name) only once. Thus,
	 * we cannot render via the standard renderers for scripts or stylesheets.
	 * Instead, this implementation builds the HTML tag itself.
	 */
	private void encodeResourceComponent(FacesContext context, ResponseWriter writer, UIComponent component) throws IOException {
		Map<String, Object> attributes = component.getAttributes();
		String resourceName = (String) attributes.get("name");
		String library = (String) attributes.get("library");

		if ((resourceName == null) || resourceName.isEmpty()) {
			return;
		}

		Resource resource = context.getApplication().getResourceHandler().createResource(resourceName, library);
		if (resource == null) {
			LOGGER.warning("Resource not found: resourceName=" + resourceName
					+ (library != null ? ", library=" + library : ""));
			return;
		}
		String path = resource.getRequestPath();

		if (component.getRendererType().equals(JAVASCRIPT)) {
			writer.startElement("script", component);
			writer.writeAttribute("type", "text/javascript", null);
			writer.writeURIAttribute("src", context.getExternalContext().encodeResourceURL(path), null);
			writer.endElement("script");
		} else if (component.getRendererType().equals(STYLESHEET)) {
			writer.startElement("link", component);
			writer.writeAttribute("rel", "stylesheet", null);
			writer.writeAttribute("type", "text/css", null);
			writer.writeURIAttribute("href", context.getExternalContext().encodeResourceURL(path), null);
			writer.endElement("link");
		}
	}

	private String generateJSCode(OpenVectorEditorCore plugin, String editorTargetDivId, String iframeId, String hiddenInputId) {
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