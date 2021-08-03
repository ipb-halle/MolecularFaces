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

import java.io.IOException;
import java.util.Formatter;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;

import de.ipb_halle.molecularfaces.util.RendererUtils;

/**
 * This {@link javax.faces.render.Renderer} renders a chemical structure editor or viewer using the
 * <a href="https://github.com/cheminfo/openchemlib-js">OpenChemLib JS</a>
 * Javascript plugin.
 * 
 * @author flange
 */
@FacesRenderer(rendererType = OpenChemLibJSRenderer.RENDERER_TYPE, componentFamily = MolPluginCore.COMPONENT_FAMILY)
public class OpenChemLibJSRenderer extends MolPluginRenderer {
	public static final String RENDERER_TYPE = "molecularfaces.OpenChemLibJSRenderer";

	/**
	 * Name of the JavaScript global variable that represents a common
	 * ResourcesLoader instance for all rendered components of this plugin type.
	 * This variable is defined in MolecularFaces.js.
	 */
	private String loaderJSVar = "molecularfaces.openChemLibJSLoaderInstance";

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
			encodeViewer(context, writer, plugin);
		} else {
			encodeEditor(context, writer, plugin);
		}

		// end of surrounding <div>
		writer.endElement("div");
	}

	private void encodeViewer(FacesContext context, ResponseWriter writer, MolPluginCore plugin) throws IOException {
		String clientId = plugin.getClientId();
		String hiddenInputId = clientId + "_Input";
		String divId = clientId + "_OpenChemLibJSViewer";

		encodeViewerHTML(context, writer, plugin, divId, hiddenInputId);
		encodeViewerJS(writer, plugin, divId, hiddenInputId);
	}

	/**
	 * Encodes the HTML part of the plugin viewer into the writer. It consists of a
	 * &lt;div&gt; element that the Javascript plugin uses as rendering target.
	 * 
	 * @param context
	 * @param writer
	 * @param plugin
	 * @param divId         DOM id of the embedded &lt;div&gt; element
	 * @param hiddenInputId DOM id of the embedded hidden &lt;input&gt; element
	 */
	private void encodeViewerHTML(FacesContext context, ResponseWriter writer, MolPluginCore plugin, String divId,
			String hiddenInputId) throws IOException {
		// inner <div> is used for the plugin's rendering (aka the Javascript target)
		writer.startElement("div", plugin);
		writer.writeAttribute("id", divId, null);
		writer.writeAttribute("style", generateDivStyle(plugin), null);
		writer.endElement("div");

		// hidden <input> without "name" attribute (prevents submission)
		writer.startElement("input", plugin);
		writer.writeAttribute("type", "hidden", null);
		writer.writeAttribute("id", hiddenInputId, null);
		writer.writeAttribute("value", RendererUtils.convertValueToString(context, plugin, plugin.getValue()), "value");
		writer.endElement("input");
	}

	/**
	 * Encodes the Javascript part of the plugin viewer into the writer.
	 * 
	 * @param writer
	 * @param plugin
	 * @param divId         DOM id of the &lt;div&gt; element
	 * @param hiddenInputId DOM id of the embedded hidden &lt;input&gt; element
	 */
	private void encodeViewerJS(ResponseWriter writer, MolPluginCore plugin, String divId, String hiddenInputId)
			throws IOException {
		writer.startElement("script", plugin);
		writer.writeAttribute("type", "text/javascript", null);

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
		 * Start viewer, set the molecule from the hidden <input> element's value and
		 * return the viewer object embedded in a Promise.
		 */
		fmt.format("%s.status().then(() => {", loaderJSVar);
		fmt.format(
				"return molecularfaces.OpenChemLibJSViewer.newViewer(\"%s\", "
						+ "document.getElementById(\"%s\").getAttribute(\"value\"), %d, %d);",
				divId, hiddenInputId, plugin.getHeight(), plugin.getWidth());

		fmt.close();

		// end of then()
		sb.append("});");

		writer.writeText(sb, null);
		writer.endElement("script");
	}

	private void encodeEditor(FacesContext context, ResponseWriter writer, MolPluginCore plugin) throws IOException {
		String clientId = plugin.getClientId();
		String hiddenInputId = clientId + "_Input";
		String divId = clientId + "_OpenChemLibJSEditor";

		encodeEditorHTML(context, writer, plugin, divId, hiddenInputId);
		encodeEditorJS(writer, plugin, divId, hiddenInputId);
	}

	/**
	 * Encodes the HTML part of the plugin editor into the writer. It consists of a
	 * &lt;div&gt; and a hidden &lt;input&gt; element.
	 * 
	 * @param context
	 * @param writer
	 * @param plugin
	 * @param divId         DOM id of the embedded &lt;div&gt; element
	 * @param hiddenInputId DOM id of the embedded hidden &lt;input&gt; element
	 */
	private void encodeEditorHTML(FacesContext context, ResponseWriter writer, MolPluginCore plugin, String divId,
			String hiddenInputId) throws IOException {
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
		writer.writeAttribute("value", RendererUtils.convertValueToString(context, plugin, plugin.getValue()), "value");
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

		// Start editor, set the molecule from the hidden <input> element's value.
		fmt.format("let editorPromise = molecularfaces.OpenChemLibJSEditor"
				+ ".newEditor(\"%s\", document.getElementById(\"%s\").getAttribute(\"value\"), \"%s\");",
				divId, hiddenInputId, plugin.getFormat());

		/*
		 * Register an on-change callback to fill the value of the hidden <input>
		 * element.
		 */
		fmt.format("editorPromise.then(editor => "
				+ "editor.getOnChangeSubject().addChangeCallback((mol) => { "
				+ "document.getElementById(\"%s\").setAttribute(\"value\", mol); }));",
				hiddenInputId);

		fmt.close();

		// Return the editor object embedded in another Promise that is written into widgetVar.
		sb.append("return editorPromise;");

		// end of then() block of the ResourcesLoader's Promise
		sb.append("});");

		writer.writeText(sb, null);
		writer.endElement("script");
	}
}