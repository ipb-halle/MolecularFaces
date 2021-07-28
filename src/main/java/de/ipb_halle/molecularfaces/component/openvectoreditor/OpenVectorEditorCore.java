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

import javax.faces.component.UIInput;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.ListenerFor;
import javax.faces.event.PostAddToViewEvent;
import de.ipb_halle.molecularfaces.util.ResourceLoader;

/**
 * This class holds the attribute states of the &lt;mol:openVectorEditor&gt; component and
 * provides support for dynamic resource loading.
 * 
 * @author flange
 */
@ListenerFor(systemEventClass = PostAddToViewEvent.class)
public abstract class OpenVectorEditorCore extends UIInput implements ComponentSystemEventListener {
	/**
	 * Component family returned by {@link #getFamily()}
	 */
	public static final String COMPONENT_FAMILY = "molecularfaces.OpenVectorEditorFamily";

	private ResourceLoader resourceLoader = new ResourceLoader();

	@Override
	public String getFamily() {
		return COMPONENT_FAMILY;
	}

	protected enum PropertyKeys {
		readonly, widgetVar;
	}

	/**
	 * Return the value of the <code>readonly</code> property.
	 * <p>
	 * Flag indicating that this element is in editable or
	 * in read-only mode.
	 * 
	 * @return the value of the attribute or <code>false</code> if it has not been
	 *         set in the JSF view.
	 */
	public boolean isReadonly() {
		return (boolean) getStateHelper().eval(PropertyKeys.readonly, false);
	}

	/**
	 * Set the value of the <code>readonly</code> property.
	 * 
	 * @param readonly boolean value which indicates the rendering of the component in read-only mode
	 */
	public void setReadonly(boolean readonly) {
		getStateHelper().put(PropertyKeys.readonly, readonly);
	}

	/**
	 * Return the value of the <code>widgetVar</code> property.
	 * <p>
	 * The client-side variable name of a Promise object that embeds the plugin's
	 * JavaScript instance.
	 * 
	 * @return the value of the attribute.
	 */
	public String getWidgetVar() {
		return (String) getStateHelper().eval(PropertyKeys.widgetVar);
	}

	/**
	 * Set the value of the <code>widgetVar</code> property.
	 * 
	 * @param widgetVar name of the client-side Promise object that embeds the
	 *                  plugin's JavaScript instance
	 */
	public void setWidgetVar(String widgetVar) {
		getStateHelper().put(PropertyKeys.widgetVar, widgetVar);
	}

	/**
	 * Enqueues loading of a JavaScript resource file. The resource will be added
	 * via JSF's resource mechanism to the &lt;head&gt; when calling the
	 * {@link #processEvent(ComponentSystemEvent)} method in the PostAddToViewEvent
	 * event.
	 * 
	 * @param resource name of the file in the web project's resource library
	 */
	protected void addScriptResourceToHead(String resource) {
		resourceLoader.addScriptResourceToHead(resource);
	}

	/**
	 * Enqueues loading of a JavaScript resource file. The resource will be added
	 * via JSF's resource mechanism to the top of &lt;body&gt; when calling the
	 * {@link #processEvent(ComponentSystemEvent)} method in the PostAddToViewEvent
	 * event.
	 * <p>
	 * Note: There is no guarantee on the load order among the resources enqueued by
	 * this method.
	 * 
	 * @param resource name of the file in the web project's resource library
	 */
	protected void addScriptResourceToBodyAtTop(String resource) {
		resourceLoader.addScriptResourceToBodyAtTop(resource);
	}

	/**
	 * Enqueues loading of a JavaScript file. The resource will be loaded in the
	 * &lt;head&gt; via the JavaScript class {@code molecularfaces.ResourcesLoader}.
	 * 
	 * @param src path of the file
	 */
	protected void addScriptExtToHead(String src) {
		resourceLoader.addScriptExtToHead(src);
	}

	/**
	 * Enqueues loading of a stylesheet resource file. The resource will be added
	 * via JSF's resource mechanism by the
	 * {@link #processEvent(ComponentSystemEvent)} method in the PostAddToViewEvent
	 * event.
	 * 
	 * @param resource name of the file in the web project's resource library
	 */
	protected void addCssResource(String resource) {
		resourceLoader.addCssResource(resource);
	}

	/**
	 * Enqueues loading of a stylesheet file. The resource will be loaded
	 * via the JavaScript class {@code molecularfaces.ResourcesLoader}.
	 * 
	 * @param href path of the file
	 */
	protected void addCssExt(String href) {
		resourceLoader.addCssExt(href);
	}

	/*
	 * We would like to load resources programmatically (not via
	 * the @ResourceDependencies annotation). This has to be done before the render
	 * response, so an event listener for PostAddToViewEvent is registered
	 * via @ListenerFor to this component class, which is processed here.
	 * See: https://stackoverflow.com/a/12451778
	 */
	@Override
	public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
		if (event instanceof PostAddToViewEvent) {
			processPostAddToViewEvent();
			resourceLoader.processPostAddToViewEvent(getFacesContext());
		}

		super.processEvent(event);
	}

	/**
	 * Interested client components can override this method to execute code in the
	 * {@link PostAddToViewEvent} of the component before resource loading happens.
	 * The default implementation does nothing.
	 */
	protected void processPostAddToViewEvent() {
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
	protected StringBuilder encodeLoadExtResources(String loaderJSVar) {
		return resourceLoader.encodeLoadExtResources(loaderJSVar);
	}
}