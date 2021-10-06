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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.io.StringWriter;

import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.convert.BooleanConverter;

import org.apache.myfaces.shared.renderkit.html.HtmlResponseWriterImpl;
import org.apache.myfaces.test.mock.MockHttpServletRequest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.ipb_halle.molecularfaces.test.MockedJSFContainerRule;

/**
 * 
 * @author flange
 */
public class OpenVectorEditorRendererTest {
	private FacesContext context;
	private MockHttpServletRequest servletRequest;
	private StringWriter writer;
	private OpenVectorEditorComponent comp;
	private OpenVectorEditorRenderer renderer = new OpenVectorEditorRenderer();

	@Rule
	public MockedJSFContainerRule rule = new MockedJSFContainerRule();

	@Before
	public void init() {
		context = rule.getContainer().getFacesContext();
		servletRequest = rule.getContainer().getRequest();
		writer = new StringWriter();

		/*
		 * We use the implementation of MyFaces here, because MockResponseWriter escapes
		 * JavaScript code.
		 */
		context.setResponseWriter(new HtmlResponseWriterImpl(writer, "text/html", "UTF-8", false));

		comp = new OpenVectorEditorComponent();
	}

	/*
	 * Tests for decode(FacesContext, UIComponent)
	 */
	@Test
	public void test_decode_withWrongComponentClass_throwsClassCastException() {
		assertThrows(ClassCastException.class, () -> renderer.decode(context, new UIOutput()));
	}

	@Test
	public void test_decode_withReadonlyComponent() {
		comp.setId("myId");
		comp.setReadonly(true);
		assertNull(comp.getSubmittedValue());
		servletRequest.addParameter("myId", "value");

		renderer.decode(context, comp);

		assertNull(comp.getSubmittedValue());
	}

	@Test
	public void test_decode_withWritableComponent() {
		comp.setId("myId");
		comp.setReadonly(false);
		assertNull(comp.getSubmittedValue());

		servletRequest.addParameter("wrongId", "value");
		renderer.decode(context, comp);
		assertNull(comp.getSubmittedValue());

		servletRequest.addParameter("myId", "123");
		renderer.decode(context, comp);
		assertEquals("123", comp.getSubmittedValue());
	}

	/*
	 * Tests for getConvertedValue(FacesContext, UIComponent, Object)
	 */
	@Test
	public void test_getConvertedValue_withConverter() {
		comp.setConverter(new BooleanConverter());

		assertEquals(Boolean.TRUE, renderer.getConvertedValue(context, comp, "true"));
	}

	@Test
	public void test_getConvertedValue_withoutConverter_returnsValue() {
		comp.setConverter(null);
		String value = "the value";

		assertEquals(value, renderer.getConvertedValue(context, comp, value));
	}

	/*
	 * Tests for encodeBegin(FacesContext, UIComponent)
	 */
	@Test
	public void test_encodeBegin_withWrongComponentClass_throwsClassCastException() throws IOException {
		assertThrows(ClassCastException.class, () -> renderer.encodeBegin(context, new UIOutput()));
	}

	@Test
	public void test_encodeBegin_componentNotRendered() throws IOException {
		comp.setRendered(false);
		renderer.encodeBegin(context, comp);
		assertEquals("", writer.toString());
	}
}