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
 * 
 */
molecularfaces.OpenVectorEditorResizeHelper = class {
	/**
	 * 
	 */
	constructor(iframeId) {
		this._iframeId = iframeId;

		this._initialResize();
		this._addOnClickEventToOpenButton();
	}

	_getIframe() {
		return document.getElementById(this._iframeId);
	}

	_initialResize() {
		let iframe = this._getIframe();
		// These numbers were obtained via trial and error.
        iframe.width  = iframe.contentWindow.document.body.scrollWidth + 20;
        iframe.height = iframe.contentWindow.document.body.scrollHeight + 34;
	}

	_iframeToFullscreen() {
		// style from https://stackoverflow.com/a/14738668
		this._getIframe().setAttribute("style",
			"position:fixed; top:0; left:0; bottom:0; right:0; width:100%; height:100%; " +
			"border:none; margin:0; padding:0; overflow:hidden; z-index:999999;");
	}

	_iframeToSmallSize() {
		this._getIframe().setAttribute("style", "border:none;");
	}

	_addOnClickEventToButton(buttonClasses, fn) {
		let button = this._getIframe().contentDocument.getElementsByClassName(buttonClasses)[0];
		button.addEventListener("click", function() {
			fn.call();
		});
	}

	_addOnClickEventToOpenButton() {
		let obj = this;

		this._addOnClickEventToButton("bp3-button bp3-intent-primary", function() {
			obj._iframeToFullscreen();

			// Do this in the next tick.
			setTimeout(function() {
				obj._addOnClickEventToCloseButton();
			}, 0);
		});
	}

	_addOnClickEventToCloseButton() {
		let obj = this;

		this._addOnClickEventToButton("bp3-button bp3-minimal ve-close-fullscreen-button", function() {
			obj._iframeToSmallSize();

			// Do this in the next tick.
			setTimeout(function() {
				obj._addOnClickEventToOpenButton();
			}, 0);
		});
	}
}