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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlBody;
import javax.faces.context.FacesContext;
import javax.faces.event.PostAddToViewEvent;

import org.apache.myfaces.test.mock.MockServletContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.ipb_halle.molecularfaces.test.MockedJSFContainerRule;
import de.ipb_halle.molecularfaces.util.ResourceLoader;
import de.ipb_halle.molecularfaces.util.WebXmlImpl;

public class OpenVectorEditorComponentTest {
	private FacesContext context;
	private MockServletContext servletContext;
	private UIViewRoot root;
	private OpenVectorEditorComponent comp;

	private String javaScript = "javax.faces.resource.Script";
	private String stylesheet = "javax.faces.resource.Stylesheet";
	private String libraryName = ResourceLoader.RESOURCES_LIBRARY_NAME;

	@Rule
	public MockedJSFContainerRule rule = new MockedJSFContainerRule();

	@Before
	public void init() {
		context = rule.getContainer().getFacesContext();
		servletContext = rule.getContainer().getServletContext();
		root = context.getViewRoot();

		root.getChildren().add(new HtmlBody());

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
		servletContext.addInitParameter(WEBXML_CUSTOM_RESOURCE_BASE_URL, "baseUrl");

		comp = new OpenVectorEditorComponent();
		ResourceLoader loader = comp.getResourceLoader();

		assertThat(loader.getScriptResourcesToLoadInHead(), hasSize(1));
		assertThat(loader.getScriptResourcesToLoadInBodyAtTop(), hasSize(0));
		assertThat(loader.getScriptsExtToLoadInHead(), hasSize(0));
		assertThat(loader.getScriptsExtToLoadInBodyAtTop(), hasSize(1));
		assertThat(loader.getCssResourcesToLoad(), hasSize(0));
		assertThat(loader.getCssExtToLoad(), hasSize(1));
		assertTrue(loader.getScriptResourcesToLoadInHead().contains("js/MolecularFaces.min.js"));
		assertTrue(loader.getScriptsExtToLoadInBodyAtTop().contains("baseUrl/open-vector-editor.min.js"));
		assertTrue(loader.getCssExtToLoad().contains("baseUrl/main.css"));

		// Also test adding resources via the JSF event mechanism.
		rule.getContainer().getApplication().publishEvent(context, PostAddToViewEvent.class, comp);

		List<UIComponent> componentsInHead = getResourceComponentsFromHead();
		List<UIComponent> componentsInBody = getComponentsInBody();
		assertThat(componentsInHead, hasSize(1));
		assertThat(matchingResourceComponentsInList(componentsInHead, javaScript, "js/MolecularFaces.min.js", libraryName), hasSize(1));
		assertThat(componentsInBody, hasSize(0));
	}

	@Test
	public void test_enqueuedResources_withoutContextParam() {
		assertNull(new WebXmlImpl().getContextParam(WEBXML_CUSTOM_RESOURCE_BASE_URL, context, null));
		checkResourceLoadingViaResourceLoaderAndViaJSFEvent();
	}

	@Test
	public void test_enqueuedResources_withEmptyContextParam() {
		servletContext.addInitParameter(WEBXML_CUSTOM_RESOURCE_BASE_URL, "");
		assertEquals("", new WebXmlImpl().getContextParam(WEBXML_CUSTOM_RESOURCE_BASE_URL, context, null));
		checkResourceLoadingViaResourceLoaderAndViaJSFEvent();
	}

	private void checkResourceLoadingViaResourceLoaderAndViaJSFEvent() {
		comp = new OpenVectorEditorComponent();
		ResourceLoader loader = comp.getResourceLoader();

		assertThat(loader.getScriptResourcesToLoadInHead(), hasSize(1));
		assertThat(loader.getScriptResourcesToLoadInBodyAtTop(), hasSize(1));
		assertThat(loader.getScriptsExtToLoadInHead(), hasSize(0));
		assertThat(loader.getScriptsExtToLoadInBodyAtTop(), hasSize(0));
		assertThat(loader.getCssResourcesToLoad(), hasSize(0));
		assertThat(loader.getCssExtToLoad(), hasSize(0));
		assertTrue(loader.getScriptResourcesToLoadInHead().contains("js/MolecularFaces.min.js"));
		assertTrue(loader.getScriptResourcesToLoadInBodyAtTop().contains("plugins/openVectorEditor/open-vector-editor.min.js"));
		assertThat(matchingResourceComponentsInList(comp.getFacet(ResourceLoader.FACET_NAME).getChildren(), stylesheet, "plugins/openVectorEditor/main.css", libraryName), hasSize(1));

		// Also test adding resources via the JSF event mechanism.
		rule.getContainer().getApplication().publishEvent(context, PostAddToViewEvent.class, comp);

		List<UIComponent> componentsInHead = getResourceComponentsFromHead();
		List<UIComponent> componentsInBody = getComponentsInBody();
		assertThat(componentsInHead, hasSize(1));
		assertThat(matchingResourceComponentsInList(componentsInHead, javaScript, "js/MolecularFaces.min.js", libraryName), hasSize(1));
		assertThat(matchingResourceComponentsInList(comp.getFacet(ResourceLoader.FACET_NAME).getChildren(), stylesheet, "plugins/openVectorEditor/main.css", libraryName), hasSize(1));
		assertThat(componentsInBody, hasSize(1));
		assertThat(matchingResourceComponentsInList(componentsInBody, javaScript, "plugins/openVectorEditor/open-vector-editor.min.js", libraryName), hasSize(1));
	}
}