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

import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.convert.BooleanConverter;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;

import org.apache.myfaces.test.mock.MockHttpServletRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.ipb_halle.molecularfaces.test.MockedJSFContainerRule;

/**
 * 
 * @author flange
 */
public class RendererUtilsTest {
	private FacesContext context;
	private MockHttpServletRequest servletRequest;
	private Converter exceptionConverter = new Converter() {
		@Override
		public String getAsString(FacesContext context, UIComponent component, Object value) {
			throw new ConverterException();
		}

		@Override
		public Object getAsObject(FacesContext context, UIComponent component, String value) {
			throw new ConverterException();
		}
	};

	@Rule
	public MockedJSFContainerRule rule = new MockedJSFContainerRule();

	@Before
	public void init() {
		context = rule.getContainer().getFacesContext();
		servletRequest = rule.getContainer().getRequest();
	}

	/*
	 * Tests for RendererUtils.decodeComponent(FacesContext, UIInput)
	 */
	@Test
	public void test_decodeComponent_decodesSubmittelValueFromHttpRequest() {
		UIInput component = new UIInput();
		component.setId("IdOfThisComponent");
		servletRequest.addParameter("NotTheIdOfThisComponent", "some value");

		assertNull(component.getSubmittedValue());

		RendererUtils.decodeComponent(context, component);
		assertNull(component.getSubmittedValue());

		servletRequest.addParameter("IdOfThisComponent", "some value");
		RendererUtils.decodeComponent(context, component);
		assertEquals("some value", component.getSubmittedValue());
	}

	@Test
	public void test_decodeComponent_throwsNPE() {
		UIInput component = new UIInput();

		assertThrows(NullPointerException.class, () -> RendererUtils.decodeComponent(null, null));
		assertThrows(NullPointerException.class, () -> RendererUtils.decodeComponent(context, null));
		assertThrows(NullPointerException.class, () -> RendererUtils.decodeComponent(null, component));
	}

	/*
	 * Tests for RendererUtils.convertSubmittedValueToObject(FacesContext,
	 * UIComponent, Object)
	 */
	@Test
	public void test_convertSubmittedValueToObject_withConverter() {
		UIInput component = new UIInput();
		component.setConverter(new BooleanConverter());

		assertEquals(Boolean.TRUE, RendererUtils.convertSubmittedValueToObject(context, component, "true"));
	}

	@Test
	public void test_convertSubmittedValueToObject_submittedValueIsNullOrNoString_returnsSubmittedValue() {
		UIInput component = new UIInput();
		component.setConverter(new BooleanConverter());

		assertEquals(null, RendererUtils.convertSubmittedValueToObject(context, component, null));
		Integer val = 42;
		assertEquals(val, RendererUtils.convertSubmittedValueToObject(context, component, val));
	}

	@Test
	public void test_convertSubmittedValueToObject_componentIsNoUIInput_returnsSubmittedValue() {
		UIOutput component = new UIOutput();
		component.setConverter(new BooleanConverter());
		String submittedValue = "true";

		assertEquals(submittedValue, RendererUtils.convertSubmittedValueToObject(context, component, submittedValue));
	}

	@Test
	public void test_convertSubmittedValueToObject_withoutConverter_returnsSubmittedValue() {
		UIInput component = new UIInput();
		component.setConverter(null);
		String submittedValue = "the value";

		assertEquals(submittedValue, RendererUtils.convertSubmittedValueToObject(context, component, submittedValue));
	}

	@Test
	public void test_convertSubmittedValueToObject_throwsConverterException() {
		UIInput component = new UIInput();
		component.setConverter(exceptionConverter);
		String submittedValue = "the value";

		assertThrows(ConverterException.class,
				() -> RendererUtils.convertSubmittedValueToObject(context, component, submittedValue));
	}

	@Test
	public void test_convertSubmittedValueToObject_throwsNPE() {
		UIComponent component = new UIInput();
		Object submittedValue = null;

		assertThrows(NullPointerException.class,
				() -> RendererUtils.convertSubmittedValueToObject(null, null, submittedValue));
		assertThrows(NullPointerException.class,
				() -> RendererUtils.convertSubmittedValueToObject(context, null, submittedValue));
		assertThrows(NullPointerException.class,
				() -> RendererUtils.convertSubmittedValueToObject(null, component, submittedValue));
	}

	/*
	 * Tests for RendererUtils.convertValueToString(FacesContext, UIInput, Object)
	 */
	@Test
	public void test_convertValueToString_withConverter() {
		UIInput component = new UIInput();
		component.setConverter(new BooleanConverter());

		assertEquals("true", RendererUtils.convertValueToString(context, component, Boolean.TRUE));
	}

	@Test
	public void test_convertValueToString_withoutConverter_returnsValue() {
		UIInput component = new UIInput();
		component.setConverter(null);
		String value = "the value";

		assertEquals(value, RendererUtils.convertValueToString(context, component, value));
	}

	@Test
	public void test_convertValueToString_withoutConverterAndNoStringAsValue_throwsClassCastException() {
		UIInput component = new UIInput();
		component.setConverter(null);
		Object value = new Object();

		assertThrows(ClassCastException.class, () -> RendererUtils.convertValueToString(context, component, value));
	}

	@Test
	public void test_convertValueToString_throwsConverterException() {
		UIInput component = new UIInput();
		component.setConverter(exceptionConverter);
		String value = "the value";

		assertThrows(ConverterException.class, () -> RendererUtils.convertValueToString(context, component, value));
	}

	@Test
	public void test_convertValueToString_throwsNPE() {
		UIInput component = new UIInput();
		Object value = null;

		assertThrows(NullPointerException.class, () -> RendererUtils.convertValueToString(null, null, value));
		assertThrows(NullPointerException.class, () -> RendererUtils.convertValueToString(context, null, value));
		assertThrows(NullPointerException.class, () -> RendererUtils.convertValueToString(null, component, value));
	}
}