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
 * This class represents a chemical structure editor. It supports the observer-
 * pattern for changes of the molecule.
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
	 * The callback function "fn" will receive the new molecule as parameter.
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
	 * Notifies all registered on-change callback listeners about the new molecule.
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
	 * Note: setMolecule(molecule) is not overridden. It should not trigger 
	 * notifyChange(newMolecule) explicitly, but the editor plugin itself may
	 * trigger an on-change event.
	 */
}