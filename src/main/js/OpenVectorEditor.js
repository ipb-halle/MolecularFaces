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
 * This class implements the OpenVectorEditor editor plugin.
 */
molecularfaces.OpenVectorEditor = class {
	/**
	 * This constructor should not be used directly to receive an instance of
	 * this class. Use the static factory method "newEditor" instead. 
	 */
	constructor(divId, sequence, readonly) {
		this._divId = divId;
		this._sequence = sequence;
		this._readonly = readonly;
		this._editor = null;
		this._onChangeSubject = new molecularfaces.OnChangeSubject();
		this._editorProps = null;

		this._buildEditorProps();
	}

	_buildEditorProps() {
		let obj = this;

		this._editorProps = {
			shouldAutosave: true,
			onSave: function(event, sequenceDataToSave, editorState, onSuccessCallback) {
				obj._sequence = sequenceDataToSave;
				obj._onChangeSubject.notifyChange(sequenceDataToSave);
				onSuccessCallback();
			},
			withPreviewMode: true,
			showReadOnly: false,
			disableSetReadOnly: true,
			showMenuBar: true
		}
	}

	/**
	 * Returns an initialized OpenVectorEditor editor instance embedded inside a
	 * Promise. The editor is rendered in a <div> container with the id given by
	 * the parameter "divId", a sequence according to the "sequence" parameter and
	 * is in read-only mode depending on the "readonly" parameter.
	 */
	static newEditor(divId, sequence, readonly) {
		return new Promise((resolve, reject) => {
			let obj = new molecularfaces.OpenVectorEditor(divId, sequence, readonly);
			obj.init().then(resolve(obj));
		});
	}

	init() {
		return new Promise((resolve, reject) => {
			let domNode = document.getElementById(this._divId);

			// Clear all previously rendered editors in our <div>.
			domNode.innerHTML = '';

			this._editor = window.createVectorEditor(domNode, this._editorProps);

			this.setSequence(this._sequence);

			resolve(this);
		});
	}

	/**
	 * Returns the stored sequence object.
	 */
	getSequence() {
		return this._sequence;
	}

	/**
	 * Sets the sequence.
	 * 
	 * Returns this object embedded in a Promise that indicates the status of this
	 * data change process.
	 */
	setSequence(sequence) {
		return new Promise((resolve, reject) => {
			if (typeof sequence !== "undefined") {
				this._sequence = sequence;
				let isCircular = sequence.circular;

				let newEditorState = {
					readOnly: this._readonly,
					sequenceData: sequence,
					panelsShown: [
						[
							{
								id: "circular",
								name: "Circular Map",
								active: isCircular
							},
							{
								id: "rail",
								name: "Linear Map",
								active: !isCircular
							},
							{
								id: "properties",
								name: "Properties",
								active: false
							}
						],
						[
							{
								id: "sequence",
								name: "Sequence Map",
								active: true
							}
						]
					]
				}

				this._editor.updateEditor(newEditorState);
			}

			resolve(this);
		});
	}

	/**
	 * Returns the OnChangeSubject instance that observes changes of this editor's
	 * sequence data.
	 */
	getOnChangeSubject() {
		return this._onChangeSubject;
	}

	/**
	 * Returns the internally used editor object.
	 */
	getEditorObj() {
		return this._editor;
	}
}