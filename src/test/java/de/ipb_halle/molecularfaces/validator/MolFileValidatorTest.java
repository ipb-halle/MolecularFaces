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
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import org.junit.BeforeClass;
import org.junit.Test;
import de.ipb_halle.molecularfaces.validator.Molfile.Format;
import de.ipb_halle.molecularfaces.validator.Molfile.Mode;

public class MolFileValidatorTest {
	private static Validator validator;

	@BeforeClass
	public static void setUpValidator() {
		validator = Validation.buildDefaultValidatorFactory().getValidator();
	}

	private String validV2000Molfile = "\n" + "Actelion Java MolfileCreator 1.0\n" + "\n"
			+ "  1  0  0  0  0  0  0  0  0  0999 V2000\n"
			+ "   10.3125  -11.8125   -0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + "M  END\n";

	// one space too less after the element symbol
	private String invalidV2000Molfile = "\n" + "Actelion Java MolfileCreator 1.0\n" + "\n"
			+ "  1  0  0  0  0  0  0  0  0  0999 V2000\n"
			+ "   10.3125  -11.8125   -0.0000 C  0  0  0  0  0  0  0  0  0  0  0  0\n" + "M  END\n";

	private String evilV2000Molfile = "\n" + "</script><script>alert('Hello world');</script><script>\n" + "\n"
			+ "  1  0  0  0  0  0  0  0  0  0999 V2000\n"
			+ "   10.3125  -11.8125   -0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + "M  END\n";

	private class RelaxedV2000Entity {
		@Molfile(mode = Mode.RELAXED, format = Format.V2000)
		private final String molecule;

		public RelaxedV2000Entity(String molecule) {
			this.molecule = molecule;
		}
	}

	private class StrictV2000Entity {
		@Molfile(mode = Mode.STRICT, format = Format.V2000)
		private final String molecule;

		public StrictV2000Entity(String molecule) {
			this.molecule = molecule;
		}
	}

	@Test
	public void testValidV2000Molfile() {
		RelaxedV2000Entity relaxedEntity = new RelaxedV2000Entity(validV2000Molfile);
		StrictV2000Entity strictEntity = new StrictV2000Entity(validV2000Molfile);
		Set<ConstraintViolation<RelaxedV2000Entity>> relaxedConstraintViolations = validator.validate(relaxedEntity);
		Set<ConstraintViolation<StrictV2000Entity>> strictConstraintViolations = validator.validate(strictEntity);

		// valid for relaxed and strict
		assertEquals(0, relaxedConstraintViolations.size());
		assertEquals(0, strictConstraintViolations.size());
	}

	@Test
	public void testInvalidV2000Molfile() {
		RelaxedV2000Entity relaxedEntity = new RelaxedV2000Entity(invalidV2000Molfile);
		StrictV2000Entity strictEntity = new StrictV2000Entity(invalidV2000Molfile);
		Set<ConstraintViolation<RelaxedV2000Entity>> relaxedConstraintViolations = validator.validate(relaxedEntity);
		Set<ConstraintViolation<StrictV2000Entity>> strictConstraintViolations = validator.validate(strictEntity);

		// invalid for both relaxed and strict
		assertEquals(1, relaxedConstraintViolations.size());
		assertEquals(1, strictConstraintViolations.size());
		assertEquals("invalid MDL Molfile", relaxedConstraintViolations.iterator().next().getMessage());
		assertEquals("invalid MDL Molfile", strictConstraintViolations.iterator().next().getMessage());
	}

	@Test
	public void testV2000MolfileWithJavaScript() {
		RelaxedV2000Entity relaxedEntity = new RelaxedV2000Entity(evilV2000Molfile);
		StrictV2000Entity strictEntity = new StrictV2000Entity(evilV2000Molfile);
		Set<ConstraintViolation<RelaxedV2000Entity>> relaxedConstraintViolations = validator.validate(relaxedEntity);
		Set<ConstraintViolation<StrictV2000Entity>> strictConstraintViolations = validator.validate(strictEntity);

		// valid for both relaxed and strict
		assertEquals(0, relaxedConstraintViolations.size());
		assertEquals(0, strictConstraintViolations.size());
	}

	private class RelaxedV3000Entity {
		@Molfile(mode = Mode.RELAXED, format = Format.V3000)
		private final String molecule;

		public RelaxedV3000Entity(String molecule) {
			this.molecule = molecule;
		}
	}

	private class StrictV3000Entity {
		@Molfile(mode = Mode.STRICT, format = Format.V3000)
		private final String molecule;

		public StrictV3000Entity(String molecule) {
			this.molecule = molecule;
		}
	}

	private String validV3000Molfile = "\n" + "Actelion Java MolfileCreator 2.0\n" + "\n"
			+ "  0  0  0  0  0  0              0 V3000\n" + "M  V30 BEGIN CTAB\n" + "M  V30 COUNTS 1 0 0 0 0\n"
			+ "M  V30 BEGIN ATOM\n" + "M  V30 1 C 0 0 0 0\n" + "M  V30 END ATOM\n" + "M  V30 BEGIN BOND\n"
			+ "M  V30 END BOND\n" + "M  V30 END CTAB\n" + "M  END";

	// missing "END ATOM"
	private String invalidV3000Molfile = "\n" + "Actelion Java MolfileCreator 2.0\n" + "\n"
			+ "  0  0  0  0  0  0              0 V3000\n" + "M  V30 BEGIN CTAB\n" + "M  V30 COUNTS 1 0 0 0 0\n"
			+ "M  V30 BEGIN ATOM\n" + "M  V30 1 C 0 0 0 0\n" // + "M V30 END ATOM\n"
			+ "M  V30 BEGIN BOND\n" + "M  V30 END BOND\n" + "M  V30 END CTAB\n" + "M  END";

	private String evilV3000Molfile = "\n" + "</script><script>alert('Hello world');</script><script>\n" + "\n"
			+ "  0  0  0  0  0  0              0 V3000\n" + "M  V30 BEGIN CTAB\n" + "M  V30 COUNTS 1 0 0 0 0\n"
			+ "M  V30 BEGIN ATOM\n" + "M  V30 1 C 0 0 0 0\n" + "M  V30 END ATOM\n" + "M  V30 BEGIN BOND\n"
			+ "M  V30 END BOND\n" + "M  V30 END CTAB\n" + "M  END";

	@Test
	public void testValidV3000Molfile() {
		RelaxedV3000Entity relaxedEntity = new RelaxedV3000Entity(validV3000Molfile);
		StrictV3000Entity strictEntity = new StrictV3000Entity(validV3000Molfile);
		Set<ConstraintViolation<RelaxedV3000Entity>> relaxedConstraintViolations = validator.validate(relaxedEntity);
		Set<ConstraintViolation<StrictV3000Entity>> strictConstraintViolations = validator.validate(strictEntity);

		// valid for relaxed and strict
		assertEquals(0, relaxedConstraintViolations.size());
		assertEquals(0, strictConstraintViolations.size());
	}

	@Test
	public void testInvalidV3000Molfile() {
		RelaxedV3000Entity relaxedEntity = new RelaxedV3000Entity(invalidV3000Molfile);
		StrictV3000Entity strictEntity = new StrictV3000Entity(invalidV3000Molfile);
		Set<ConstraintViolation<RelaxedV3000Entity>> relaxedConstraintViolations = validator.validate(relaxedEntity);
		Set<ConstraintViolation<StrictV3000Entity>> strictConstraintViolations = validator.validate(strictEntity);

		// invalid for both relaxed and strict
		assertEquals(1, relaxedConstraintViolations.size());
		assertEquals(1, strictConstraintViolations.size());
		assertEquals("invalid MDL Molfile", relaxedConstraintViolations.iterator().next().getMessage());
		assertEquals("invalid MDL Molfile", strictConstraintViolations.iterator().next().getMessage());
	}

	@Test
	public void testV3000MolfileWithJavaScript() {
		RelaxedV3000Entity relaxedEntity = new RelaxedV3000Entity(evilV3000Molfile);
		StrictV3000Entity strictEntity = new StrictV3000Entity(evilV3000Molfile);
		Set<ConstraintViolation<RelaxedV3000Entity>> relaxedConstraintViolations = validator.validate(relaxedEntity);
		Set<ConstraintViolation<StrictV3000Entity>> strictConstraintViolations = validator.validate(strictEntity);

		// valid for both relaxed and strict
		assertEquals(0, relaxedConstraintViolations.size());
		assertEquals(0, strictConstraintViolations.size());
	}

	private class DefaultEntity {
		@Molfile
		private final String molecule;

		public DefaultEntity(String molecule) {
			this.molecule = molecule;
		}
	}

	@Test
	public void testDefaultFormat() {
		DefaultEntity validEntity = new DefaultEntity(validV2000Molfile);
		DefaultEntity invalidEntity = new DefaultEntity(validV3000Molfile);

		Set<ConstraintViolation<DefaultEntity>> constraintViolations = validator.validate(validEntity);
		assertEquals(0, constraintViolations.size());

		constraintViolations = validator.validate(invalidEntity);
		assertEquals(1, constraintViolations.size());
	}

	private class OverwrittenMessageEntity {
		@Molfile(message = "overwritten message")
		private final String molecule;

		public OverwrittenMessageEntity(String molecule) {
			this.molecule = molecule;
		}
	}

	@Test
	public void testOverwrittenMessage() {
		OverwrittenMessageEntity entity = new OverwrittenMessageEntity(invalidV2000Molfile);
		Set<ConstraintViolation<OverwrittenMessageEntity>> constraintViolations = validator.validate(entity);

		assertEquals("overwritten message", constraintViolations.iterator().next().getMessage());
	}
}