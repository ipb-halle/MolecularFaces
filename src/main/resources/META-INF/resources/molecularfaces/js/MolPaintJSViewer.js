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
 * This class implements the MolPaintJS viewer plugin. The molecule viewer is 
 * attached to a <div> container.
 */
molecularfaces.MolPaintJSViewer = class extends molecularfaces.StructurePlugin {
	/**
	 * Initializes the MolPaintJS viewer in a <div> container with the id given 
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
		this._viewer = null;

		this.init();
	}

	init() {
		// Try to initialize the plugin registry.
		if (molecularfaces._molPaintJSRegistry == null) {
			molecularfaces._molPaintJSRegistry = new MolPaintJS();
		}

		this._viewer = molecularfaces._molPaintJSRegistry.newContext(this._divId, {
			installPath: this._installPath,
			iconSize: 32,
			sizeX: this._width,
			sizeY: this._height,
			viewer: 1
		});
		this._viewer.setMolecule(this._molecule);
		this._viewer.init();

		return this;
	}

	getMDLv2000() {
		return this._molecule;
	}

	setMDLv2000(molecule) {
		if (typeof molecule !== "undefined") {
			this._molecule = molecule;
			this._viewer.setMolecule(molecule);
		}

		return this;
	}
}