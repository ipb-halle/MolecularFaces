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
 * Singleton instances of ResourcesLoader to be commonly used by the specific
 * plugin type instances.
 */
molecularfaces.openChemLibJSLoaderInstance = new molecularfaces.ResourcesLoader();
molecularfaces.molPaintJSLoaderInstance = new molecularfaces.ResourcesLoader();
molecularfaces.marvinJSLoaderInstance = new molecularfaces.ResourcesLoader();
molecularfaces.openVectorEditorLoaderInstance = new molecularfaces.ResourcesLoader();

/**
 * Promise that resolves upon on-document-ready.
 */
molecularfaces._onDocumentReadyPromise = new Promise((resolve) => {
	/**
	  * Vanilla JavaScript on-document-ready.
	  * See https://stackoverflow.com/a/9899701.
	  */
	if (document.readyState === "complete" || document.readyState === "interactive") {
		setTimeout(() => resolve(), 1);
	} else {
		document.addEventListener("DOMContentLoaded", () => resolve());
	}
});