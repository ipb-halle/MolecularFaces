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

class StructurePlugin {
	constructor() {
		if (new.target === StructurePlugin) {
			throw new TypeError("Cannot construct instances of this abstract class.");
		}
	}

	getMol() {
		return this.getMDLv2000();
	}

	setMol(molecule) {
		this.setMDLv2000(molecule);

		return this;
	}

	getMDLv2000() {
		throw new Error("This method is abstract and must be implemented by the subclass.");
	}

	setMDLv2000(molecule) {
		throw new Error("This method is abstract and must be implemented by the subclass.");
	}
}

class StructureEditor extends StructurePlugin {
	constructor() {
		super();

		if (new.target === StructureEditor) {
			throw new TypeError("Cannot construct instances of this abstract class.");
		}

		this._changeListeners = [];
	}

	addChangeListener(fn) {
		this._changeListeners.push(fn);

		return this;
	}

	removeChangeListener(fn) {
		let index = this._changeListeners.indexOf(fn);
		if (index > -1) {
			this._changeListeners.splice(index);
		}

		return this;
	}

	notifyChange(newMolecule) {
		for (let fn of this._changeListeners) {
			fn.call(this, newMolecule);
		}
	}

	// Note: setMol(molecule) is not overridden. It should not trigger notifyChange(newMolecule).
}

class OpenChemLibJSEditor extends StructureEditor {
	constructor(divId, molecule) {
		super();

		this._divId = divId;
		this._molecule = molecule;

		this._init();
	}

	_init() {
		this._editor = window.OCL.StructureEditor.createSVGEditor(this._divId, 1);

		this.setMDLv2000(this._molecule);

		let obj = this;
		this._editor.setChangeListenerCallback(function(idcode, molecule) {
			/*
			  * The object 'this' is not available in this scope, because the callback is executed in the future.
			  * The let construct above solves this issue.
			  */

			let mol = molecule.toMolfile();

			obj._molecule = mol;
			obj.notifyChange(mol);
		});
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
}

class OpenChemLibJSViewer extends StructurePlugin {
	constructor(divId, molecule, height, width) {
		super();

		this._divId = divId;
		this._molecule = molecule;
		this._height = height;
		this._width = width;

		this._init();
	}

	_init() {
		this._svg = window.OCL.Molecule.fromMolfile(this._molecule).toSVG(this._width, this._height, null);
		document.getElementById(this._divId).innerHTML = this._svg;
	}

	getMDLv2000() {
		return this._molecule;
	}

	setMDLv2000(molecule) {
		if (typeof molecule !== "undefined") {
			this._molecule = molecule;
			
			this._init();
		}

		return this;
	}
}