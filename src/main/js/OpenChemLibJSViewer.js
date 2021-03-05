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
 * This class implements the OpenChemLibJS viewer plugin. The molecule viewer is 
 * attached as <svg> to a <div> container.
 */
molecularfaces.OpenChemLibJSViewer = class extends molecularfaces.StructurePlugin {
	/**
	 * This constructor should not be used directly to receive an instance of
	 * this class. Use the static factory method "newViewer" instead. 
	 */
	constructor(divId, molecule, height, width) {
		super();

		this._divId = divId;
		this._molecule = molecule;
		this._height = height;
		this._width = width;
	}

	/**
	 * Returns an initialized OpenChemLibJS viewer instance embedded inside a 
	 * Promise. The viewer is rendered in a <div> container with the id given by
	 * the parameter "divId" and a molecule according to the "molecule" parameter.
	 * The "height" and "width" parameters should not exceed the size of the
	 * surrounding <div>.
	 */
	static newViewer(divId, molecule, height, width) {
		return new Promise((resolve, reject) => {
			let obj = new molecularfaces.OpenChemLibJSViewer(divId, molecule, height, width);
			obj.init().then(resolve(obj));
		});
	}

	init() {
		return new Promise((resolve, reject) => {
			let svg = window.OCL.Molecule.fromMolfile(this._molecule).toSVG(this._width, this._height, null);
			document.getElementById(this._divId).innerHTML = svg;

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

				this.init().then(resolve(this));
			} else {
				resolve(this);
			}
		});
	}
}