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

public class MolFileValidator implements ConstraintValidator<MolFile, String> {
	private MolFile annotation;

	@Override
	public void initialize(MolFile constraintAnnotation) {
		annotation = constraintAnnotation;
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.isEmpty()) {
			return true;
		}

		// translation between different enumerations
		Mode mode;
		switch (annotation.mode()) {
		case RELAXED:
			mode = Mode.RELAXED;
			break;
		case STRICT:
		default:
			mode = Mode.STRICT;
			break;
		}

		// try to read the molfile
		try (MDLV2000Reader reader = new MDLV2000Reader(new StringReader(value), mode)) {
			reader.read(new AtomContainer());
			return true;
		} catch (Exception e) {
			// TODO: fill the constraint validation message with something useful from the exception
			return false;
		}
	}
}