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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;

import org.junit.Rule;
import org.junit.Test;

import de.ipb_halle.molecularfaces.test.MockedJSFContainerRule;

public class RendererUtilsTest {
	@Rule
	public MockedJSFContainerRule rule = new MockedJSFContainerRule();

	@Test
	public void test_decodeComponent_decodesSubmittelValueFromHttpRequest() {
		UIInput component = new UIInput();
		component.setId("IdOfThisComponent");
		FacesContext context = rule.getContainer().getFacesContext();
		rule.getContainer().getRequest().addParameter("IdOfThisComponent", "some value");
		assertNull(component.getSubmittedValue());

		RendererUtils.decodeComponent(context, component);
		assertEquals("some value", component.getSubmittedValue());
	}

	@Test
	public void test_decodeComponent_throwsNPE() {
		UIInput component = new UIInput();
		FacesContext context = rule.getContainer().getFacesContext();

		assertThrows(NullPointerException.class, () -> RendererUtils.decodeComponent(null, null));
		assertThrows(NullPointerException.class, () -> RendererUtils.decodeComponent(context, null));
		assertThrows(NullPointerException.class, () -> RendererUtils.decodeComponent(null, component));
	}
}