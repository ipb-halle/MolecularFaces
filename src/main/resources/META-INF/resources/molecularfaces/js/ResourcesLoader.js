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
 * This class provides support for dynamic resource loading of JavaScript and
 * stylesheet files.
 * 
 * Usage:
 * - enqueue resources with the addScriptToHead(String) and addCssToHead(String)
 *   methods
 * - start attachment of the resources to <head> by calling loadResources()
 * - register a callback function with onLoad(function) that is called as soon as
 *   ALL resources are loaded.
 *
 * Identical resources may be enqueued several times, but are loaded exactly once.
 */
molecularfaces.ResourcesLoader = class {
	constructor() {
		this._promises = [];
		this._resources = [];
	}

	/**
	 * Enqueues the loading of a JavaScript file.
	 */
	addScriptToHead(src) {
		if (!this._resources.includes(src)) {
			this._resources.push(src);

			let script = document.createElement("script");

			this._promises.push(new Promise(function(resolve, reject) {
				script.onload = () => resolve(script);
				script.onerror = () => reject(new Error("Load error for script " + src));
			}));

			script.setAttribute("type", "text/javascript");
			script.setAttribute("src", src);

			document.head.appendChild(script);
		}

		return this;
	}

	/**
	 * Enqueues the loading of a stylesheet file.
	 */
	addCssToHead(href) {
		if (!this._resources.includes(href)) {
			this._resources.push(href);

			let link = document.createElement("link");

			this._promises.push(new Promise(function(resolve, reject) {
				link.onload = () => resolve(link);
				link.onerror = () => reject(new Error("Load error for css " + href));
			}));

			link.setAttribute("rel", "stylesheet");
			link.setAttribute("type", "text/css");
			link.setAttribute("href", href);

			document.head.appendChild(link);
		}

		return this;
	}

	status() {
		return Promise.all(this._promises);
	}
}