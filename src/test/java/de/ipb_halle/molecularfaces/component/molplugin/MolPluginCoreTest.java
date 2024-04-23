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

import static de.ipb_halle.molecularfaces.test.TestUtils.getComponentsInBody;
import static de.ipb_halle.molecularfaces.test.TestUtils.getResourceComponentsFromHead;
import static de.ipb_halle.molecularfaces.test.TestUtils.matchingResourceComponentsInList;
import static de.ipb_halle.molecularfaces.util.ResourceLoader.JAVASCRIPT;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PostAddToViewEvent;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.ipb_halle.molecularfaces.test.MockedJSFContainerRule;
import de.ipb_halle.molecularfaces.util.ResourceLoader;

/**
 * 
 * @author flange
 */
public class MolPluginCoreTest {
	private FacesContext context;
	private MolPluginCore comp;

	@Rule
	public MockedJSFContainerRule rule = new MockedJSFContainerRule();

	@Before
	public void init() {
		context = rule.getContainer().getFacesContext();
		comp = new MolPluginCore() {};
	}

	@Test
	public void test_componentFamily() {
		assertEquals(MolPluginCore.COMPONENT_FAMILY, comp.getFamily());
	}

	@Test
	public void test_gettersAndSettersAndDefaults() {
		assertFalse(comp.isBorder());
		comp.setBorder(true);
		assertTrue(comp.isBorder());

		assertEquals(MolPluginCore.DEFAULT_FORMAT, comp.getFormat());
		comp.setFormat("some format");
		assertEquals("some format", comp.getFormat());

		assertEquals(MolPluginCore.DEFAULT_HEIGHT, comp.getHeight());
		comp.setHeight(42);
		assertEquals(42, comp.getHeight());

		assertFalse(comp.isReadonly());
		comp.setReadonly(true);
		assertTrue(comp.isReadonly());

		assertNull(comp.getWidgetVar());
		comp.setWidgetVar("myWidgetVar");
		assertEquals("myWidgetVar", comp.getWidgetVar());

		assertEquals(MolPluginCore.DEFAULT_WIDTH, comp.getWidth());
		comp.setWidth(42);
		assertEquals(42, comp.getWidth());
	}

	@Test
	public void test_enqueuedResources() {
		ResourceLoader loader = comp.getResourceLoader();

		assertThat(loader.getScriptResourcesToLoadInHead(), hasSize(1));
		assertThat(loader.getScriptResourcesToLoadInBodyAtTop(), hasSize(0));
		assertThat(loader.getScriptsExtToLoadInHead(), hasSize(0));
		assertThat(loader.getScriptsExtToLoadInBodyAtTop(), hasSize(0));
		assertThat(loader.getCssResourcesToLoad(), hasSize(0));
		assertThat(loader.getCssExtToLoad(), hasSize(0));
		assertTrue(loader.getScriptResourcesToLoadInHead().contains("js/MolecularFaces.min.js"));

		rule.getContainer().getApplication().publishEvent(context, PostAddToViewEvent.class, comp);

		List<UIComponent> componentsInHead = getResourceComponentsFromHead();
		List<UIComponent> componentsInBody = getComponentsInBody();
		assertThat(componentsInHead, hasSize(1));
		assertThat(matchingResourceComponentsInList(componentsInHead, JAVASCRIPT, "js/MolecularFaces.min.js"), hasSize(1));
		assertThat(componentsInBody, hasSize(0));
	}
}
