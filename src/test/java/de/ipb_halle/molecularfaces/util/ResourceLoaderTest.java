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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlBody;
import javax.faces.context.FacesContext;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.event.PreRenderComponentEvent;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.ipb_halle.molecularfaces.test.MockedJSFContainerRule;

/**
 * 
 * @author flange
 */
public class ResourceLoaderTest {
	private FacesContext context;
	private UIViewRoot root;
	private HtmlBody body;
	private UIComponent component;
	private ResourceLoader loader;

	private String javaScript = "javax.faces.resource.Script";
	private String stylesheet = "javax.faces.resource.Stylesheet";
	private String libraryName = "molecularfaces";

	@Rule
	public MockedJSFContainerRule rule = new MockedJSFContainerRule();

	@Before
	public void init() {
		context = rule.getContainer().getFacesContext();
		root = context.getViewRoot();

		body = new HtmlBody();
		root.getChildren().add(body);

		component = new UIInput();
		loader = new ResourceLoader(component);
	}

	@Test
	public void test_withoutEnqueuedResources_processEvent() {
		assertThat(getComponentsInHead(), hasSize(0));
		assertThat(getComponentsInBody(), hasSize(0));

		loader.processEvent(new PostAddToViewEvent(component));

		assertThat(getComponentsInHead(), hasSize(0));
		assertThat(getComponentsInBody(), hasSize(0));
	}

	@Test
	public void test_enqueueResources_processEventWithoutPostAddToViewEvent() {
		enqueueResources();
		assertThat(getComponentsInHead(), hasSize(0));
		assertThat(getComponentsInBody(), hasSize(0));

		loader.processEvent(new PreRenderComponentEvent(component));

		assertThat(getComponentsInHead(), hasSize(0));
		assertThat(getComponentsInBody(), hasSize(0));
	}

	@Test
	public void test_enqueueResources_processEventWithPostAddToViewEvent_thenCheckResources() {
		enqueueResources();
		assertThat(getComponentsInHead(), hasSize(0));

		/*
		 * Add some components to the body, so we can assert on the components' ordering
		 * later.
		 */
		UIOutput outputComponent = new UIOutput();
		UIInput inputComponent = new UIInput();
		getComponentsInBody().add(outputComponent);
		getComponentsInBody().add(inputComponent);
		assertThat(getComponentsInBody(), hasSize(2));

		/*
		 * Repeat this a few times to make sure that already loaded resources are
		 * removed from the queues and do not load again.
		 */
		for (int i = 0; i < 5; i++) {
			loader.processEvent(new PostAddToViewEvent(component));

			List<UIComponent> componentsInHead = getComponentsInHead();
			assertThat(componentsInHead, hasSize(4));
			assertThat(matchingComponentsInList(componentsInHead, javaScript, "ScriptResourceToHead1", libraryName), hasSize(1));
			assertThat(matchingComponentsInList(componentsInHead, javaScript, "ScriptResourceToHead2", libraryName), hasSize(1));
			assertThat(matchingComponentsInList(componentsInHead, stylesheet, "CssResource1", libraryName), hasSize(1));
			assertThat(matchingComponentsInList(componentsInHead, stylesheet, "CssResource2", libraryName), hasSize(1));

			List<UIComponent> componentsInBody = getComponentsInBody();
			assertThat(componentsInBody, hasSize(4));
			assertThat(matchingComponentsInList(componentsInBody, javaScript, "ScriptResourceToBodyAtTop1", libraryName), hasSize(1));
			assertThat(matchingComponentsInList(componentsInBody, javaScript, "ScriptResourceToBodyAtTop2", libraryName), hasSize(1));
			assertEquals(outputComponent, componentsInBody.get(2));
			assertEquals(inputComponent, componentsInBody.get(3));
		}
	}

	@Test
	public void test_withoutEnqueuedResources_encodeLoadExtResources() {
		StringBuilder result = loader.encodeLoadExtResources("myLoader");
		assertThat(result.length(), is(0));
	}

	@Test
	public void test_enqueueResources_encodeLoadExtResources_thenCheckString() {
		enqueueResources();
		String result = loader.encodeLoadExtResources("myLoader").toString();
		String expected = "myLoader"
				+ ".addScriptToHead(\"ScriptExtToHead2\")"
				+ ".addScriptToHead(\"ScriptExtToHead1\")"
				+ ".addScriptToBodyAtTop(\"ScriptExtToBodyAtTop2\")"
				+ ".addScriptToBodyAtTop(\"ScriptExtToBodyAtTop1\")"
				+ ".addCssToHead(\"CssExt2\")"
				+ ".addCssToHead(\"CssExt1\");";

		assertEquals(expected, result);
	}

	private void enqueueResources() {
		loader.addScriptResourceToHead("ScriptResourceToHead1");
		loader.addScriptResourceToHead("ScriptResourceToHead2");
		loader.addScriptResourceToBodyAtTop("ScriptResourceToBodyAtTop1");
		loader.addScriptResourceToBodyAtTop("ScriptResourceToBodyAtTop2");
		loader.addScriptExtToHead("ScriptExtToHead1");
		loader.addScriptExtToHead("ScriptExtToHead2");
		loader.addScriptExtToBodyAtTop("ScriptExtToBodyAtTop1");
		loader.addScriptExtToBodyAtTop("ScriptExtToBodyAtTop2");
		loader.addCssResource("CssResource1");
		loader.addCssResource("CssResource2");
		loader.addCssExt("CssExt1");
		loader.addCssExt("CssExt2");
	}

	private List<UIComponent> getComponentsInHead() {
		return root.getComponentResources(context, "head");
	}

	private List<UIComponent> matchingComponentsInList(List<UIComponent> components, String rendererType, String name,
			String library) {
		return components.stream()
				.filter(c -> c.getRendererType().equals(rendererType))
				.filter(c -> c.getAttributes().get("name").equals(name))
				.filter(c -> c.getAttributes().get("library").equals(library))
				.collect(Collectors.toList());
	}

	private List<UIComponent> getComponentsInBody() {
		return body.getChildren();
	}
}