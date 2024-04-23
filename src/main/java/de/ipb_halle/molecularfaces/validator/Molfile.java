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

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Molfile check constraint.
 * 
 * @author flange
 */
@Target({ FIELD, METHOD, PARAMETER, ANNOTATION_TYPE, TYPE_USE })
@Retention(RUNTIME)
@Constraint(validatedBy = MolfileValidator.class)
@Documented
public @interface Molfile {
	String message() default "invalid MDL Molfile";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	public enum Mode {
		/**
		 * Only fail on serious format problems in the molfile.
		 */
		RELAXED,
		/**
		 * Fail on any format problem in the molfile.
		 */
		STRICT;
	}

	Mode mode() default Mode.RELAXED;

	public enum Format {
		/**
		 * MDL Molfile V2000
		 */
		V2000,
		/**
		 * MDL Molfile V3000
		 */
		V3000;
	}

	Format format() default Format.V2000;
}
