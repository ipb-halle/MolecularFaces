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

import static de.ipb_halle.molecularfaces.util.ResourceLoader.RESOURCES_LIBRARY_NAME;
import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.html.HtmlBody;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.Renderer;

import org.apache.commons.io.IOUtils;

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
			String name) {
		return components.stream().filter(c -> c.getClass().equals(UIOutput.class))
				.filter(c -> c.getRendererType().equals(rendererType))
				.filter(c -> c.getAttributes().get("name").equals(name))
				.filter(c -> c.getAttributes().get("library").equals(RESOURCES_LIBRARY_NAME)).collect(toList());
	}

	public static List<UIComponent> matchingResourceComponentsInList(List<UIComponent> components, String rendererType,
			String name, Map<String, Object> attributesToFilter) {
		Stream<UIComponent> stream = matchingResourceComponentsInList(components, rendererType, name).stream();

		for (Entry<String, Object> entry : attributesToFilter.entrySet()) {
			stream = stream.filter(c -> c.getAttributes().get(entry.getKey()).equals(entry.getValue()));
		}

		return stream.collect(toList());
	}

	public static void encodeRenderer(Renderer renderer, FacesContext context, UIComponent component)
			throws IOException {
		renderer.encodeBegin(context, component);
		if (renderer.getRendersChildren()) {
			renderer.encodeChildren(context, component);
		}
		renderer.encodeEnd(context, component);
	}

	public static <T> String readResourceFile(Class<T> clazz, String fileName) throws IOException {
		return IOUtils.toString(clazz.getResourceAsStream(fileName), "UTF-8");
	}

	public static <T> String readResourceFileIgnoreNewlinesAndTabs(Class<T> clazz, String fileName) throws IOException {
		return readResourceFile(clazz, fileName).replace("\n", "").replace("\t", "");
	}
}
