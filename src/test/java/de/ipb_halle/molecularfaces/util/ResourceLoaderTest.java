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

import static de.ipb_halle.molecularfaces.test.TestUtils.getComponentsInBody;
import static de.ipb_halle.molecularfaces.test.TestUtils.getResourceComponentsFromHead;
import static de.ipb_halle.molecularfaces.test.TestUtils.matchingResourceComponentsInList;
import static de.ipb_halle.molecularfaces.util.ResourceLoader.JAVASCRIPT;
import static de.ipb_halle.molecularfaces.util.ResourceLoader.JAVASCRIPT_FACET_NAME;
import static de.ipb_halle.molecularfaces.util.ResourceLoader.STYLESHEET;
import static de.ipb_halle.molecularfaces.util.ResourceLoader.STYLESHEET_FACET_NAME;
import static java.util.Collections.EMPTY_SET;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UIOutput;
import jakarta.faces.event.PostAddToViewEvent;
import jakarta.faces.event.PreRenderComponentEvent;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.ipb_halle.molecularfaces.test.MockedJSFContainerRule;

/**
 * 
 * @author flange
 */
public class ResourceLoaderTest {
	private UIComponent component;
	private ResourceLoader loader;

	@Rule
	public MockedJSFContainerRule rule = new MockedJSFContainerRule();

	@Before
	public void init() {
		component = new UIInput();
		loader = new ResourceLoader(component);
	}

	@Test
	public void test_checkQueueGetters_withoutEnqueuedResources() {
		assertEquals(EMPTY_SET,	loader.getScriptResourcesToLoadInHead());
		assertEquals(EMPTY_SET, loader.getScriptResourcesToLoadInBodyAtTop());
		assertEquals(EMPTY_SET, loader.getScriptsExtToLoadInHead());
		assertEquals(EMPTY_SET,	loader.getScriptsExtToLoadInBodyAtTop());
		assertEquals(EMPTY_SET, loader.getCssResourcesToLoad());
		assertEquals(EMPTY_SET, loader.getCssExtToLoad());
	}

	@Test
	public void test_queueGetters_areImmutable() {
		assertThrows(UnsupportedOperationException.class, () -> loader.getScriptResourcesToLoadInHead().add("abc"));
		assertThrows(UnsupportedOperationException.class, () -> loader.getScriptResourcesToLoadInBodyAtTop().add("abc"));
		assertThrows(UnsupportedOperationException.class, () -> loader.getScriptsExtToLoadInHead().add("abc"));
		assertThrows(UnsupportedOperationException.class, () -> loader.getScriptsExtToLoadInBodyAtTop().add("abc"));
		assertThrows(UnsupportedOperationException.class, () -> loader.getCssResourcesToLoad().add("abc"));
		assertThrows(UnsupportedOperationException.class, () -> loader.getCssExtToLoad().add("abc"));
	}

	@Test
	public void test_enqueueResources_checkQueueGetters() {
		enqueueResources();

		assertEquals(makeSet("ScriptResourceToHead1", "ScriptResourceToHead2"),
				loader.getScriptResourcesToLoadInHead());
		assertEquals(makeSet("ScriptResourceToBodyAtTop1", "ScriptResourceToBodyAtTop2"),
				loader.getScriptResourcesToLoadInBodyAtTop());
		assertEquals(makeSet("ScriptExtToHead1", "ScriptExtToHead2"), loader.getScriptsExtToLoadInHead());
		assertEquals(makeSet("ScriptExtToBodyAtTop1", "ScriptExtToBodyAtTop2"),
				loader.getScriptsExtToLoadInBodyAtTop());
		assertEquals(makeSet("CssResource1", "CssResource2"), loader.getCssResourcesToLoad());
		assertEquals(makeSet("CssExt1", "CssExt2"), loader.getCssExtToLoad());
	}

	@Test
	public void test_withoutEnqueuedResources_processEvent() {
		assertThat(getResourceComponentsFromHead(), hasSize(0));
		assertThat(getComponentsInBody(), hasSize(0));

		loader.processEvent(new PostAddToViewEvent(component));

		assertThat(getResourceComponentsFromHead(), hasSize(0));
		assertThat(getComponentsInBody(), hasSize(0));
	}

	@Test
	public void test_enqueueResources_processEventWithoutPostAddToViewEvent() {
		enqueueResources();
		assertThat(getResourceComponentsFromHead(), hasSize(0));
		assertThat(getComponentsInBody(), hasSize(0));

		loader.processEvent(new PreRenderComponentEvent(component));

		assertThat(getResourceComponentsFromHead(), hasSize(0));
		assertThat(getComponentsInBody(), hasSize(0));
	}

	@Test
	public void test_enqueueResources_processEventWithPostAddToViewEvent_thenCheckResources() {
		enqueueResources();
		assertThat(getResourceComponentsFromHead(), hasSize(0));

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

			List<UIComponent> componentsInHead = getResourceComponentsFromHead();
			assertThat(componentsInHead, hasSize(4));
			assertThat(matchingResourceComponentsInList(componentsInHead, JAVASCRIPT, "ScriptResourceToHead1"), hasSize(1));
			assertThat(matchingResourceComponentsInList(componentsInHead, JAVASCRIPT, "ScriptResourceToHead2"), hasSize(1));
			assertThat(matchingResourceComponentsInList(componentsInHead, STYLESHEET, "CssResource1"), hasSize(1));
			assertThat(matchingResourceComponentsInList(componentsInHead, STYLESHEET, "CssResource2"), hasSize(1));

			List<UIComponent> componentsInBody = getComponentsInBody();
			assertThat(componentsInBody, hasSize(4));
			assertThat(matchingResourceComponentsInList(componentsInBody, JAVASCRIPT, "ScriptResourceToBodyAtTop1"), hasSize(1));
			assertThat(matchingResourceComponentsInList(componentsInBody, JAVASCRIPT, "ScriptResourceToBodyAtTop2"), hasSize(1));
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

	@Test
	public void test_addScriptResourceAsFacetComponent() {
		Map<String, Object> expectedAttributes = new HashMap<>();
		expectedAttributes.put("external", Boolean.FALSE);

		assertThat(component.getFacets().size(), is(0));

		loader.addScriptResourceAsFacetComponent("ScriptResourceAsFacetComponent1");
		loader.addScriptResourceAsFacetComponent("ScriptResourceAsFacetComponent2");

		assertThat(component.getFacets().size(), is(1));
		UIComponent facet = component.getFacet(JAVASCRIPT_FACET_NAME);
		assertNotNull(facet);
		List<UIComponent> children = facet.getChildren();
		assertThat(children, hasSize(2));
		assertThat(matchingResourceComponentsInList(children, JAVASCRIPT, "ScriptResourceAsFacetComponent1", expectedAttributes), hasSize(1));
		assertThat(matchingResourceComponentsInList(children, JAVASCRIPT, "ScriptResourceAsFacetComponent2", expectedAttributes), hasSize(1));
	}

	@Test
	public void test_addScriptExtAsFacetComponent() {
		Map<String, Object> expectedAttributes = new HashMap<>();
		expectedAttributes.put("external", Boolean.TRUE);

		assertThat(component.getFacets().size(), is(0));

		loader.addScriptExtAsFacetComponent("ScriptExtAsFacetComponent1");
		loader.addScriptExtAsFacetComponent("ScriptExtAsFacetComponent2");

		assertThat(component.getFacets().size(), is(1));
		UIComponent facet = component.getFacet(JAVASCRIPT_FACET_NAME);
		assertNotNull(facet);
		List<UIComponent> children = facet.getChildren();
		assertThat(children, hasSize(2));
		assertThat(matchingResourceComponentsInList(children, JAVASCRIPT, "ScriptExtAsFacetComponent1", expectedAttributes), hasSize(1));
		assertThat(matchingResourceComponentsInList(children, JAVASCRIPT, "ScriptExtAsFacetComponent2", expectedAttributes), hasSize(1));
	}

	@Test
	public void test_addCssResourceAsFacetComponent() {
		Map<String, Object> expectedAttributes = new HashMap<>();
		expectedAttributes.put("external", Boolean.FALSE);

		assertThat(component.getFacets().size(), is(0));

		loader.addCssResourceAsFacetComponent("CssResourceAsFacetComponent1");
		loader.addCssResourceAsFacetComponent("CssResourceAsFacetComponent2");

		assertThat(component.getFacets().size(), is(1));
		UIComponent facet = component.getFacet(STYLESHEET_FACET_NAME);
		assertNotNull(facet);
		List<UIComponent> children = facet.getChildren();
		assertThat(children, hasSize(2));
		assertThat(matchingResourceComponentsInList(children, STYLESHEET, "CssResourceAsFacetComponent1", expectedAttributes), hasSize(1));
		assertThat(matchingResourceComponentsInList(children, STYLESHEET, "CssResourceAsFacetComponent2", expectedAttributes), hasSize(1));
	}

	@Test
	public void test_addCssExtAsFacetComponent() {
		Map<String, Object> expectedAttributes = new HashMap<>();
		expectedAttributes.put("external", Boolean.TRUE);

		assertThat(component.getFacets().size(), is(0));

		loader.addCssExtAsFacetComponent("CssExtAsFacetComponent1");
		loader.addCssExtAsFacetComponent("CssExtAsFacetComponent2");

		assertThat(component.getFacets().size(), is(1));
		UIComponent facet = component.getFacet(STYLESHEET_FACET_NAME);
		assertNotNull(facet);
		List<UIComponent> children = facet.getChildren();
		assertThat(children, hasSize(2));
		assertThat(matchingResourceComponentsInList(children, STYLESHEET, "CssExtAsFacetComponent1", expectedAttributes), hasSize(1));
		assertThat(matchingResourceComponentsInList(children, STYLESHEET, "CssExtAsFacetComponent2", expectedAttributes), hasSize(1));
	}

	private static Set<String> makeSet(String... elements) {
		Set<String> result = new HashSet<>();
		for (String element : elements) {
			result.add(element);
		}
		return result;
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
}
