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

import java.util.Formatter;
import java.util.HashSet;
import java.util.Set;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.html.HtmlBody;
import javax.faces.context.FacesContext;

/**
 * This class enqueues resources and loads them upon request.
 * 
 * @author flange
 */
public class ResourceLoader {
	/**
	 * Resource library name.
	 */
	private static final String RESOURCES_LIBRARY_NAME = "molecularfaces";

	/*
	 * Queue objects
	 */
	private Set<String> scriptResourcesToLoadInHead = new HashSet<>();
	private Set<String> scriptResourcesToLoadInBodyAtTop = new HashSet<>();
	private Set<String> scriptsExtToLoadInHead = new HashSet<>();
	private Set<String> cssResourcesToLoad = new HashSet<>();
	private Set<String> cssExtToLoad = new HashSet<>();

	/**
	 * Enqueues loading of a JavaScript resource file. The resource will be added
	 * via JSF's resource mechanism to the &lt;head&gt; when calling the
	 * {@link #processPostAddToViewEvent(FacesContext)} method.
	 * 
	 * @param resource name of the file in the web project's resource library
	 */
	public void addScriptResourceToHead(String resource) {
		scriptResourcesToLoadInHead.add(resource);
	}

	/**
	 * Enqueues loading of a JavaScript resource file. The resource will be added
	 * via JSF's resource mechanism to the top of &lt;body&gt; when calling the
	 * {@link #processPostAddToViewEvent(FacesContext)} method.
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
	 * Enqueues loading of a JavaScript file. The resource will be loaded in the
	 * &lt;head&gt; via the JavaScript class {@code molecularfaces.ResourcesLoader}.
	 * The code snippet can be requested via
	 * {@link #encodeLoadExtResources(String)}.
	 * 
	 * @param src path of the file
	 */
	public void addScriptExtToHead(String src) {
		scriptsExtToLoadInHead.add(src);
	}

	/**
	 * Enqueues loading of a stylesheet resource file. The resource will be added
	 * via JSF's resource mechanism when calling the
	 * {@link #processPostAddToViewEvent(FacesContext)} method.
	 * 
	 * @param resource name of the file in the web project's resource library
	 */
	public void addCssResource(String resource) {
		cssResourcesToLoad.add(resource);
	}

	/**
	 * Enqueues loading of a stylesheet file. The resource will be loaded via the
	 * JavaScript class {@code molecularfaces.ResourcesLoader}. The code snippet can
	 * be requested via {@link #encodeLoadExtResources(String)}.
	 * 
	 * @param href path of the file
	 */
	public void addCssExt(String href) {
		cssExtToLoad.add(href);
	}

	/**
	 * Adds all resources enqueued via {@link #addScriptResourceToHead(String)},
	 * {@link #addScriptResourceToBodyAtTop(String)} and
	 * {@link #addCssResource(String)} as JSF resource components to their
	 * respective targets and clears the queues.
	 * 
	 * @param context current faces context
	 */
	public void processPostAddToViewEvent(FacesContext context) {
		loadScriptResources(context);
		loadCssResources(context);
	}

	private void loadScriptResources(FacesContext context) {
		for (String resource : scriptResourcesToLoadInHead) {
			UIComponent component = createResourceComponent(resource, "javax.faces.resource.Script");
			addComponentToHead(component, context);
		}
		scriptResourcesToLoadInHead.clear();

		for (String resource : scriptResourcesToLoadInBodyAtTop) {
			UIComponent component = createResourceComponent(resource, "javax.faces.resource.Script");
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
			UIComponent component = createResourceComponent(resource, "javax.faces.resource.Stylesheet");
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

	/**
	 * Creates an inline JavaScript code fragment for loading resources that have
	 * been enqueued via {@link #addScriptExtToHead(String)} and
	 * {@link #addCssExt(String)}.
	 * 
	 * @param loaderJSVar JavaScript variable name of the
	 *                    {@code molecularfaces.ResourcesLoader} instance
	 * @return JavaScript code
	 */
	public StringBuilder encodeLoadExtResources(String loaderJSVar) {
		if (scriptsExtToLoadInHead.isEmpty() && cssExtToLoad.isEmpty()) {
			return new StringBuilder();
		} else {
			StringBuilder sb = new StringBuilder(256);
			Formatter fmt = new Formatter(sb);

			sb.append(loaderJSVar);
			for (String script : scriptsExtToLoadInHead) {
				fmt.format(".addScriptToHead(\"%s\")", script);
			}
			for (String href : cssExtToLoad) {
				fmt.format(".addCssToHead(\"%s\")", href);
			}
			sb.append(";");

			fmt.close();
			return sb;
		}
	}
}