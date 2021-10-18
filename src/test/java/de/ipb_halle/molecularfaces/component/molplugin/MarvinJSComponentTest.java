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
package de.ipb_halle.molecularfaces.component.molplugin;

import static de.ipb_halle.molecularfaces.component.molplugin.MarvinJSComponent.WEBXML_MARVINJS_BASE_URL;
import static de.ipb_halle.molecularfaces.component.molplugin.MarvinJSComponent.WEBXML_MARVINJS_WEBSERVICES;
import static de.ipb_halle.molecularfaces.test.TestUtils.getComponentsInBody;
import static de.ipb_halle.molecularfaces.test.TestUtils.getResourceComponentsFromHead;
import static de.ipb_halle.molecularfaces.test.TestUtils.matchingResourceComponentsInList;
import static de.ipb_halle.molecularfaces.util.ResourceLoader.JAVASCRIPT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.PostAddToViewEvent;

import org.apache.myfaces.test.mock.MockServletContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.ipb_halle.molecularfaces.test.MockedJSFContainerRule;
import de.ipb_halle.molecularfaces.util.ResourceLoader;

/**
 * 
 * @author flange
 */
public class MarvinJSComponentTest {
	private FacesContext context;
	private MockServletContext servletContext;
	private MarvinJSComponent comp;

	@Rule
	public MockedJSFContainerRule rule = new MockedJSFContainerRule();

	@Before
	public void init() {
		context = rule.getContainer().getFacesContext();
		servletContext = rule.getContainer().getServletContext();
		comp = new MarvinJSComponent();
	}

	@Test
	public void test_rendererType() {
		assertEquals(MarvinJSComponent.DEFAULT_RENDERER, comp.getRendererType());
	}

	@Test
	public void test_enqueuedResources_withBaseUrl_withoutWebservices_notReadonly() {
		servletContext.addInitParameter(WEBXML_MARVINJS_BASE_URL, "baseUrl");

		comp = new MarvinJSComponent();
		comp.setReadonly(false);
		ResourceLoader loader = comp.getResourceLoader();

		assertThat(loader.getScriptResourcesToLoadInHead(), hasSize(1));
		assertThat(loader.getScriptResourcesToLoadInBodyAtTop(), hasSize(0));
		assertThat(loader.getScriptsExtToLoadInHead(), hasSize(2));
		assertThat(loader.getScriptsExtToLoadInBodyAtTop(), hasSize(0));
		assertThat(loader.getCssResourcesToLoad(), hasSize(0));
		assertThat(loader.getCssExtToLoad(), hasSize(0));
		assertTrue(loader.getScriptResourcesToLoadInHead().contains("js/MolecularFaces.min.js"));
		assertTrue(loader.getScriptsExtToLoadInHead().contains("baseUrl/gui/lib/promise-1.0.0.min.js"));
		assertTrue(loader.getScriptsExtToLoadInHead().contains("baseUrl/js/marvinjslauncher.js"));

		// Also test adding resources via the JSF event mechanism.
		rule.getContainer().getApplication().publishEvent(context, PostAddToViewEvent.class, comp);
		// The JSF mock implementation does not call processEvent() of the component.
		comp.processEvent(new PostAddToViewEvent(comp));

		List<UIComponent> componentsInHead = getResourceComponentsFromHead();
		List<UIComponent> componentsInBody = getComponentsInBody();
		assertThat(componentsInHead, hasSize(1));
		assertThat(matchingResourceComponentsInList(componentsInHead, JAVASCRIPT, "js/MolecularFaces.min.js"), hasSize(1));
		assertThat(componentsInBody, hasSize(0));
		assertThat(loader.getScriptsExtToLoadInHead(), hasSize(2));
		assertTrue(loader.getScriptsExtToLoadInHead().contains("baseUrl/gui/lib/promise-1.0.0.min.js"));
		assertTrue(loader.getScriptsExtToLoadInHead().contains("baseUrl/js/marvinjslauncher.js"));
	}

	@Test
	public void test_enqueuedResources_withBaseUrl_withoutWebservices_readonly() {
		servletContext.addInitParameter(WEBXML_MARVINJS_BASE_URL, "baseUrl");

		comp = new MarvinJSComponent();
		comp.setReadonly(true);
		ResourceLoader loader = comp.getResourceLoader();

		assertThat(loader.getScriptResourcesToLoadInHead(), hasSize(1));
		assertThat(loader.getScriptResourcesToLoadInBodyAtTop(), hasSize(0));
		assertThat(loader.getScriptsExtToLoadInHead(), hasSize(2));
		assertThat(loader.getScriptsExtToLoadInBodyAtTop(), hasSize(0));
		assertThat(loader.getCssResourcesToLoad(), hasSize(0));
		assertThat(loader.getCssExtToLoad(), hasSize(0));
		assertTrue(loader.getScriptResourcesToLoadInHead().contains("js/MolecularFaces.min.js"));
		assertTrue(loader.getScriptsExtToLoadInHead().contains("baseUrl/gui/lib/promise-1.0.0.min.js"));
		assertTrue(loader.getScriptsExtToLoadInHead().contains("baseUrl/js/marvinjslauncher.js"));

		// Also test adding resources via the JSF event mechanism.
		rule.getContainer().getApplication().publishEvent(context, PostAddToViewEvent.class, comp);
		// The JSF mock implementation does not call processEvent() of the component.
		comp.processEvent(new PostAddToViewEvent(comp));

		List<UIComponent> componentsInHead = getResourceComponentsFromHead();
		List<UIComponent> componentsInBody = getComponentsInBody();
		assertThat(componentsInHead, hasSize(1));
		assertThat(matchingResourceComponentsInList(componentsInHead, JAVASCRIPT, "js/MolecularFaces.min.js"), hasSize(1));
		assertThat(componentsInBody, hasSize(0));
		assertThat(loader.getScriptsExtToLoadInHead(), hasSize(2));
		assertTrue(loader.getScriptsExtToLoadInHead().contains("baseUrl/gui/lib/promise-1.0.0.min.js"));
		assertTrue(loader.getScriptsExtToLoadInHead().contains("baseUrl/js/marvinjslauncher.js"));
	}

	@Test
	public void test_enqueuedResources_withBaseUrl_withWebservices_readonly() {
		servletContext.addInitParameter(WEBXML_MARVINJS_BASE_URL, "baseUrl");
		servletContext.addInitParameter(WEBXML_MARVINJS_WEBSERVICES, "true");

		comp = new MarvinJSComponent();
		comp.setReadonly(true);
		ResourceLoader loader = comp.getResourceLoader();

		assertThat(loader.getScriptResourcesToLoadInHead(), hasSize(1));
		assertThat(loader.getScriptResourcesToLoadInBodyAtTop(), hasSize(0));
		assertThat(loader.getScriptsExtToLoadInHead(), hasSize(2));
		assertThat(loader.getScriptsExtToLoadInBodyAtTop(), hasSize(0));
		assertThat(loader.getCssResourcesToLoad(), hasSize(0));
		assertThat(loader.getCssExtToLoad(), hasSize(0));
		assertTrue(loader.getScriptResourcesToLoadInHead().contains("js/MolecularFaces.min.js"));
		assertTrue(loader.getScriptsExtToLoadInHead().contains("baseUrl/gui/lib/promise-1.0.0.min.js"));
		assertTrue(loader.getScriptsExtToLoadInHead().contains("baseUrl/js/marvinjslauncher.js"));

		// Also test adding resources via the JSF event mechanism.
		rule.getContainer().getApplication().publishEvent(context, PostAddToViewEvent.class, comp);
		// The JSF mock implementation does not call processEvent() of the component.
		comp.processEvent(new PostAddToViewEvent(comp));

		List<UIComponent> componentsInHead = getResourceComponentsFromHead();
		List<UIComponent> componentsInBody = getComponentsInBody();
		assertThat(componentsInHead, hasSize(1));
		assertThat(matchingResourceComponentsInList(componentsInHead, JAVASCRIPT, "js/MolecularFaces.min.js"), hasSize(1));
		assertThat(componentsInBody, hasSize(0));
		assertThat(loader.getScriptsExtToLoadInHead(), hasSize(3));
		assertTrue(loader.getScriptsExtToLoadInHead().contains("baseUrl/gui/lib/promise-1.0.0.min.js"));
		assertTrue(loader.getScriptsExtToLoadInHead().contains("baseUrl/js/marvinjslauncher.js"));
		assertTrue(loader.getScriptsExtToLoadInHead().contains("baseUrl/js/webservices.js"));
	}
}