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
	 * This constructor should not be used directly to receive an instance of
	 * this class. Use the static factory method "newEditor" instead. 
	 */
	constructor(divId, molecule) {
		super();

		this._divId = divId;
		this._molecule = molecule;
		this._editor = null;
	}

	/**
	 * Returns an initialized OpenChemLibJS editor instance embedded inside a
	 * Promise. The editor is rendered in a <div> container with the id given by
	 * the parameter "divId" and a molecule according to the "molecule" parameter.
	 */
	static newEditor(divId, molecule) {
		return new Promise((resolve, reject) => {
			let obj = new molecularfaces.OpenChemLibJSEditor(divId, molecule);
			obj.init().then(resolve(obj));
		});
	}

	init() {
		return new Promise((resolve, reject) => {
			// Clear all previously rendered editors in our <div>.
			document.getElementById(this._divId).innerHTML = '';

			this._editor = window.OCL.StructureEditor.createSVGEditor(this._divId, 1);

			this.setMDLv2000(this._molecule);

			let obj = this;
			this._editor.setChangeListenerCallback(function(idcode, molecule) {
				let mol = molecule.toMolfile();

				obj._molecule = mol;
				obj.notifyChange(mol);
			});

			resolve(this);
		});
	}

	getMDLv2000() {
		return this._molecule;
	}

	setMDLv2000(molecule) {
		return new Promise((resolve, reject) => {
			if (typeof molecule !== "undefined") {
				this._molecule = molecule;

				// OCL.StructureEditor's setMolFile() will fire an onChange event.
				this._editor.setMolFile(molecule);
			}

			resolve(this);
		});
	}

	getEditorObj() {
		return this._editor;
	}
}