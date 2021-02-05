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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;

/**
 * Contract of this class:
 * 
 * @author flange
 */
public class AddResourcesListener implements SystemEventListener {
	private static final String LIBRARY_NAME = "molecularfaces";
	private static final String SCRIPT_RESOURCE_KEY = "de.ipb_halle.molecularfaces.AddResourcesListener.ScriptResource";
	private static final String SCRIPT_EXT_KEY = "de.ipb_halle.molecularfaces.AddResourcesListener.ScriptExt";
	private static final String SCRIPT_AS_TEXT_KEY = "de.ipb_halle.molecularfaces.AddResourcesListener.ScriptAsText";
	private static final String CSS_RESOURCE_KEY = "de.ipb_halle.molecularfaces.AddResourcesListener.CssResource";
	private static final String CSS_EXT_KEY = "de.ipb_halle.molecularfaces.AddResourcesListener.CssExt";

	public static void addScriptResource(String resource) {
		getResourceSetFromViewMap(SCRIPT_RESOURCE_KEY).add(resource);
	}

	public static void addScriptExt(String src) {
		getResourceSetFromViewMap(SCRIPT_EXT_KEY).add(src);
	}

	public static void addScriptAsText(String script) {
		getResourceSetFromViewMap(SCRIPT_AS_TEXT_KEY).add(script);
	}

	public static void addCssResource(String resource) {
		getResourceSetFromViewMap(CSS_RESOURCE_KEY).add(resource);
	}

	public static void addCssExt(String href) {
		getResourceSetFromViewMap(CSS_EXT_KEY).add(href);
	}

	private static Set<String> getResourceSetFromViewMap(String key) {
		Map<String, Object> viewMap = FacesContext.getCurrentInstance().getViewRoot().getViewMap();

		@SuppressWarnings("unchecked")
		Set<String> resourceSet = (Set<String>) viewMap.get(key);

		if (resourceSet == null) {
			resourceSet = new HashSet<>();
			viewMap.put(key, resourceSet);
		}

		return resourceSet;
	}

	private static void removeResourceSetFromViewMap(String key) {
		Map<String, Object> viewMap = FacesContext.getCurrentInstance().getViewRoot().getViewMap();
		viewMap.remove(key);
	}

	@Override
	public void processEvent(SystemEvent event) throws AbortProcessingException {
		FacesContext context = FacesContext.getCurrentInstance();
		UIViewRoot root = context.getViewRoot();

		addScriptResourcesToHead(root, context);
		addScriptExtToHead(root, context);
		addScriptAsTextToHead(root, context);
		addCssResourceToHead(root, context);
		addCssExtToHead(root, context);
	}

	@Override
	public boolean isListenerForSource(Object source) {
		return (source instanceof UIViewRoot);
	}

	private void addScriptResourcesToHead(UIViewRoot root, FacesContext context) {
		for (String resource : getResourceSetFromViewMap(SCRIPT_RESOURCE_KEY)) {
			/*
			 * Create a UIComponent that includes the file as script from the resource
			 * library.
			 */
			UIOutput jsResource = new UIOutput();
			jsResource.setRendererType("javax.faces.resource.Script");
			jsResource.getAttributes().put("library", LIBRARY_NAME);
			jsResource.getAttributes().put("name", resource);

			// Add this component to the <head>.
			context.getViewRoot().addComponentResource(context, jsResource, "head");
		}

		removeResourceSetFromViewMap(SCRIPT_RESOURCE_KEY);
	}

	private void addScriptExtToHead(UIViewRoot root, FacesContext context) {
		for (String src : getResourceSetFromViewMap(SCRIPT_EXT_KEY)) {
			/*
			 * Create a very simple UIComponent that renders <script type="text/javascript"
			 * scr="..." />.
			 */
			UIOutput script = new UIOutput() {
				@Override
				public void encodeBegin(FacesContext context) throws IOException {
					ResponseWriter writer = context.getResponseWriter();
					writer.startElement("script", this);
					writer.writeAttribute("type", "text/javascript", null);
					writer.writeAttribute("src", src, null);
					writer.endElement("script");
				};
			};

			// Add this component to the <head>.
			context.getViewRoot().addComponentResource(context, script, "head");
		}

		removeResourceSetFromViewMap(SCRIPT_EXT_KEY);
	}

	private void addScriptAsTextToHead(UIViewRoot root, FacesContext context) {
		for (String script : getResourceSetFromViewMap(SCRIPT_AS_TEXT_KEY)) {
			/*
			 * Create a very simple UIComponent that renders <script
			 * type="text/javascript">...</script>.
			 */
			UIOutput js = new UIOutput() {
				@Override
				public void encodeBegin(FacesContext context) throws IOException {
					ResponseWriter writer = context.getResponseWriter();
					writer.startElement("script", this);
					writer.writeAttribute("type", "text/javascript", null);
					writer.writeText(script, null);
					writer.endElement("script");
				};
			};

			// Add this component to the <head>.
			context.getViewRoot().addComponentResource(context, js, "head");
		}

		removeResourceSetFromViewMap(SCRIPT_AS_TEXT_KEY);
	}

	private void addCssResourceToHead(UIViewRoot root, FacesContext context) {
		for (String resource : getResourceSetFromViewMap(CSS_RESOURCE_KEY)) {
			/*
			 * Create a UIComponent that includes the file from the resource library.
			 */
			UIOutput cssResource = new UIOutput();
			cssResource.setRendererType("javax.faces.resource.Stylesheet");
			cssResource.getAttributes().put("library", LIBRARY_NAME);
			cssResource.getAttributes().put("name", resource);

			// Add this component to the <head>.
			context.getViewRoot().addComponentResource(context, cssResource, "head");
		}

		removeResourceSetFromViewMap(CSS_RESOURCE_KEY);
	}

	private void addCssExtToHead(UIViewRoot root, FacesContext context) {
		for (String href : getResourceSetFromViewMap(CSS_EXT_KEY)) {
			/*
			 * Create a very simple UIComponent that renders <link rel="stylesheet"
			 * type="text/css" href="..." />.
			 */
			UIOutput link = new UIOutput() {
				@Override
				public void encodeBegin(FacesContext context) throws IOException {
					ResponseWriter writer = context.getResponseWriter();
					writer.startElement("link", this);
					writer.writeAttribute("rel", "stylesheet", null);
					writer.writeAttribute("type", "text/css", null);
					writer.writeAttribute("href", href, null);
					writer.endElement("script");
				};
			};

			// Add this component to the <head>.
			context.getViewRoot().addComponentResource(context, link, "head");
		}

		removeResourceSetFromViewMap(CSS_EXT_KEY);
	}
}