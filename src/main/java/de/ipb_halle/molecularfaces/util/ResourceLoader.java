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
package de.ipb_halle.molecularfaces.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.html.HtmlBody;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.PostAddToViewEvent;

/**
 * This class enqueues resources and loads them either automatically or upon
 * request.
 * 
 * @author flange
 */
public class ResourceLoader implements ComponentSystemEventListener, Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Resource library name.
	 */
	public static final String RESOURCES_LIBRARY_NAME = "molecularfaces";

	/**
	 * Name of the facet that is used when adding JavaScript resources as facet components.
	 */
	public static final String JAVASCRIPT_FACET_NAME = "molecularfaces.resourceloader.javascript";

	/**
	 * Name of the facet that is used when adding stylesheet resources as facet components.
	 */
	public static final String STYLESHEET_FACET_NAME = "molecularfaces.resourceloader.stylesheet";

	/**
	 * Renderer type for JavaScript resources.
	 */
	public static final String JAVASCRIPT = "javax.faces.resource.Script";

	/**
	 * Renderer type for stylesheet resources.
	 */
	public static final String STYLESHEET = "javax.faces.resource.Stylesheet";

	private final UIComponent component;

	/*
	 * Queue objects
	 */
	private Set<String> scriptResourcesToLoadInHead = new HashSet<>();
	private Set<String> scriptResourcesToLoadInBodyAtTop = new HashSet<>();
	private Set<String> scriptsExtToLoadInHead = new HashSet<>();
	private Set<String> scriptsExtToLoadInBodyAtTop = new HashSet<>();
	private Set<String> cssResourcesToLoad = new HashSet<>();
	private Set<String> cssExtToLoad = new HashSet<>();

	/**
	 * Wraps the given component and registers a PostAddToView event to it, in which
	 * this instance will attach its enqueued resources to the component tree.
	 * 
	 * @param component UI component
	 */
	public ResourceLoader(UIComponent component) {
		this.component = component;
		component.subscribeToEvent(PostAddToViewEvent.class, this);
	}

	@Override
	public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
		if (event instanceof PostAddToViewEvent) {
			FacesContext context = FacesContext.getCurrentInstance();
			loadScriptResources(context);
			loadCssResources(context);
		}
	}

	/**
	 * Enqueues loading of a JavaScript resource file that will be added via JSF's
	 * resource mechanism to the &lt;head&gt;.
	 * 
	 * @param resource name of the file in the web project's resource library
	 */
	public void addScriptResourceToHead(String resource) {
		scriptResourcesToLoadInHead.add(resource);
	}

	/**
	 * @return An immutable {@link Set} of the JavaScript resources enqueued by
	 *         {@link #addScriptResourceToHead(String)} and that have not been
	 *         loaded via {{@link #processEvent(ComponentSystemEvent)} yet.
	 */
	public Set<String> getScriptResourcesToLoadInHead() {
		return Collections.unmodifiableSet(scriptResourcesToLoadInHead);
	}

	/**
	 * Enqueues loading of a JavaScript resource file that will be added via JSF's
	 * resource mechanism to the top of &lt;body&gt;.
	 * <p>
	 * Note: There is no guarantee on the load order among the resources enqueued by
	 * this method.
	 * 
	 * @param resource name of the file in the web project's resource library
	 */
	public void addScriptResourceToBodyAtTop(String resource) {
		scriptResourcesToLoadInBodyAtTop.add(resource);
	}

	/**
	 * @return An immutable {@link Set} of the JavaScript resources enqueued by
	 *         {@link #addScriptResourceToBodyAtTop(String)} and that have not been
	 *         loaded via {{@link #processEvent(ComponentSystemEvent)} yet.
	 */
	public Set<String> getScriptResourcesToLoadInBodyAtTop() {
		return Collections.unmodifiableSet(scriptResourcesToLoadInBodyAtTop);
	}

	/**
	 * Enqueues loading of a JavaScript file that will be loaded in the &lt;head&gt;
	 * via the JavaScript class {@code molecularfaces.ResourcesLoader}. The code
	 * snippet can be requested via {@link #encodeLoadExtResources(String)}.
	 * 
	 * @param src path of the file
	 */
	public void addScriptExtToHead(String src) {
		scriptsExtToLoadInHead.add(src);
	}

	/**
	 * @return An immutable {@link Set} of the JavaScript resources enqueued by
	 *         {@link #addScriptExtToHead(String)}.
	 */
	public Set<String> getScriptsExtToLoadInHead() {
		return Collections.unmodifiableSet(scriptsExtToLoadInHead);
	}

	/**
	 * Enqueues loading of a JavaScript file that will be loaded in the top of
	 * &lt;body&gt; via the JavaScript class {@code molecularfaces.ResourcesLoader}.
	 * The code snippet can be requested via
	 * {@link #encodeLoadExtResources(String)}.
	 * 
	 * @param src path of the file
	 */
	public void addScriptExtToBodyAtTop(String src) {
		scriptsExtToLoadInBodyAtTop.add(src);
	}

	/**
	 * @return An immutable {@link Set} of the JavaScript resources enqueued by
	 *         {@link #addScriptExtToBodyAtTop(String)}.
	 */
	public Set<String> getScriptsExtToLoadInBodyAtTop() {
		return Collections.unmodifiableSet(scriptsExtToLoadInBodyAtTop);
	}

	/**
	 * Enqueues loading of a stylesheet resource file that will be added via JSF's
	 * resource mechanism.
	 * 
	 * @param resource name of the file in the web project's resource library
	 */
	public void addCssResource(String resource) {
		cssResourcesToLoad.add(resource);
	}

	/**
	 * @return An immutable {@link Set} of the stylesheet resources enqueued by
	 *         {@link #addCssResource(String)} and that have not been loaded via
	 *         {{@link #processEvent(ComponentSystemEvent)} yet.
	 */
	public Set<String> getCssResourcesToLoad() {
		return Collections.unmodifiableSet(cssResourcesToLoad);
	}

	/**
	 * Enqueues loading of a stylesheet file that will be loaded via the JavaScript
	 * class {@code molecularfaces.ResourcesLoader}. The code snippet can be
	 * requested via {@link #encodeLoadExtResources(String)}.
	 * 
	 * @param href path of the file
	 */
	public void addCssExt(String href) {
		cssExtToLoad.add(href);
	}

	/**
	 * @return An immutable {@link Set} of the stylesheet resources enqueued by
	 *         {@link #addCssExt(String)}.
	 */
	public Set<String> getCssExtToLoad() {
		return Collections.unmodifiableSet(cssExtToLoad);
	}

	/**
	 * Adds a JavaScript resource component as facet to the wrapped component. The
	 * wrapped component is responsible for rendering its facets.
	 * 
	 * @param resource name of the file in the web project's resource library
	 */
	public void addScriptResourceAsFacetComponent(String resource) {
		UIComponent resourceComponent = createResourceComponent(resource, JAVASCRIPT);
		resourceComponent.getAttributes().put("external", Boolean.FALSE);
		addComponentToResourceContainerInFacet(resourceComponent, JAVASCRIPT_FACET_NAME, component.getFacets());
	}

	/**
	 * Adds a JavaScript file as facet resource component to the wrapped component.
	 * The wrapped component is responsible for rendering its facets.
	 * 
	 * @param src path of the file
	 */
	public void addScriptExtAsFacetComponent(String src) {
		UIComponent resourceComponent = createResourceComponent(src, JAVASCRIPT);
		resourceComponent.getAttributes().put("external", Boolean.TRUE);
		addComponentToResourceContainerInFacet(resourceComponent, JAVASCRIPT_FACET_NAME, component.getFacets());
	}

	/**
	 * Adds a stylesheet resource component as facet to the wrapped component. The
	 * wrapped component is responsible for rendering its facets.
	 * 
	 * @param resource name of the file in the web project's resource library
	 */
	public void addCssResourceAsFacetComponent(String resource) {
		UIComponent resourceComponent = createResourceComponent(resource, STYLESHEET);
		resourceComponent.getAttributes().put("external", Boolean.FALSE);
		addComponentToResourceContainerInFacet(resourceComponent, STYLESHEET_FACET_NAME, component.getFacets());
	}

	/**
	 * Adds a stylesheet file as facet resource component to the wrapped component.
	 * The wrapped component is responsible for rendering its facets.
	 * 
	 * @param href path of the file
	 */
	public void addCssExtAsFacetComponent(String href) {
		UIComponent resourceComponent = createResourceComponent(href, STYLESHEET);
		resourceComponent.getAttributes().put("external", Boolean.TRUE);
		addComponentToResourceContainerInFacet(resourceComponent, STYLESHEET_FACET_NAME, component.getFacets());
	}

	private void loadScriptResources(FacesContext context) {
		for (String resource : scriptResourcesToLoadInHead) {
			UIComponent component = createResourceComponent(resource, JAVASCRIPT);
			addComponentToHead(component, context);
		}
		scriptResourcesToLoadInHead.clear();

		for (String resource : scriptResourcesToLoadInBodyAtTop) {
			UIComponent component = createResourceComponent(resource, JAVASCRIPT);
			addComponentToBodyAtTop(component, context);
		}
		/*
		 * Why is it important to clear THIS queue? MyFaces fires another
		 * PostAddToViewEvent to the component when adding a child to the body (done in
		 * addComponentToBodyAtTop(...)), thus we end up in an infinite
		 * fire-event-add-component loop. Mojarra shows no such behavior.
		 */
		scriptResourcesToLoadInBodyAtTop.clear();
	}

	private void loadCssResources(FacesContext context) {
		for (String resource : cssResourcesToLoad) {
			UIComponent component = createResourceComponent(resource, STYLESHEET);
			addComponentToHead(component, context);
		}
		cssResourcesToLoad.clear();
	}

	private UIComponent createResourceComponent(String resourceName, String rendererType) {
		UIOutput resourceComponent = new UIOutput();
		resourceComponent.setRendererType(rendererType);
		resourceComponent.getAttributes().put("library", RESOURCES_LIBRARY_NAME);
		resourceComponent.getAttributes().put("name", resourceName);

		return resourceComponent;
	}

	private void addComponentToHead(UIComponent componentToAdd, FacesContext context) {
		context.getViewRoot().addComponentResource(context, componentToAdd, "head");
	}

	private void addComponentToBodyAtTop(UIComponent componentToAdd, FacesContext context) {
		UIComponent body = findBodyComponent(context);

		if (body != null) {
			// MyFaces fires a PostAddToViewEvent after this.
			body.getChildren().add(0, componentToAdd);
		}
	}

	private UIComponent findBodyComponent(FacesContext context) {
		UIComponent root = context.getViewRoot();
		for (UIComponent component : root.getChildren()) {
			if (component instanceof HtmlBody) {
				return component;
			}
		}

		return null;
	}

	private void addComponentToResourceContainerInFacet(UIComponent component, String facetName,
			Map<String, UIComponent> facets) {
		UIComponent container = facets.computeIfAbsent(facetName, k -> FacesContext.getCurrentInstance()
				.getApplication().createComponent("javax.faces.ComponentResourceContainer"));
		container.getChildren().add(component);
	}

	/**
	 * Generates a JavaScript code fragment for loading resources that have been
	 * enqueued for loading via {@code molecularfaces.ResourcesLoader}.
	 * 
	 * @param loaderJSVar JavaScript variable name of the
	 *                    {@code molecularfaces.ResourcesLoader} instance
	 * @return JavaScript code
	 */
	public StringBuilder encodeLoadExtResources(String loaderJSVar) {
		StringBuilder sb = new StringBuilder(256);
		Formatter fmt = new Formatter(sb);

		for (String script : scriptsExtToLoadInHead) {
			fmt.format(".addScriptToHead(\"%s\")", script);
		}
		for (String script : scriptsExtToLoadInBodyAtTop) {
			fmt.format(".addScriptToBodyAtTop(\"%s\")", script);
		}
		for (String href : cssExtToLoad) {
			fmt.format(".addCssToHead(\"%s\")", href);
		}
		fmt.close();

		if (sb.length() > 0) {
			sb.insert(0, loaderJSVar);
			sb.append(";");
		}

		return sb;
	}
}