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
package de.ipb_halle.molecularfaces.validator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.io.StringReader;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.io.MDLV2000Reader;

import de.ipb_halle.molecularfaces.validator.Molfile.Mode;

public class MolFileValidatorTest {
	private static Validator validator;

	@BeforeClass
	public static void setUpValidator() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	private class RelaxedEntity {
		@Molfile(mode = Mode.RELAXED)
		private final String molecule;

		public RelaxedEntity(String molecule) {
			this.molecule = molecule;
		}
	}

	private class StrictEntity {
		@Molfile(mode = Mode.STRICT)
		private final String molecule;

		public StrictEntity(String molecule) {
			this.molecule = molecule;
		}
	}

	@Test
	public void testValidMolfile() {
		String validMolfile = "\n" + "Actelion Java MolfileCreator 1.0\n" + "\n"
				+ "  1  0  0  0  0  0  0  0  0  0999 V2000\n"
				+ "   10.3125  -11.8125   -0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + "M  END\n";

		RelaxedEntity relaxedEntity = new RelaxedEntity(validMolfile);
		StrictEntity strictEntity = new StrictEntity(validMolfile);
		Set<ConstraintViolation<RelaxedEntity>> relaxedConstraintViolations = validator.validate(relaxedEntity);
		Set<ConstraintViolation<StrictEntity>> strictConstraintViolations = validator.validate(strictEntity);

		// valid for relaxed and strict
		assertEquals(0, relaxedConstraintViolations.size());
		assertEquals(0, strictConstraintViolations.size());
	}

	@Test
	public void testInvalidMolfile() {
		// one space too less after the element symbol
		String invalidMolfile = "\n" + "Actelion Java MolfileCreator 1.0\n" + "\n"
				+ "  1  0  0  0  0  0  0  0  0  0999 V2000\n"
				+ "   10.3125  -11.8125   -0.0000 C  0  0  0  0  0  0  0  0  0  0  0  0\n" + "M  END\n";

		RelaxedEntity relaxedEntity = new RelaxedEntity(invalidMolfile);
		StrictEntity strictEntity = new StrictEntity(invalidMolfile);
		Set<ConstraintViolation<RelaxedEntity>> relaxedConstraintViolations = validator.validate(relaxedEntity);
		Set<ConstraintViolation<StrictEntity>> strictConstraintViolations = validator.validate(strictEntity);

		// invalid for both relaxed and strict
		assertEquals(1, relaxedConstraintViolations.size());
		assertEquals(1, strictConstraintViolations.size());
		assertEquals("invalid MolFile", relaxedConstraintViolations.iterator().next().getMessage());
		assertEquals("invalid MolFile", strictConstraintViolations.iterator().next().getMessage());
	}

	@Test
	public void testMolfileWithJavaScript() {
		String evilMolfile = "\n" + "</script><script>alert('Hello world');</script><script>\n" + "\n"
				+ "  1  0  0  0  0  0  0  0  0  0999 V2000\n"
				+ "   10.3125  -11.8125   -0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + "M  END\n";

		RelaxedEntity relaxedEntity = new RelaxedEntity(evilMolfile);
		StrictEntity strictEntity = new StrictEntity(evilMolfile);
		Set<ConstraintViolation<RelaxedEntity>> relaxedConstraintViolations = validator.validate(relaxedEntity);
		Set<ConstraintViolation<StrictEntity>> strictConstraintViolations = validator.validate(strictEntity);

		// valid for both relaxed and strict
		assertEquals(0, relaxedConstraintViolations.size());
		assertEquals(0, strictConstraintViolations.size());
	}

	/*
	 * Dear CDK developers: Please fix your parser and check for EOF!
	 */
	@Test
	public void testCDKReaderThrowsNPE() throws IOException, CDKException {
		try (MDLV2000Reader reader = new MDLV2000Reader(new StringReader("abc"))) {
			assertThrows(NullPointerException.class, () -> reader.read(new AtomContainer()));
		}
	}
}