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
		molecularfaces.onDocumentReady(
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