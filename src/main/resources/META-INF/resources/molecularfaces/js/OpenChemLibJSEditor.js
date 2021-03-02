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
 * This class implements the OpenChemLibJS editor plugin. The editor is attached 
 * to a <div> container. The size of the plugin is defined by the surrounding <div>.
 */
molecularfaces.OpenChemLibJSEditor = class extends molecularfaces.StructureEditor {
	/**
	 * Initializes the OpenChemLibJS editor in a <div> container with the id given 
	 * by the parameter "divId" and sets its molecule according to the "molecule" 
	 * parameter.  
	 */
	constructor(divId, molecule) {
		super();

		this._divId = divId;
		this._molecule = molecule;
		this._editor = null;

		this.init();
	}

	init() {
		// Clear all previously rendered editors in our <div>.
		document.getElementById(this._divId).innerHTML = '';

		this._editor = window.OCL.StructureEditor.createSVGEditor(this._divId, 1);

		this.setMDLv2000(this._molecule);

		let obj = this;
		this._editor.setChangeListenerCallback(function(idcode, molecule) {
			/*
			  * The object 'this' is not available in this scope, because the 
			  * callback is executed in the future. The let construct above solves 
			  * this issue.
			  */

			let mol = molecule.toMolfile();

			obj._molecule = mol;
			obj.notifyChange(mol);
		});

		return this;
	}

	getMDLv2000() {
		return this._editor.getMolFile();
		//return this._molecule;
	}

	setMDLv2000(molecule) {
		if (typeof molecule !== "undefined") {
			this._molecule = molecule;

			// OCL.StructureEditor's setMolFile() will fire an onChange event.
			this._editor.setMolFile(molecule);
		}

		return this;
	}

	getEditorObj() {
		return this._editor;
	}
}