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
 * This class implements the MarvinJS editor plugin. The editor is rendered inside 
 * an <iframe> container.
 */
molecularfaces.MarvinJSEditor = class extends molecularfaces.StructureEditor {
	/**
	 * Initializes the MarvinJS editor in an <iframe> container with the id given 
	 * by the parameter "iframeId" and sets its molecule according to the 
	 * "molecule" parameter. The plugin resource location needs to be defined by 
	 * the parameter "installPath" and the license location (relative to 
	 * "installPath") is defined by the parameter "licensePath"). The "height" 
	 * and "width" parameters should not exceed the size of the surrounding 
	 * container.
	 */
	constructor(iframeId, molecule, installPath, licensePath, height, width) {
		super();

		this._iframeId = iframeId;
		this._molecule = molecule;
		this._installPath = installPath;
		this._licensePath = licensePath;
		this._height = height;
		this._width = width;
		this._editor = null;

		this.init();
	}

	init() {
		// MarvinJS has some problems with empty molecule strings.
		let mol = null;
		if (this._molecule !== "") {
			mol = this._molecule;
		}

		let obj = this;
		molecularfaces.onDocumentReady(function() {
			// Set the license via the Marvin JS package.
			if (obj._licensePath != "") {
				MarvinJSUtil.getPackage("#" + obj._iframeId).then(function(marvinNameSpace) {
					marvinNameSpace.onReady(function() {
						marvinNameSpace.Sketch.license(obj._licensePath);
					});
				}, function(error) {
					alert("Cannot retrieve marvin instance from iframe:" + error);
				});
			}

			// Draw the editor in the given iframe.
			MarvinJSUtil.getEditor("#" + obj._iframeId).then(function(sketcherInstance) {
				obj._editor = sketcherInstance;

				// Set the molecule
				obj._editor.importStructure("mol", mol).catch(function(error) {
					alert(error);
				});

				// Register an on-change listener.
				obj._editor.on("molchange", function() {
					let molecule = obj._editor.exportAsMol();

					obj._molecule = molecule;
					obj.notifyChange(molecule);
				});
			}, function(error) {
				alert("Cannot retrieve MarvinJS sketcher instance from iframe:" + error);
			});
		});

		return this;
	}

	getMDLv2000() {
		return this._editor.exportAsMol();
		//return this._molecule;
	}

	setMDLv2000(molecule) {
		if (typeof molecule !== "undefined") {
			this._molecule = molecule;

			// MarvinJS has some problems with empty molecule strings.
			let mol = null;
			if (molecule !== "") {
				mol = molecule;
			}
			this._editor.importStructure("mol", mol).catch(function(error) {
				alert(error);
			});
		}

		return this;
	}

	getEditorObj() {
		return this._editor;
	}
}