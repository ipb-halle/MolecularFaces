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

import org.apache.myfaces.test.mock.MockedJsfTestContainer;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class MockedJSFContainerRule implements TestRule {
	MockedJsfTestContainer container;

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
		container.getApplication().addComponent("javax.faces.ComponentResourceContainer",
				"org.apache.myfaces.component.ComponentResourceContainer");
	}

	private void after() {
		container.tearDownAll();
	}
}