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
 * stylesheet files. Identical resources may be enqueued several times, but are
 * loaded exactly once.
 * 
 * Usage:
 * - enqueue resources with the add...(String) methods
 * - the Promise that status() returns will resolve as soon as all enqueued
 *   resources are loaded.
 */
molecularfaces.ResourcesLoader = class {
	constructor() {
		this._promises = [];
		this._resources = [];
	}

	/**
	 * Enqueues the loading of a JavaScript file to be added to &lt;head&gt;.
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
	 * Enqueues the loading of a JavaScript file to be added to the top of
	 * &lt;<body&gt;.
	 */
	addScriptToBodyAtTop(src) {
		if (!this._resources.includes(src)) {
			this._resources.push(src);

			let script = document.createElement("script");

			this._promises.push(new Promise(function(resolve, reject) {
				script.onload = () => resolve(script);
				script.onerror = () => reject(new Error("Load error for script " + src));
			}));

			script.setAttribute("type", "text/javascript");
			script.setAttribute("src", src);

			document.body.prepend(script);
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

	/**
	 * Returns a Promise that indicates the load status of all enqueued resources.
	 */
	status() {
		return Promise.all(this._promises);
	}
}