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
 * This class loads an instance of the Marvin JS package namespace to be used
 * for image exports of molecules.
 */
molecularfaces.MarvinJSNamespaceLoader = class {
	/*
	 * Attaches an <iframe> that includes installPath/marvinpack.html to the end 
	 * of <body> and tries to load the Marvin JS package namespace. 
	 */
	constructor(installPath) {
		this._installPath = installPath;
		this._promise = this._loadMarvinJSPackage();
	}

	/**
	 * Loads the Marvin JS namespace and returns it embedded in a Promise.
	 * See https://marvinjs-demo.chemaxon.com/latest/examples/example-create-image.html.
	 */
	_loadMarvinJSPackage() {
		// Create an iframe that inserts marvinpack.html.
		let iframe = document.createElement("iframe");

		let iframePromise = new Promise(function(resolve, reject) {
			iframe.onload = () => resolve(iframe);
			iframe.onerror = () => reject(new Error("Load error for iframe " + iframe));
		});

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

		return iframePromise.then(() => { return MarvinJSUtil.getPackage("#marvinjspackage-iframe"); })
			.then((marvinNameSpace) => {
				return new Promise((resolve, reject) => {
					marvinNameSpace.onReady(() => resolve(marvinNameSpace));
				});
			}, (error) => {
				reject("Cannot retrieve marvin instance from iframe: " + error);
			});
	}

	/**
	 * Returns a Promise object that embeds the Marvin JS namespace.
	 */
	status() {
		return this._promise;
	}
}