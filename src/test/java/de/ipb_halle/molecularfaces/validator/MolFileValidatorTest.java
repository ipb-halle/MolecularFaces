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

import de.ipb_halle.molecularfaces.validator.MolFile.Mode;

public class MolFileValidatorTest {
	private static Validator validator;

	@BeforeClass
	public static void setUpValidator() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	private class RelaxedEntity {
		@MolFile(mode = Mode.RELAXED)
		private String molecule = "";

		public void setMolecule(String molecule) {
			this.molecule = molecule;
		}
	}

	private class StrictEntity {
		@MolFile(mode = Mode.STRICT)
		private String molecule = "";

		public void setMolecule(String molecule) {
			this.molecule = molecule;
		}
	}

	private Set<ConstraintViolation<RelaxedEntity>> relaxedConstraintViolations;
	private Set<ConstraintViolation<StrictEntity>> strictConstraintViolations;

	private RelaxedEntity relaxedEntity = new RelaxedEntity();
	private StrictEntity strictEntity = new StrictEntity();

	@Test
	public void testIsValid() {
		String validMolfile = "\n" + "Actelion Java MolfileCreator 1.0\n" + "\n"
				+ "  1  0  0  0  0  0  0  0  0  0999 V2000\n"
				+ "   10.3125  -11.8125   -0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + "M  END\n";

		relaxedEntity.setMolecule(validMolfile);
		strictEntity.setMolecule(validMolfile);

		// valid for relaxed and strict
		relaxedConstraintViolations = validator.validate(relaxedEntity);
		assertEquals(0, relaxedConstraintViolations.size());
		strictConstraintViolations = validator.validate(strictEntity);
		assertEquals(0, strictConstraintViolations.size());

		// one space too less after the element symbol
		String maybeValidMolfile = "\n" + "Actelion Java MolfileCreator 1.0\n" + "\n"
				+ "  1  0  0  0  0  0  0  0  0  0999 V2000\n"
				+ "   10.3125  -11.8125   -0.0000 C  0  0  0  0  0  0  0  0  0  0  0  0\n" + "M  END\n";

		relaxedEntity.setMolecule(maybeValidMolfile);
		strictEntity.setMolecule(maybeValidMolfile);

		// invalid for both relaxed and strict
		relaxedConstraintViolations = validator.validate(relaxedEntity);
		assertEquals(1, relaxedConstraintViolations.size());
		strictConstraintViolations = validator.validate(strictEntity);
		assertEquals(1, strictConstraintViolations.size());
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