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

import static de.ipb_halle.molecularfaces.component.molplugin.OpenChemLibJSComponent.WEBXML_CUSTOM_RESOURCE_URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.io.StringWriter;

import jakarta.faces.component.UIOutput;
import jakarta.faces.context.FacesContext;

import org.apache.myfaces.renderkit.html.HtmlResponseWriterImpl;
import org.apache.myfaces.test.mock.MockServletContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.ipb_halle.molecularfaces.test.MockedJSFContainerRule;
import de.ipb_halle.molecularfaces.test.TestUtils;

/**
 * 
 * @author flange
 */
public class OpenChemLibJSRendererTest {
	private FacesContext context;
	private MockServletContext servletContext;
	private StringWriter writer;
	private OpenChemLibJSComponent comp;
	private OpenChemLibJSRenderer renderer = new OpenChemLibJSRenderer();

	@Rule
	public MockedJSFContainerRule rule = new MockedJSFContainerRule();

	@Before
	public void init() {
		context = rule.getContainer().getFacesContext();
		servletContext = rule.getContainer().getServletContext();
		writer = new StringWriter();

		/*
		 * We use the implementation of MyFaces here, because MockResponseWriter
		 * HTML-escapes JavaScript code in <script> tags.
		 */
		context.setResponseWriter(new HtmlResponseWriterImpl(writer, "text/html", "UTF-8", false));

		comp = new OpenChemLibJSComponent();
	}

	@Test
	public void test_encodeBegin_withWrongComponentClass_throwsClassCastException() throws IOException {
		assertThrows(ClassCastException.class, () -> renderer.encodeBegin(context, new UIOutput()));
	}

	@Test
	public void test_encode_componentNotRendered() throws IOException {
		comp.setRendered(false);
		TestUtils.encodeRenderer(renderer, context, comp);
		assertEquals("", writer.toString());
	}

	/*
	 * Viewer
	 */
	@Test
	public void test_encode_viewer_withoutWidgetVar() throws IOException {
		comp.setId("myId");
		comp.setValue("molfile");
		comp.setReadonly(true);

		TestUtils.encodeRenderer(renderer, context, comp);
		String expected = TestUtils.readResourceFileIgnoreNewlinesAndTabs(OpenChemLibJSRendererTest.class,
				"OpenChemLibJSRendererTest_encode_viewer_withoutWidgetVar.txt");
		assertEquals(expected, writer.toString());
	}

	@Test
	public void test_encode_viewer_withEmptyWidgetVar() throws IOException {
		comp.setId("myId");
		comp.setValue("molfile");
		comp.setWidgetVar("");
		comp.setReadonly(true);

		TestUtils.encodeRenderer(renderer, context, comp);
		String expected = TestUtils.readResourceFileIgnoreNewlinesAndTabs(OpenChemLibJSRendererTest.class,
				"OpenChemLibJSRendererTest_encode_viewer_withoutWidgetVar.txt");
		assertEquals(expected, writer.toString());
	}

	@Test
	public void test_encode_viewer_withMDLV3000Format() throws IOException {
		comp.setId("myId");
		comp.setValue("molfile");
		comp.setFormat(MolPluginCore.Format.MDLV3000.toString());
		comp.setReadonly(true);

		TestUtils.encodeRenderer(renderer, context, comp);
		String expected = TestUtils.readResourceFileIgnoreNewlinesAndTabs(OpenChemLibJSRendererTest.class,
				"OpenChemLibJSRendererTest_encode_viewer_withoutWidgetVar.txt");
		assertEquals(expected, writer.toString());
	}

	@Test
	public void test_encode_viewer_withWidgetVar() throws IOException {
		comp.setId("myId");
		comp.setValue("molfile");
		comp.setReadonly(true);
		comp.setWidgetVar("viewer1");

		TestUtils.encodeRenderer(renderer, context, comp);
		String expected = TestUtils.readResourceFileIgnoreNewlinesAndTabs(OpenChemLibJSRendererTest.class,
				"OpenChemLibJSRendererTest_encode_viewer_withWidgetVar.txt");
		assertEquals(expected, writer.toString());
	}

	@Test
	public void test_encode_viewer_withCustomResourceUrl_withWidgetVar() throws IOException {
		servletContext.addInitParameter(WEBXML_CUSTOM_RESOURCE_URL, "/plugins/openchemlib-full.js");
		comp = new OpenChemLibJSComponent();
		comp.setId("myId");
		comp.setValue("molfile");
		comp.setReadonly(true);
		comp.setWidgetVar("viewer1");

		TestUtils.encodeRenderer(renderer, context, comp);
		String expected = TestUtils.readResourceFileIgnoreNewlinesAndTabs(OpenChemLibJSRendererTest.class,
				"OpenChemLibJSRendererTest_encode_viewer_withCustomResourceUrl_withWidgetVar.txt");
		assertEquals(expected, writer.toString());
	}

	@Test
	public void test_encode_viewer_withPassthroughAttribute() throws IOException {
		comp.setId("myId");
		comp.setValue("molfile");
		comp.setReadonly(true);
		comp.getPassThroughAttributes().put("myattribute", "the value");

		TestUtils.encodeRenderer(renderer, context, comp);
		String expected = TestUtils.readResourceFileIgnoreNewlinesAndTabs(OpenChemLibJSRendererTest.class,
				"OpenChemLibJSRendererTest_encode_viewer_withPassthroughAttribute.txt");
		assertEquals(expected, writer.toString());
	}

	/*
	 * Editor
	 */
	@Test
	public void test_encode_editor_withoutWidgetVar() throws IOException {
		comp.setId("myId");
		comp.setValue("molfile");

		TestUtils.encodeRenderer(renderer, context, comp);
		String expected = TestUtils.readResourceFileIgnoreNewlinesAndTabs(OpenChemLibJSRendererTest.class,
				"OpenChemLibJSRendererTest_encode_editor_withoutWidgetVar.txt");
		assertEquals(expected, writer.toString());
	}

	@Test
	public void test_encode_editor_withEmptyWidgetVar() throws IOException {
		comp.setId("myId");
		comp.setValue("molfile");
		comp.setWidgetVar("");

		TestUtils.encodeRenderer(renderer, context, comp);
		String expected = TestUtils.readResourceFileIgnoreNewlinesAndTabs(OpenChemLibJSRendererTest.class,
				"OpenChemLibJSRendererTest_encode_editor_withoutWidgetVar.txt");
		assertEquals(expected, writer.toString());
	}

	@Test
	public void test_encode_editor_withMDLV3000Format() throws IOException {
		comp.setId("myId");
		comp.setValue("molfile");
		comp.setFormat(MolPluginCore.Format.MDLV3000.toString());

		TestUtils.encodeRenderer(renderer, context, comp);
		String expected = TestUtils.readResourceFileIgnoreNewlinesAndTabs(OpenChemLibJSRendererTest.class,
				"OpenChemLibJSRendererTest_encode_editor_withMDLV3000Format.txt");
		assertEquals(expected, writer.toString());
	}

	@Test
	public void test_encode_editor_withWidgetVar() throws IOException {
		comp.setId("myId");
		comp.setValue("molfile");
		comp.setWidgetVar("editor1");

		TestUtils.encodeRenderer(renderer, context, comp);
		String expected = TestUtils.readResourceFileIgnoreNewlinesAndTabs(OpenChemLibJSRendererTest.class,
				"OpenChemLibJSRendererTest_encode_editor_withWidgetVar.txt");
		assertEquals(expected, writer.toString());
	}

	@Test
	public void test_encode_editor_withCustomResourceUrl_withWidgetVar() throws IOException {
		servletContext.addInitParameter(WEBXML_CUSTOM_RESOURCE_URL, "/plugins/openchemlib-full.js");
		comp = new OpenChemLibJSComponent();
		comp.setId("myId");
		comp.setValue("molfile");
		comp.setWidgetVar("editor1");

		TestUtils.encodeRenderer(renderer, context, comp);
		String expected = TestUtils.readResourceFileIgnoreNewlinesAndTabs(OpenChemLibJSRendererTest.class,
				"OpenChemLibJSRendererTest_encode_editor_withCustomResourceUrl_withWidgetVar.txt");
		assertEquals(expected, writer.toString());
	}

	@Test
	public void test_encode_editor_withPassthroughAttribute() throws IOException {
		comp.setId("myId");
		comp.setValue("molfile");
		comp.getPassThroughAttributes().put("myattribute", "the value");

		TestUtils.encodeRenderer(renderer, context, comp);
		String expected = TestUtils.readResourceFileIgnoreNewlinesAndTabs(OpenChemLibJSRendererTest.class,
				"OpenChemLibJSRendererTest_encode_editor_withPassthroughAttribute.txt");
		assertEquals(expected, writer.toString());
	}
}
