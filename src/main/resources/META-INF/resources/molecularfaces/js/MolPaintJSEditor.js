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
 * This class implements the MolPaintJS editor plugin. The editor is attached 
 * to a <div> container. 
 */
molecularfaces.MolPaintJSEditor = class extends molecularfaces.StructureEditor {
	/**
	 * Initializes the MolPaintJS editor in a <div> container with the id given 
	 * by the parameter "divId" and sets its molecule according to the "molecule" 
	 * parameter. The plugin resource location needs to be defined by the parameter 
	 * "installPath". The "height" and "width" parameters should not exceed the 
	 * size of the surrounding <div>.
	 */
	constructor(divId, molecule, installPath, height, width) {
		super();

		this._divId = divId;
		this._molecule = molecule;
		this._installPath = installPath;
		this._height = height;
		this._width = width;
		this._iconSize = 32;
		this._editor = null;

		this.init();
	}

	init() {
		// Try to initialize the plugin registry.
		if (molecularfaces._molPaintJSRegistry == null) {
			molecularfaces._molPaintJSRegistry = new MolPaintJS();
		}

		this._editor = molecularfaces._molPaintJSRegistry.newContext(this._divId, {
			installPath: this._installPath,
			iconSize: this._iconSize,
			sizeX: this._width - 2 * this._iconSize - 2,
			sizeY: this._height - this._iconSize - 7
		});
		this._editor.setMolecule(this._molecule);

		let obj = this;
		this._editor.setChangeListener(function() {
			/*
			  * The object 'this' is not available in this scope, because the 
			  * callback is executed in the future. The let construct above solves 
			  * this issue.
			  */

			let mol = obj.getMDLv2000();

			obj._molecule = mol;
			obj.notifyChange(mol);
		});

		this._editor.init();

		return this;
	}

	getMDLv2000() {
		return molecularfaces._molPaintJSRegistry.getMDLv2000(this._divId);
		//return this._molecule;
	}

	setMDLv2000(molecule) {
		if (typeof molecule !== "undefined") {
			this._molecule = molecule;

			this._editor.setMolecule(molecule);
		}

		return this;
	}

	getEditorObj() {
		return this._editor;
	}
}