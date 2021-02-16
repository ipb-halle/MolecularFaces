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
	 * Abstract method
	 * 
	 * This method shall be used for reinitialization of the plugin.
	 * 
	 * To support method chaining, this method should return this object.
	 */
	init() {
		throw new Error("This method is abstract and must be implemented by the subclass.");
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
	 * The callback function fn will receive the new molecule (in the MDL 
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
 * to a <div> container. The size of the plugin is defined by the surrounding <div>.
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

		this.init();
	}

	init() {
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

		return this;
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

		this.init();
	}

	init() {
		let svg = window.OCL.Molecule.fromMolfile(this._molecule).toSVG(this._width, this._height, null);
		document.getElementById(this._divId).innerHTML = svg;

		return this;
	}

	getMDLv2000() {
		return this._molecule;
	}

	setMDLv2000(molecule) {
		if (typeof molecule !== "undefined") {
			this._molecule = molecule;

			this.init();
		}

		return this;
	}
}

/**
 * Global variable that stores the common MolPaintJS plugin registry. It is 
 * initialized lazily by the classes molecularfaces.MolPaintJSEditor and 
 * molecularfaces.MolPaintJSViewer.
 */
molecularfaces._molPaintJSRegistry = null;

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
			iconSize: 32,
			sizeX: this._width,
			sizeY: this._height
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

/**
 * A vanilla JavaScript on-document-ready execution of function fn. See 
 * https://stackoverflow.com/a/9899701.
 */
molecularfaces._onDocumentReady = function(fn) {
	if (document.readyState === "complete" || document.readyState === "interactive") {
		setTimeout(fn, 1);
	} else {
		document.addEventListener("DOMContentLoaded", fn);
	}
}

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
		molecularfaces._onDocumentReady(function() {
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

/**
 * This class loads an instance of of the Marvin JS package namespace to be used 
 * for image exports of molecules. It supports the observer-pattern for 
 * notification as soon as the namespace becomes available.
 */
molecularfaces.MarvinJSNamespaceLoader = class {
	/*
	 * Attaches an <iframe> that includes installPath/marvinpack.html to the end 
	 * of <body> and tries to load the Marvin JS package namespace. 
	 */
	constructor(installPath) {
		this._installPath = installPath;

		this._marvinJSViewerPackage = null;
		this._finishListeners = [];

		this._loadMarvinJSPackage();
	}

	/**
	 * Loads the Marvin JS namespace.
	 * See https://marvinjs-demo.chemaxon.com/latest/examples/example-create-image.html.
	 */
	_loadMarvinJSPackage() {
		// Create an iframe that inserts marvinpack.html.
		let iframe = document.createElement("iframe");

		let idAttribute = document.createAttribute("id");
		idAttribute.value = "marvinjspackage-iframe";
		iframe.setAttributeNode(idAttribute);

		let srcAttribute = document.createAttribute("src");
		srcAttribute.value = this._installPath + "/marvinpack.html";
		iframe.setAttributeNode(srcAttribute);

		let styleAttribute = document.createAttribute("style");
		styleAttribute.value = "width:0;height:0;display:initial;position:absolute;left:-1000;top:-1000;margin:0;padding:0;";
		iframe.setAttributeNode(styleAttribute);

		// Attach this iframe to the end of <body>.
		document.body.append(iframe);

		let obj = this;
		// Execute package loading on-document-ready
		molecularfaces._onDocumentReady(
			function() {
				MarvinJSUtil.getPackage("#marvinjspackage-iframe").then(function(marvinNameSpace) {
					marvinNameSpace.onReady(function() {
						obj._marvinJSViewerPackage = marvinNameSpace;
						obj._notifyListeners();
					});
				}, function(error) {
					alert("Cannot retrieve marvin instance from iframe:" + error);
				});
			}
		);
	}

	/**
	 * Adds a callback listener that will be notified as soon as 
	 * this._marvinJSViewerPackage is initialized. The callback function fn will 
	 * receive the _marvinJSViewerPackage as parameter. Execution of fn may also 
	 * happen immediately if this._marvinJSViewerPackage has already been 
	 * initialized.
	 */
	addFinishListener(fn) {
		if (this._marvinJSViewerPackage == null) {
			// _marvinJSViewerPackage is not yet initialized, notify later
			this._finishListeners.push(fn);
		} else {
			// _marvinJSViewerPackage is initialized, notify now
			this._notifyListener(fn);
		}
	}

	/**
	 * Notifies all registered listeners that this._marvinJSViewerPackage is 
	 * initialized.
	 */
	_notifyListeners() {
		for (let fn of this._finishListeners) {
			this._notifyListener(fn);
		}
	}

	/**
	 * Notifies a single listener that this._marvinJSViewerPackage is 
	 * initialized.
	 */
	_notifyListener(fn) {
		fn.call(this, this._marvinJSViewerPackage);
	}
}

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
	 * Initializes the MolPaintJS viewer in a <div> container with the id given 
	 * by the parameter "divId" and sets its molecule according to the "molecule" 
	 * parameter. The install location of Marvin JS needs to be defined by the 
	 * parameter "installPath". The "height" and "width" parameters should not 
	 * exceed the size of the surrounding <div>.
	 */
	constructor(divId, molecule, installPath, height, width) {
		super();

		this._divId = divId;
		this._molecule = molecule;
		this._installPath = installPath;
		this._height = height;
		this._width = width;

		this.init();
	}

	init() {
		// Try to initialize the plugin registry.
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

		// Try to plot the image as soon as we have the Marvin namespace.
		let obj = this;
		molecularfaces._marvinJSNamespaceLoaderInstance.addFinishListener(function(namespace) {
			// Get image of the molecule as SVG.
			let imgData = namespace.ImageExporter.molToDataUrl(mol, "image/svg", settings);

			// target <div>
			let div = document.getElementById(obj._divId);

			// clear everything (might be relevant if setMDLv2000(molecule) is called)
			div.innerHTML = '';

			// attach the image
			div.insertAdjacentHTML('beforeend', imgData);
		});

		return this;
	}

	getMDLv2000() {
		return this._molecule;
	}

	setMDLv2000(molecule) {
		if (typeof molecule !== "undefined") {
			this._molecule = molecule;

			this.init();
		}

		return this;
	}
}