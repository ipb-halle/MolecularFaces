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

		this._changeListeners = [];
	}

	/**
	 * Adds a callback listener that will be notified upon a change of the molecule.
	 * The function callback function fn will receive the new molecule (in the MDL 
	 * Molfile v2000 format) as parameter.
	 * 
	 * Returns this object to support method chaining.
	 */
	addChangeListener(fn) {
		this._changeListeners.push(fn);

		return this;
	}

	/**
	 * Removes an on-change callback listener.
	 * 
	 * Returns this object to support method chaining.
	 */
	removeChangeListener(fn) {
		let index = this._changeListeners.indexOf(fn);
		if (index > -1) {
			this._changeListeners.splice(index);
		}

		return this;
	}

	/**
	 * Notifies all registered on-change callback listeners about the new molecule 
	 * (in the MDL Molfile v2000 format).
	 */
	notifyChange(newMolecule) {
		for (let fn of this._changeListeners) {
			fn.call(this, newMolecule);
		}
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

	/* 
	 * Note: setMol(molecule) is not overridden. It should not trigger 
	 * notifyChange(newMolecule) explicitly.
	 */
}

/**
 * This class implements the OpenChemLibJS editor plugin. The editor is attached 
 * to a <div> container. 
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

		this._init();
	}

	_init() {
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

/**
 * This class implements the OpenChemLibJS viewer plugin. The molecule viewer is 
 * attached as <svg> to a <div> container.
 */
molecularfaces.OpenChemLibJSViewer = class extends molecularfaces.StructurePlugin {
	/**
	 * Initializes the OpenChemLibJS viewer in a <div> container with the id given 
	 * by the parameter "divId" and sets its molecule according to the "molecule" 
	 * parameter. The "height" and "width" parameters should not exceed the size of 
	 * the surrounding <div>.
	 */
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