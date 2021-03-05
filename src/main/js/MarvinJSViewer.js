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
 * Singleton instance of molecularfaces.MarvinJSNamespaceLoader. The class 
 * molecularfaces.MarvinJSViewer needs to ensure its lazy loading.
 */
molecularfaces._marvinJSNamespaceLoaderInstance = null;

/**
 * This class implements the Marvin JS viewer plugin. The molecule viewer is 
 * attached to a <div> container.
 */
molecularfaces.MarvinJSViewer = class extends molecularfaces.StructurePlugin {
	/**
	 * This constructor should not be used directly to receive an instance of
	 * this class. Use the static factory method "newViewer" instead. 
	 */
	constructor(divId, molecule, installPath, height, width) {
		super();

		this._divId = divId;
		this._molecule = molecule;
		this._installPath = installPath;
		this._height = height;
		this._width = width;
	}

	/**
	 * Returns an initialized MolPaintJS viewer instance embedded inside a 
	 * Promise. The viewer is rendered in a <div> container with the id given by
	 * the parameter "divId" and a molecule according to the "molecule" parameter.
	 * The install location of Marvin JS needs to be defined by the parameter
	 * "installPath". The "height" and "width" parameters should not exceed the
	 * size of the surrounding <div>.
	 */
	static newViewer(divId, molecule, installPath, height, width) {
		return new Promise((resolve, reject) => {
			let obj = new molecularfaces.MarvinJSViewer(divId, molecule, installPath, height, width);
			obj.init().then(resolve(obj));
		});
	}

	init() {
		return new Promise((resolve, reject) => {
			// Try to initialize the common Marvin package namespace.
			if (molecularfaces._marvinJSNamespaceLoaderInstance == null) {
				molecularfaces._marvinJSNamespaceLoaderInstance = new molecularfaces.MarvinJSNamespaceLoader(this._installPath);
			}

			// Plot settings
			let settings = {
				width: this._width,
				height: this._height,
				zoomMode: "autoshrink"
			};

			// MarvinJS has some problems with empty molecule strings.
			let mol = null;
			if (this._molecule !== "") {
				mol = this._molecule;
			}

			// Try to plot the image as soon as we have the Marvin package namespace.
			let obj = this;
			molecularfaces._marvinJSNamespaceLoaderInstance.status().then((namespace) => {
				// Get image of the molecule as SVG.
				let imgData = namespace.ImageExporter.molToDataUrl(mol, "image/svg", settings);

				// target <div>
				let div = document.getElementById(obj._divId);

				// clear everything (might be relevant if setMDLv2000(molecule) is called)
				div.innerHTML = '';

				// attach the image
				div.insertAdjacentHTML('beforeend', imgData);
				resolve(obj);
			});
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