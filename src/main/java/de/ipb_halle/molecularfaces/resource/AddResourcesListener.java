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
package de.ipb_halle.molecularfaces.resource;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PreRenderViewEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;

/**
 * This class can be used for dynamic resource loading by {@link UIComponent}s.
 * It uses the same implementation strategy like Bootsfaces' <a href=
 * "https://github.com/TheCoder4eu/BootsFaces-OSP/blob/master/src/main/java/net/bootsfaces/listeners/AddResourcesListener.java">AddResourcesListener</a>
 * class. This class has to be registered as {@code system-event-listener} in
 * faces-config.xml with "javax.faces.event.PreRenderViewEvent" as
 * {@code system-event-class}.
 * <p>
 * Usage:
 * <p>
 * The {@link UIComponent} requiring resources calls the add...(String)-methods
 * as soon as possible (preferably in its constructor).
 * <p>
 * Mechanism:
 * <p>
 * The add...()-methods add resources to the view map to be retrieved in a later
 * JSF lifecycle phase. Duplicate entries are suppressed by the use of
 * {@link HashSet}s. Later, while processing the {@link PreRenderViewEvent}, the
 * resources are retrieved from the view map and added as {@link UIComponent}s
 * to &lt;head&gt;. Note: Duplicate resources that are already present in the
 * component tree are taken into consideration, i.e. cause no resource clutter
 * after AJAX calls. This is achieved (1) by keeping track which resources were
 * already added to the &lt;head&gt; via the view map and (2) by using resource
 * {@link UIComponent}s that render only once.
 * 
 * @author flange
 */
public class AddResourcesListener implements SystemEventListener {
	private static final String LIBRARY_NAME = "molecularfaces";

	// These Strings are used as keys in the view map.
	private static final String SCRIPT_RESOURCE_KEY = "de.ipb_halle.molecularfaces.AddResourcesListener.ScriptResource";
	private static final String SCRIPT_RESOURCE_ADDED_KEY = "de.ipb_halle.molecularfaces.AddResourcesListener.ScriptResourceAdded";

	private static final String SCRIPT_EXT_KEY = "de.ipb_halle.molecularfaces.AddResourcesListener.ScriptExt";
	private static final String SCRIPT_EXT_ADDED_KEY = "de.ipb_halle.molecularfaces.AddResourcesListener.ScriptExtAdded";

	private static final String SCRIPT_AS_TEXT_KEY = "de.ipb_halle.molecularfaces.AddResourcesListener.ScriptAsText";
	private static final String SCRIPT_AS_TEXT_ADDED_KEY = "de.ipb_halle.molecularfaces.AddResourcesListener.ScriptAsTextAdded";

	private static final String CSS_RESOURCE_KEY = "de.ipb_halle.molecularfaces.AddResourcesListener.CssResource";
	private static final String CSS_RESOURCE_ADDED_KEY = "de.ipb_halle.molecularfaces.AddResourcesListener.CssResourceAdded";

	private static final String CSS_EXT_KEY = "de.ipb_halle.molecularfaces.AddResourcesListener.CssExt";
	private static final String CSS_EXT_ADDED_KEY = "de.ipb_halle.molecularfaces.AddResourcesListener.CssExtAdded";

	/**
	 * Adds a JavaScript file from JSF's resources (library "molecularfaces").
	 * 
	 * @param resource
	 */
	public static void addScriptResource(String resource) {
		Set<String> alreadyAdded = getResourceSetFromViewMap(SCRIPT_RESOURCE_ADDED_KEY);

		if (!alreadyAdded.contains(resource)) {
			getResourceSetFromViewMap(SCRIPT_RESOURCE_KEY).add(resource);
		}
	}

	/**
	 * Adds a JavaScript file from an external source. It will render &lt;script
	 * type=&quot;text/javascript&quot; scr=&quot;{@code src}&quot; /&gt;.
	 * 
	 * @param src
	 */
	public static void addScriptExt(String src) {
		Set<String> alreadyAdded = getResourceSetFromViewMap(SCRIPT_EXT_ADDED_KEY);

		if (!alreadyAdded.contains(src)) {
			getResourceSetFromViewMap(SCRIPT_EXT_KEY).add(src);
		}
	}

	/**
	 * Adds JavaScript code as text. It will render &lt;script
	 * type=&quot;text/javascript&quot;&gt;{@code script}&lt;/script&gt;.
	 * 
	 * @param script
	 */
	public static void addScriptAsText(String script) {
		Set<String> alreadyAdded = getResourceSetFromViewMap(SCRIPT_AS_TEXT_ADDED_KEY);

		if (!alreadyAdded.contains(script)) {
			getResourceSetFromViewMap(SCRIPT_AS_TEXT_KEY).add(script);
		}
	}

	/**
	 * Add a CSS file from JSF's resources (library "molecularfaces").
	 * 
	 * @param resource
	 */
	public static void addCssResource(String resource) {
		Set<String> alreadyAdded = getResourceSetFromViewMap(CSS_RESOURCE_ADDED_KEY);

		if (!alreadyAdded.contains(resource)) {
			getResourceSetFromViewMap(CSS_RESOURCE_KEY).add(resource);
		}
	}

	/**
	 * Adds a CSS file from an external source. It will render &lt;link
	 * rel=&quot;stylesheet&quot; type=&quot;text/css&quot;
	 * href=&quot;{@code href}&quot; /&gt;.
	 * 
	 * @param href
	 */
	public static void addCssExt(String href) {
		Set<String> alreadyAdded = getResourceSetFromViewMap(CSS_EXT_ADDED_KEY);

		if (!alreadyAdded.contains(href)) {
			getResourceSetFromViewMap(CSS_EXT_KEY).add(href);
		}
	}

	/**
	 * Retrieves an item with the given key from the view map. If it does not exist,
	 * it is initialized as a new {@link HashSet}.
	 * 
	 * @param key
	 * @return
	 */
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

	/**
	 * Remove an item with the given key from the view map.
	 * 
	 * @param key
	 */
	private static void removeResourceSetFromViewMap(String key) {
		Map<String, Object> viewMap = FacesContext.getCurrentInstance().getViewRoot().getViewMap();
		viewMap.remove(key);
	}

	/**
	 * Processes the {@link PreRenderViewEvent} event. This adds the resources
	 * defined in the view map to the component tree.
	 */
	@Override
	public void processEvent(SystemEvent event) throws AbortProcessingException {
		if (event instanceof PreRenderViewEvent) {
			FacesContext context = FacesContext.getCurrentInstance();
			UIViewRoot root = context.getViewRoot();

			addScriptResourcesToHead(root, context);
			addScriptExtToHead(root, context);
			addScriptAsTextToHead(root, context);
			addCssResourceToHead(root, context);
			addCssExtToHead(root, context);
		}
	}

	/**
	 * Listens only to {@link UIViewRoot}.
	 */
	@Override
	public boolean isListenerForSource(Object source) {
		return (source instanceof UIViewRoot);
	}

	private void addScriptResourcesToHead(UIViewRoot root, FacesContext context) {
		for (String resource : getResourceSetFromViewMap(SCRIPT_RESOURCE_KEY)) {
			/*
			 * Create an UIComponent that includes the file as script from the resource
			 * library.
			 */
			UIOutput jsResource = new UIOutput();
			jsResource.setRendererType("javax.faces.resource.Script");
			jsResource.getAttributes().put("library", LIBRARY_NAME);
			jsResource.getAttributes().put("name", resource);

			// Add the component to <head>.
			context.getViewRoot().addComponentResource(context, jsResource, "head");

			// Mark this component as added.
			getResourceSetFromViewMap(SCRIPT_RESOURCE_ADDED_KEY).add(resource);
		}

		removeResourceSetFromViewMap(SCRIPT_RESOURCE_KEY);
	}

	private void addScriptExtToHead(UIViewRoot root, FacesContext context) {
		for (String src : getResourceSetFromViewMap(SCRIPT_EXT_KEY)) {
			/*
			 * Create an UIComponent that renders <script type="text/javascript" scr="..."
			 * />.
			 */
			ExtScriptResource script = new ExtScriptResource(src);

			// Add the component to <head>.
			root.addComponentResource(context, script, "head");

			// Mark this component as added.
			getResourceSetFromViewMap(SCRIPT_EXT_ADDED_KEY).add(src);
		}

		removeResourceSetFromViewMap(SCRIPT_EXT_KEY);
	}

	private void addScriptAsTextToHead(UIViewRoot root, FacesContext context) {
		for (String script : getResourceSetFromViewMap(SCRIPT_AS_TEXT_KEY)) {
			/*
			 * Create an UIComponent that renders <script
			 * type="text/javascript">...</script>.
			 */
			ScriptAsTextResource js = new ScriptAsTextResource(script);

			// Add the component to <head>.
			root.addComponentResource(context, js, "head");

			// Mark this component as added.
			getResourceSetFromViewMap(SCRIPT_AS_TEXT_ADDED_KEY).add(script);
		}

		removeResourceSetFromViewMap(SCRIPT_AS_TEXT_KEY);
	}

	private void addCssResourceToHead(UIViewRoot root, FacesContext context) {
		for (String resource : getResourceSetFromViewMap(CSS_RESOURCE_KEY)) {
			/*
			 * Create an UIComponent that includes the file from the resource library.
			 */
			UIOutput cssResource = new UIOutput();
			cssResource.setRendererType("javax.faces.resource.Stylesheet");
			cssResource.getAttributes().put("library", LIBRARY_NAME);
			cssResource.getAttributes().put("name", resource);

			// Add the component to <head>.
			root.addComponentResource(context, cssResource, "head");

			// Mark this component as added.
			getResourceSetFromViewMap(CSS_RESOURCE_ADDED_KEY).add(resource);
		}

		removeResourceSetFromViewMap(CSS_RESOURCE_KEY);
	}

	private void addCssExtToHead(UIViewRoot root, FacesContext context) {
		for (String href : getResourceSetFromViewMap(CSS_EXT_KEY)) {
			/*
			 * Create an UIComponent that renders <link rel="stylesheet" type="text/css"
			 * href="..." />.
			 */
			ExtCSSResource link = new ExtCSSResource(href);

			// Add the component to <head>.
			root.addComponentResource(context, link, "head");

			// Mark this component as added.
			getResourceSetFromViewMap(CSS_EXT_ADDED_KEY).add(href);
		}

		removeResourceSetFromViewMap(CSS_EXT_KEY);
	}
}