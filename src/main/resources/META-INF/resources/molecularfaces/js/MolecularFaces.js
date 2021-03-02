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
 * Global variable that stores a ResourcesLoader instance to be commonly used by
 * all OpenChemLibJS plugin instances.
 */
molecularfaces.openChemLibJSLoaderInstance = new molecularfaces.ResourcesLoader();

/**
 * Global variable that stores a ResourcesLoader instance to be commonly used by
 * all MolPaintJS plugin instances.
 */
molecularfaces.molPaintJSLoaderInstance = new molecularfaces.ResourcesLoader();

/**
 * Global variable that stores a ResourcesLoader instance to be commonly used by
 * all Marvin JS plugin instances.
 */
molecularfaces.marvinJSLoaderInstance = new molecularfaces.ResourcesLoader();

/**
 * Global variable that stores the common MolPaintJS plugin registry. It is 
 * initialized lazily by the classes molecularfaces.MolPaintJSEditor and 
 * molecularfaces.MolPaintJSViewer.
 */
molecularfaces._molPaintJSRegistry = null;

/**
 * Singleton instance of molecularfaces.MarvinJSNamespaceLoader. The class 
 * molecularfaces.MarvinJSViewer needs to ensure its lazy loading.
 */
molecularfaces._marvinJSNamespaceLoaderInstance = null;

/**
 * A vanilla JavaScript on-document-ready execution of function fn. See 
 * https://stackoverflow.com/a/9899701.
 */
molecularfaces.onDocumentReady = function(fn) {
	if (document.readyState === "complete" || document.readyState === "interactive") {
		setTimeout(fn, 1);
	} else {
		document.addEventListener("DOMContentLoaded", fn);
	}
}