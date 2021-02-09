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

import java.io.IOException;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

/**
 * This {@link UIComponent} renders &lt;link rel=&quot;stylesheet&quot;
 * type=&quot;text/css&quot; href=&quot;...&quot; /&gt;.
 * <p>
 * Use the constructor {@link ExtCSSResource(String)} to specify the location of
 * the CSS file that is referenced to in the {@code href} attribute.
 * <p>
 * Note: This component renders only once in its lifecycle.
 * 
 * @author flange
 */
@FacesComponent
public class ExtCSSResource extends UIOutput {
	public ExtCSSResource() {
		super();
		setRendererType(null);
	}

	public ExtCSSResource(String href) {
		this();
		setHref(href);
	}

	public String getHref() {
		return (String) getStateHelper().eval("href");
	}

	public void setHref(String href) {
		getStateHelper().put("href", href);
	}

	public boolean getAlreadyRendered() {
		return (boolean) getStateHelper().eval("alreadyRendered", false);
	}

	public void setAlreadyRendered(boolean alreadyRendered) {
		getStateHelper().put("alreadyRendered", alreadyRendered);
	}

	/**
	 * Renders renders &lt;link rel=&quot;stylesheet&quot; type=&quot;text/css&quot;
	 * href=&quot;...&quot; /&gt; if it has not been rendered before.
	 */
	@Override
	public void encodeBegin(FacesContext context) throws IOException {
		/*
		 * This method is executed in each request/response cycle after the component
		 * has been added to <head>. Execution in each AJAX call would clutter the
		 * <head> with one and the same resource HTML tag. Thus, this component has to
		 * be rendered only once.
		 * 
		 * Problem: The JSF framework does not reuse the same UIComponent object between
		 * different request/response cycles, so we cannot use a simple object property
		 * here to denote if the component was already rendered.
		 * 
		 * Solution: Abuse the StateHelper for this purpose.
		 * 
		 * To be honest, this is a very dirty trick!
		 */
		if (getAlreadyRendered()) {
			return;
		}
		
		ResponseWriter writer = context.getResponseWriter();
		writer.startElement("link", this);
		writer.writeAttribute("rel", "stylesheet", null);
		writer.writeAttribute("type", "text/css", null);
		writer.writeAttribute("href", getHref(), null);
		writer.endElement("link");

		setAlreadyRendered(true);
	};
}