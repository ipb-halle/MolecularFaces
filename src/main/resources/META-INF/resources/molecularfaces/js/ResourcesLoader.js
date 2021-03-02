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
		this._resourcesToLoad = [];
		this._loadingResources = [];
		this._loadedResources = [];
		this._onLoadListeners = [];

		let obj = this;
		this._onLoadCallback = function(resource) {
			// Add this resource to the list of loaded resources.
			obj._loadedResources.push(resource);

			// Remove this resource from the loading resources list.
			obj._loadingResources.splice(obj._loadingResources.indexOf(resource), 1);

			// Notify the listeners in case all resources are loaded.
			if (obj._loadingResources.length == 0) {
				obj._notifyOnLoad();
			}
		}
	}

	/**
	 * Enqueues the loading of a JavaScript file.
	 */
	addScriptToHead(src) {
		if (!this._loadingResources.includes(src)
			&& !this._loadedResources.includes(src)) {

			this._loadingResources.push(src);

			let script = document.createElement("script");
			script.setAttribute("type", "text/javascript");
			script.setAttribute("src", src);

			let obj = this;
			script.onreadystatechange = function() {
				if (script.readyState == 'complete') {
					obj._onLoadCallback(src);
				}
			}
			script.onload = function() {
				obj._onLoadCallback(src);
			}

			// Enqueue this element to be appended to the DOM tree by loadResources().
			this._resourcesToLoad.push(script);
		}

		return this;
	}

	/**
	 * Enqueues the loading of a stylesheet file.
	 */
	addCssToHead(href) {
		if (!this._loadingResources.includes(href)
			&& !this._loadedResources.includes(href)) {

			this._loadedResources.push(href);

			let link = document.createElement("link");
			link.setAttribute("rel", "stylesheet");
			link.setAttribute("type", "text/css");
			link.setAttribute("href", href);

			// Enqueue this element to be appended to the DOM tree by loadResources().
			this._resourcesToLoad.push(link);
		}

		return this;
	}

	/**
	 * Adds all enqueued resources to <head>.
	 */
	loadResources() {
		for (let element of this._resourcesToLoad) {
			document.head.appendChild(element);
		}

		return this;
	}

	/**
	 * Registers an onLoad callback function that is executed as soon as all
	 * resources are loaded by the browser. This may happen immediately.
	 */
	onLoad(fn) {
		if (this._loadingResources.length == 0) {
			// All scripts are already loaded, notify now.
			fn.call(this);
		} else {
			// Notify later.
			this._onLoadListeners.push(fn);
		}

		return this;
	}

	/**
	 * Notifies all onLoad listeners.
	 */
	_notifyOnLoad() {
		for (let fn of this._onLoadListeners) {
			fn.call(this);
		}
	}
}