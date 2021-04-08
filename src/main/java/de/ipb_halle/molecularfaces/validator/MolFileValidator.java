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

import java.io.StringReader;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.io.IChemObjectReader.Mode;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV3000Reader;

import de.ipb_halle.molecularfaces.validator.Molfile.Format;

/**
 * Checks that a given String is a valid MDL Molfile according to the CDK
 * library.
 * 
 * @author flange
 */
public class MolFileValidator implements ConstraintValidator<Molfile, String> {
	private Mode cdkReaderMode;
	private Format format;

	@Override
	public void initialize(Molfile constraintAnnotation) {
		format = constraintAnnotation.format();

		// translation between different enumerations
		switch (constraintAnnotation.mode()) {
		case RELAXED:
			cdkReaderMode = Mode.RELAXED;
			break;
		case STRICT:
		default:
			cdkReaderMode = Mode.STRICT;
			break;
		}
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.isEmpty()) {
			return true;
		}

		// try to read the molfile
		if (format == Format.V2000) {
			try (MDLV2000Reader reader = new MDLV2000Reader(new StringReader(value), cdkReaderMode)) {
				reader.read(new AtomContainer());
				return true;
			} catch (Exception e) {
				return false;
			}
		} else if (format == Format.V3000) {
			try (MDLV3000Reader reader = new MDLV3000Reader(new StringReader(value), cdkReaderMode)) {
				reader.read(new AtomContainer());
				return true;
			} catch (Exception e) {
				return false;
			}
		} else {
			return false;
		}
	}
}