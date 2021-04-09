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
	constructor(divId, molecule, installPath, height, width, format) {
		super();

		this._divId = divId;
		this._molecule = molecule;
		this._installPath = installPath;
		this._height = height;
		this._format = format;
		this._width = width;
	}

	/**
	 * Returns an initialized MolPaintJS viewer instance embedded inside a 
	 * Promise. The viewer is rendered in a <div> container with the id given by
	 * the parameter "divId" and a molecule according to the "molecule" parameter.
	 * The install location of Marvin JS needs to be defined by the parameter
	 * "installPath". The "height" and "width" parameters should not exceed the
	 * size of the surrounding <div>. The chemical file format needs to be
	 * specified via the "format" parameter.
	 */
	static newViewer(divId, molecule, installPath, height, width, format) {
		return new Promise((resolve, reject) => {
			let obj = new molecularfaces.MarvinJSViewer(divId, molecule, installPath, height, width, format);
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
				if (obj._format === "MDLV2000") {
					// Get image of the molecule as SVG.
					let imgData = namespace.ImageExporter.molToDataUrl(mol, "image/svg", settings);

					obj._insertSvg(obj._divId, imgData);
					resolve(obj);
				} else if (obj._format === "MDLV3000") {
					// snippets from https://marvinjs-demo.chemaxon.com/latest/examples/example-create-image.html
					let defaultServices = getDefaultServices(); // function in webservices.js
					let services = {};
					services['molconvertws'] = defaultServices['molconvertws'];

					let params = {
						'imageType': "image/svg",
						'settings': settings,
						'inputFormat': "mol:V3",
						'services': services
					}

					let exporter = new namespace.ImageExporter(params);

					// MarvinJS needs to ask the webservice for V3000, thus we get a Promise.
					exporter.render(mol).then((imgData) => {
						obj._insertSvg(obj._divId, imgData);
						resolve(obj);
					});
				}
			});
		});
	}

	_insertSvg(divId, svg) {
		// target <div>
		let div = document.getElementById(divId);

		// clear everything (might be relevant if setMolecule(molecule) is called)
		div.innerHTML = '';

		// attach the image
		div.insertAdjacentHTML('beforeend', svg);
	}

	getMolecule() {
		return this._molecule;
	}

	setMolecule(molecule) {
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