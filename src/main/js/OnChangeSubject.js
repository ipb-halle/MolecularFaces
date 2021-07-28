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
 * The implementation of the subject in an observer pattern.
 */
molecularfaces.OnChangeSubject = class {
	constructor() {
		this._changeCallbacks = [];
	}

	/**
	 * Adds a callback as observer that will be notified upon a change event. The
	 * callback function "fn" will receive the new data as parameter.
	 * 
	 * Returns this object to support method chaining.
	 */
	addChangeCallback(fn) {
		this._changeCallbacks.push(fn);

		return this;
	}

	/**
	 * Removes an on-change callback.
	 * 
	 * Returns this object to support method chaining.
	 */
	removeChangeCallback(fn) {
		let index = this._changeCallbacks.indexOf(fn);
		if (index > -1) {
			this._changeCallbacks.splice(index);
		}

		return this;
	}

	/**
	 * Calls all registered on-change callbacks with the new data.
	 */
	notifyChange(data) {
		for (let fn of this._changeCallbacks) {
			fn.call(this, data);
		}
	}
}