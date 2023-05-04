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

import static de.ipb_halle.molecularfaces.component.molplugin.MolPaintJSComponent.WEBXML_CUSTOM_RESOURCE_URL;
import static de.ipb_halle.molecularfaces.test.TestUtils.getComponentsInBody;
import static de.ipb_halle.molecularfaces.test.TestUtils.getResourceComponentsFromHead;
import static de.ipb_halle.molecularfaces.test.TestUtils.matchingResourceComponentsInList;
import static de.ipb_halle.molecularfaces.util.ResourceLoader.JAVASCRIPT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

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
public class MolPaintJSComponentTest {
	private FacesContext context;
	private MockServletContext servletContext;
	private MolPaintJSComponent comp;

	@Rule
	public MockedJSFContainerRule rule = new MockedJSFContainerRule();

	@Before
	public void init() {
		context = rule.getContainer().getFacesContext();
		servletContext = rule.getContainer().getServletContext();
		comp = new MolPaintJSComponent();
	}

	@Test
	public void test_rendererType() {
		assertEquals(MolPaintJSComponent.DEFAULT_RENDERER, comp.getRendererType());
	}

	@Test
	public void test_enqueuedResources_withContextParam() {
		servletContext.addInitParameter(WEBXML_CUSTOM_RESOURCE_URL, "baseUrl/molpaint.js");

		comp = new MolPaintJSComponent();
		ResourceLoader loader = comp.getResourceLoader();

		assertThat(loader.getScriptResourcesToLoadInHead(), hasSize(1));
		assertThat(loader.getScriptResourcesToLoadInBodyAtTop(), hasSize(0));
		assertThat(loader.getScriptsExtToLoadInHead(), hasSize(1));
		assertThat(loader.getScriptsExtToLoadInBodyAtTop(), hasSize(0));
		assertThat(loader.getCssResourcesToLoad(), hasSize(0));
		assertThat(loader.getCssExtToLoad(), hasSize(0));
		assertTrue(loader.getScriptResourcesToLoadInHead().contains("js/MolecularFaces.min.js"));
		assertTrue(loader.getScriptsExtToLoadInHead().contains("baseUrl/molpaint.js"));

		// Also test adding resources via the JSF event mechanism.
		rule.getContainer().getApplication().publishEvent(context, PostAddToViewEvent.class, comp);

		List<UIComponent> componentsInHead = getResourceComponentsFromHead();
		List<UIComponent> componentsInBody = getComponentsInBody();
		assertThat(componentsInHead, hasSize(1));
		assertThat(matchingResourceComponentsInList(componentsInHead, JAVASCRIPT, "js/MolecularFaces.min.js"), hasSize(1));
		assertThat(componentsInBody, hasSize(0));
	}

	@Test
	public void test_enqueuedResources_withoutContextParam() {
		assertNull(new WebXmlImpl().getContextParam(WEBXML_CUSTOM_RESOURCE_URL, context, null));
		checkResourceLoadingViaResourceLoaderAndViaJSF();
	}

	@Test
	public void test_enqueuedResources_withEmptyContextParam() {
		servletContext.addInitParameter(WEBXML_CUSTOM_RESOURCE_URL, "");
		assertEquals("", new WebXmlImpl().getContextParam(WEBXML_CUSTOM_RESOURCE_URL, context, null));
		checkResourceLoadingViaResourceLoaderAndViaJSF();
	}

	private void checkResourceLoadingViaResourceLoaderAndViaJSF() {
		comp = new MolPaintJSComponent();
		ResourceLoader loader = comp.getResourceLoader();

		assertThat(loader.getScriptResourcesToLoadInHead(), hasSize(2));
		assertThat(loader.getScriptResourcesToLoadInBodyAtTop(), hasSize(0));
		assertThat(loader.getScriptsExtToLoadInHead(), hasSize(0));
		assertThat(loader.getScriptsExtToLoadInBodyAtTop(), hasSize(0));
		assertThat(loader.getCssResourcesToLoad(), hasSize(0));
		assertThat(loader.getCssExtToLoad(), hasSize(0));
		assertTrue(loader.getScriptResourcesToLoadInHead().contains("js/MolecularFaces.min.js"));
		assertTrue(loader.getScriptResourcesToLoadInHead().contains("plugins/molpaintjs/molpaint.js"));

		// Also test adding resources via the JSF event mechanism.
		rule.getContainer().getApplication().publishEvent(context, PostAddToViewEvent.class, comp);

		List<UIComponent> componentsInHead = getResourceComponentsFromHead();
		List<UIComponent> componentsInBody = getComponentsInBody();
		assertThat(componentsInHead, hasSize(2));
		assertThat(matchingResourceComponentsInList(componentsInHead, JAVASCRIPT, "js/MolecularFaces.min.js"), hasSize(1));
		assertThat(matchingResourceComponentsInList(componentsInHead, JAVASCRIPT, "plugins/molpaintjs/molpaint.js"), hasSize(1));
		assertThat(componentsInBody, hasSize(0));
	}
}
