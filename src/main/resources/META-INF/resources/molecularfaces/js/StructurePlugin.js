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
"use strict";

// Namespace registration
var molecularfaces = molecularfaces || {};

/**
 * Abstract class
 * 
 * This class represents a chemical structure plugin (editor or viewer).
 */
molecularfaces.StructurePlugin = class {
	constructor() {
		if (new.target === molecularfaces.StructurePlugin) {
			throw new TypeError("Cannot construct instances of this abstract class.");
		}
	}

	/**
	 * Abstract method
	 * 
	 * This method shall be used for reinitialization of the plugin.
	 * 
	 * To support method chaining, this method should return this object.
	 */
	init() {
		throw new Error("This method is abstract and must be implemented by the subclass.");
	}

	/**
	 * Returns the stored molecule in the MDL Molfile v2000 format.
	 */
	getMol() {
		return this.getMDLv2000();
	}

	/**
	 * Sets the molecule in the MDL Molfile v2000 format.
	 * 
	 * Returns this object to support method chaining.
	 */
	setMol(molecule) {
		this.setMDLv2000(molecule);

		return this;
	}

	/**
	 * Abstract method
	 * 
	 * Returns the stored molecule in the MDL Molfile v2000 format.
	 */
	getMDLv2000() {
		throw new Error("This method is abstract and must be implemented by the subclass.");
	}

	/**
	 * Abstract method
	 * 
	 * Sets the molecule in the MDL Molfile v2000 format.
	 * 
	 * To support method chaining, this method should return this object.
	 */
	setMDLv2000(molecule) {
		throw new Error("This method is abstract and must be implemented by the subclass.");
	}
}