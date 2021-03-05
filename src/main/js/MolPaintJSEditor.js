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
	 * This constructor should not be used directly to receive an instance of
	 * this class. Use the static factory method "newEditor" instead. 
	 */
	constructor(divId, molecule, height, width) {
		super();

		this._divId = divId;
		this._molecule = molecule;
		this._height = height;
		this._width = width;
		this._iconSize = 32;
		this._editor = null;
	}

	/**
	 * Returns an initialized MolPaintJS editor instance embedded inside a
	 * Promise. The editor is rendered in a <div> container with the id given by
	 * the parameter "divId" and a molecule according to the "molecule" parameter.
	 * The "height" and "width" parameters should not exceed the size of the
	 * surrounding <div>.
	 */
	static newEditor(divId, molecule, height, width) {
		return new Promise((resolve, reject) => {
			let obj = new molecularfaces.MolPaintJSEditor(divId, molecule, height, width);
			obj.init().then(resolve(obj));
		});
	}

	init() {
		return new Promise((resolve, reject) => {
			this._editor = molPaintJS.newContext(this._divId, {
				iconSize: this._iconSize,
				sizeX: this._width - 2 * this._iconSize - 2,
				sizeY: this._height - this._iconSize - 7
			});
			this._editor.init();
			this._editor.setMolecule(this._molecule);

			let obj = this;
			this._editor.setChangeListener(function() {
				let mol = molPaintJS.getMDLv2000(obj._divId);

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

				this._editor.setMolecule(molecule);
			}

			resolve(this);
		});
	}

	getEditorObj() {
		return this._editor;
	}
}