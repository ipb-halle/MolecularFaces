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

import javax.faces.component.UIOutput;
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
	private Set<String> scriptResourcesToLoad = new HashSet<>();
	private Set<String> scriptsExtToLoad = new HashSet<>();
	private Set<String> cssResourcesToLoad = new HashSet<>();
	private Set<String> cssExtToLoad = new HashSet<>();

	/**
	 * Enqueues loading of a JavaScript resource file. The resource will be added
	 * via JSF's resource mechanism by the
	 * {@link #processPostAddToViewEvent(FacesContext)} method.
	 * 
	 * @param resource name of the file in the web project's resource library
	 */
	public void addScriptResource(String resource) {
		scriptResourcesToLoad.add(resource);
	}

	/**
	 * Enqueues loading of a JavaScript resource file. The resource will be loaded
	 * via the JavaScript class {@code molecularfaces.ResourcesLoader}. The code
	 * snippet can be requested via {@link #encodeLoadExtResources(String)}.
	 * 
	 * @param src path of the resource file
	 */
	public void addScriptExt(String src) {
		scriptsExtToLoad.add(src);
	}

	/**
	 * Enqueues loading of a stylesheet resource file. The resource will be added
	 * via JSF's resource mechanism by the
	 * {@link #processPostAddToViewEvent(FacesContext)} method.
	 * 
	 * @param resource name of the file in the web project's resource library
	 */
	public void addCssResource(String resource) {
		cssResourcesToLoad.add(resource);
	}

	/**
	 * Enqueues loading of a stylesheet resource file. The resource will be loaded
	 * via the JavaScript class {@code molecularfaces.ResourcesLoader}. The code
	 * snippet can be requested via {@link #encodeLoadExtResources(String)}.
	 * 
	 * @param href path of the resource file
	 */
	public void addCssExt(String href) {
		cssExtToLoad.add(href);
	}

	/**
	 * Adds all resources enqueued via {@link #addScriptResource(String)} and
	 * {@link #addCssResource(String)} as JSF resource components to &lt;head&gt;.
	 * 
	 * @param context current faces context
	 */
	public void processPostAddToViewEvent(FacesContext context) {
		loadScriptResources(context);
		loadCssResources(context);
	}

	private void loadCssResources(FacesContext context) {
		for (String resource : cssResourcesToLoad) {
			loadResource(resource, "javax.faces.resource.Stylesheet", context);
		}
	}

	private void loadScriptResources(FacesContext context) {
		for (String resource : scriptResourcesToLoad) {
			loadResource(resource, "javax.faces.resource.Script", context);
		}
	}

	private void loadResource(String resourceName, String rendererType, FacesContext context) {
		/*
		 * Create an UIComponent that includes the resource from the resource library.
		 */
		UIOutput resourceComponent = new UIOutput();
		resourceComponent.setRendererType(rendererType);
		resourceComponent.getAttributes().put("library", RESOURCES_LIBRARY_NAME);
		resourceComponent.getAttributes().put("name", resourceName);

		// Add the component to <head>.
		context.getViewRoot().addComponentResource(context, resourceComponent, "head");
	}

	/**
	 * Creates an inline JavaScript code fragment for loading resources that have
	 * been enqueued via {@link #addScriptExt(String)} and
	 * {@link #addCssExt(String)}.
	 * 
	 * @param loaderJSVar JavaScript variable name of the
	 *                    {@code molecularfaces.ResourcesLoader} instance
	 * @return JavaScript code
	 */
	public StringBuilder encodeLoadExtResources(String loaderJSVar) {
		if (scriptsExtToLoad.isEmpty() && cssExtToLoad.isEmpty()) {
			return new StringBuilder();
		} else {
			StringBuilder sb = new StringBuilder(256);
			Formatter fmt = new Formatter(sb);

			sb.append(loaderJSVar);
			for (String script : scriptsExtToLoad) {
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