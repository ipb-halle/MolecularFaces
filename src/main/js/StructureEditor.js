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
 * This class represents a chemical structure editor.
 */
molecularfaces.StructureEditor = class extends molecularfaces.StructurePlugin {
	constructor() {
		super();

		if (new.target === molecularfaces.StructureEditor) {
			throw new TypeError("Cannot construct instances of this abstract class.");
		}

		this._onChangeSubject = new molecularfaces.OnChangeSubject();
	}

	/**
	 * Returns the OnChangeSubject instance that observes changes of this editor's
	 * molecule.
	 */
	getOnChangeSubject() {
		return this._onChangeSubject;
	}

	/**
	 * Abstract method
	 *
	 * Returns the internally used editor object. May return null if such an object 
	 * is not available.
	 */
	getEditorObj() {
		throw new Error("This method is abstract and must be implemented by the subclass.");
	}
}