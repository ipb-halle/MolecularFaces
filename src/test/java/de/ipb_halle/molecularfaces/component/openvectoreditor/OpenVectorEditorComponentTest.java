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

import static de.ipb_halle.molecularfaces.component.openvectoreditor.OpenVectorEditorComponent.WEBXML_CUSTOM_RESOURCE_BASE_URL;
import static de.ipb_halle.molecularfaces.test.TestUtils.getComponentsInBody;
import static de.ipb_halle.molecularfaces.test.TestUtils.getResourceComponentsFromHead;
import static de.ipb_halle.molecularfaces.test.TestUtils.matchingResourceComponentsInList;
import static de.ipb_halle.molecularfaces.util.ResourceLoader.JAVASCRIPT;
import static de.ipb_halle.molecularfaces.util.ResourceLoader.STYLESHEET;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PostAddToViewEvent;

import org.apache.myfaces.test.mock.MockServletContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.ipb_halle.molecularfaces.test.MockedJSFContainerRule;
import de.ipb_halle.molecularfaces.util.ResourceLoader;
import de.ipb_halle.molecularfaces.util.WebXmlImpl;

/**
 * 
 * @author flange
 */
public class OpenVectorEditorComponentTest {
	private FacesContext context;
	private MockServletContext servletContext;
	private OpenVectorEditorComponent comp;

	@Rule
	public MockedJSFContainerRule rule = new MockedJSFContainerRule();

	@Before
	public void init() {
		context = rule.getContainer().getFacesContext();
		servletContext = rule.getContainer().getServletContext();
		comp = new OpenVectorEditorComponent();
	}

	@Test
	public void test_componentFamily() {
		assertEquals(OpenVectorEditorCore.COMPONENT_FAMILY, comp.getFamily());
	}

	@Test
	public void test_rendererType() {
		assertEquals(OpenVectorEditorComponent.DEFAULT_RENDERER, comp.getRendererType());
	}

	@Test
	public void test_gettersAndSettersAndDefaults() {
		assertFalse(comp.isReadonly());
		comp.setReadonly(true);
		assertTrue(comp.isReadonly());

		assertNull(comp.getWidgetVar());
		comp.setWidgetVar("myWidgetVar");
		assertEquals("myWidgetVar", comp.getWidgetVar());
	}

	@Test
	public void test_enqueuedResources_withContextParam() {
		Map<String, Object> expectedAttributes = new HashMap<>();
		expectedAttributes.put("external", Boolean.TRUE);

		servletContext.addInitParameter(WEBXML_CUSTOM_RESOURCE_BASE_URL, "baseUrl");

		comp = new OpenVectorEditorComponent();
		ResourceLoader loader = comp.getResourceLoader();

		assertThat(loader.getScriptResourcesToLoadInHead(), hasSize(1));
		assertThat(loader.getScriptResourcesToLoadInBodyAtTop(), hasSize(0));
		assertThat(loader.getScriptsExtToLoadInHead(), hasSize(0));
		assertThat(loader.getScriptsExtToLoadInBodyAtTop(), hasSize(0));
		assertThat(loader.getCssResourcesToLoad(), hasSize(0));
		assertThat(loader.getCssExtToLoad(), hasSize(0));
		assertTrue(loader.getScriptResourcesToLoadInHead().contains("js/MolecularFaces.min.js"));

		// Also test adding resources via the JSF event mechanism.
		rule.getContainer().getApplication().publishEvent(context, PostAddToViewEvent.class, comp);

		List<UIComponent> componentsInHead = getResourceComponentsFromHead();
		List<UIComponent> componentsInBody = getComponentsInBody();
		List<UIComponent> cssFacetChildren = comp.getFacet(ResourceLoader.STYLESHEET_FACET_NAME).getChildren();
		List<UIComponent> jsFacetChildren = comp.getFacet(ResourceLoader.JAVASCRIPT_FACET_NAME).getChildren();
		assertThat(componentsInHead, hasSize(1));
		assertThat(matchingResourceComponentsInList(componentsInHead, JAVASCRIPT, "js/MolecularFaces.min.js"), hasSize(1));
		assertThat(componentsInBody, hasSize(0));
		assertThat(cssFacetChildren, hasSize(1));
		assertThat(matchingResourceComponentsInList(cssFacetChildren, STYLESHEET, "baseUrl/main.css", expectedAttributes), hasSize(1));
		assertThat(jsFacetChildren, hasSize(1));
		assertThat(matchingResourceComponentsInList(jsFacetChildren, JAVASCRIPT, "baseUrl/open-vector-editor.min.js", expectedAttributes), hasSize(1));
	}

	@Test
	public void test_enqueuedResources_withoutContextParam() {
		assertNull(new WebXmlImpl().getContextParam(WEBXML_CUSTOM_RESOURCE_BASE_URL, context, null));
		checkResourceLoadingViaResourceLoaderAndViaJSF();
	}

	@Test
	public void test_enqueuedResources_withEmptyContextParam() {
		servletContext.addInitParameter(WEBXML_CUSTOM_RESOURCE_BASE_URL, "");
		assertEquals("", new WebXmlImpl().getContextParam(WEBXML_CUSTOM_RESOURCE_BASE_URL, context, null));
		checkResourceLoadingViaResourceLoaderAndViaJSF();
	}

	private void checkResourceLoadingViaResourceLoaderAndViaJSF() {
		Map<String, Object> expectedAttributes = new HashMap<>();
		expectedAttributes.put("external", Boolean.FALSE);
		comp = new OpenVectorEditorComponent();
		ResourceLoader loader = comp.getResourceLoader();

		assertThat(loader.getScriptResourcesToLoadInHead(), hasSize(1));
		assertThat(loader.getScriptResourcesToLoadInBodyAtTop(), hasSize(0));
		assertThat(loader.getScriptsExtToLoadInHead(), hasSize(0));
		assertThat(loader.getScriptsExtToLoadInBodyAtTop(), hasSize(0));
		assertThat(loader.getCssResourcesToLoad(), hasSize(0));
		assertThat(loader.getCssExtToLoad(), hasSize(0));
		assertTrue(loader.getScriptResourcesToLoadInHead().contains("js/MolecularFaces.min.js"));

		List<UIComponent> cssFacetChildren = comp.getFacet(ResourceLoader.STYLESHEET_FACET_NAME).getChildren();
		List<UIComponent> jsFacetChildren = comp.getFacet(ResourceLoader.JAVASCRIPT_FACET_NAME).getChildren();
		assertThat(cssFacetChildren, hasSize(1));
		assertThat(matchingResourceComponentsInList(cssFacetChildren, STYLESHEET, "plugins/openVectorEditor/main.css", expectedAttributes), hasSize(1));
		assertThat(jsFacetChildren, hasSize(1));
		assertThat(matchingResourceComponentsInList(jsFacetChildren, JAVASCRIPT, "plugins/openVectorEditor/open-vector-editor.min.js", expectedAttributes), hasSize(1));

		// Also test adding resources via the JSF event mechanism.
		rule.getContainer().getApplication().publishEvent(context, PostAddToViewEvent.class, comp);

		List<UIComponent> componentsInHead = getResourceComponentsFromHead();
		List<UIComponent> componentsInBody = getComponentsInBody();
		assertThat(componentsInHead, hasSize(1));
		assertThat(matchingResourceComponentsInList(componentsInHead, JAVASCRIPT, "js/MolecularFaces.min.js"), hasSize(1));
		assertThat(componentsInBody, hasSize(0));

		cssFacetChildren = comp.getFacet(ResourceLoader.STYLESHEET_FACET_NAME).getChildren();
		jsFacetChildren = comp.getFacet(ResourceLoader.JAVASCRIPT_FACET_NAME).getChildren();
		assertThat(cssFacetChildren, hasSize(1));
		assertThat(matchingResourceComponentsInList(cssFacetChildren, STYLESHEET, "plugins/openVectorEditor/main.css", expectedAttributes), hasSize(1));
		assertThat(jsFacetChildren, hasSize(1));
		assertThat(matchingResourceComponentsInList(jsFacetChildren, JAVASCRIPT, "plugins/openVectorEditor/open-vector-editor.min.js", expectedAttributes), hasSize(1));
	}
}
