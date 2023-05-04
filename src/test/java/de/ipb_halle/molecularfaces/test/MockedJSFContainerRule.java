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
package de.ipb_halle.molecularfaces.test;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.html.HtmlBody;

import org.apache.myfaces.test.mock.MockedJsfTestContainer;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * JUnit 4 rule that brings up a mocked JSF environment before the test run and
 * tears it down after the test.
 * 
 * @author flange
 */
public class MockedJSFContainerRule implements TestRule {
	private MockedJsfTestContainer container;

	public MockedJsfTestContainer getContainer() {
		return container;
	}

	@Override
	public Statement apply(Statement base, Description description) {
		return new Statement() {
			@Override
			public void evaluate() throws Throwable {
				before();
				try {
					base.evaluate();
				} finally {
					after();
				}
			}
		};
	}

	private void before() {
		container = new MockedJsfTestContainer();
		container.setUpAll();

		// This allows the use of UIViewRoot.addComponentResource(...).
		container.getApplication().addComponent("jakarta.faces.ComponentResourceContainer",
				"org.apache.myfaces.component.ComponentResourceContainer");

                // add head facet, otherwise MockedJsfTestContainer will throw a NullPointerException
                // when head facet is added automatically in UIViewRoot without Renderer
                UIComponent component = container.getApplication()
                        .createComponent("jakarta.faces.ComponentResourceContainer");
                component.setId("jakarta_faces_location_head");
                container.getFacesContext().getViewRoot().getFacets().put("head", component);

		// Body component is needed when adding resources there. 
		container.getFacesContext().getViewRoot().getChildren().add(new HtmlBody());

		// Register renderers from myfaces-impl for JavaScript and stylesheets.
//		container.getFacesContext().getRenderKit().addRenderer(UIOutput.COMPONENT_FAMILY,
//				"jakarta.faces.resource.Script", new HtmlScriptRenderer());
//		container.getFacesContext().getRenderKit().addRenderer(UIOutput.COMPONENT_FAMILY,
//				"jakarta.faces.resource.Stylesheet", new HtmlStylesheetRenderer());
	}

	private void after() {
		container.tearDownAll();
	}
}
