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
package de.ipb_halle.molecularfaces.test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.html.HtmlBody;
import javax.faces.context.FacesContext;
import javax.faces.render.Renderer;

/**
 * Utility methods for testing.
 * 
 * @author flange
 */
public class TestUtils {
	private TestUtils() {
	};

	public static List<UIComponent> getResourceComponentsFromHead() {
		FacesContext context = FacesContext.getCurrentInstance();
		return context.getViewRoot().getComponentResources(context, "head");
	}

	public static List<UIComponent> getComponentsInBody() {
		UIComponent root = FacesContext.getCurrentInstance().getViewRoot();
		for (UIComponent component : root.getChildren()) {
			if (component instanceof HtmlBody) {
				return component.getChildren();
			}
		}

		return null;
	}

	public static List<UIComponent> matchingResourceComponentsInList(List<UIComponent> components, String rendererType,
			String name, String library) {
		return components.stream().filter(c -> c.getClass().equals(UIOutput.class))
				.filter(c -> c.getRendererType().equals(rendererType))
				.filter(c -> c.getAttributes().get("name").equals(name))
				.filter(c -> c.getAttributes().get("library").equals(library)).collect(Collectors.toList());
	}

	public static void encodeRenderer(Renderer renderer, FacesContext context, UIComponent component) throws IOException {
		renderer.encodeBegin(context, component);
		if (renderer.getRendersChildren()) {
			renderer.encodeChildren(context, component);
		}
		renderer.encodeEnd(context, component);
	}
}