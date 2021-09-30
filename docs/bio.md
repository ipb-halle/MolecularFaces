# Biology and biochemistry

## Components

### [Teselagen's Open Source Vector Editor](https://github.com/TeselaGen/openVectorEditor) Component: `<mol:openVectorEditor>`

Sequence data is passed between the OpenVectorEditor (OVE) JavaScript plugin and the JSF backing bean as JSON object in the data model described by `sequenceData` in [OVE's editorState documentation](https://github.com/TeselaGen/openVectorEditor#editorstate). For security reasons, please consider sanitizing incoming and outgoing JSON data, for instance with a JSF converter based on [OWASP's JSON Sanitizer](https://github.com/OWASP/json-sanitizer).

Teselagen's Open Source Vector/Plasmid Editor Component is licensed under the [MIT License](https://github.com/TeselaGen/openVectorEditor/blob/master/LICENSE).

#### Supported attributes

* `converter` (java.faces.convert.Converter, no default): FacesConverter for the component
* `readonly` (boolean, default: false): sets the `readOnly` flag in the editor state; do not decode the submitted component value if set to true
* `widgetVar` (String, no default): client-side variable name of a Promise object that embeds the editor's JavaScript instance

#### Protein sequence mode

When starting the OVE with no sequence data, the user cannot switch from DNA/RNA sequence mode to protein sequence mode. This can be achieved by using the JSON string `OpenVectorEditorCore.EMPTY_PROTEIN_SEQUENCE_JSON` as the initial bean property.

#### Resource configuration (optional)

All resource dependencies (open-vector-editor.min.js and main.css) are included in the jar build pipeline. Note: Font files are not included for [license reasons](https://github.com/TeselaGen/openVectorEditor/issues/749#issue-947847772).

Context-params:
* `de.ipb_halle.molecularfaces.OPENVECTOREDITOR_BASE_URL` (optional): location of OVE's standalone (UMD) installation relative to the application's context root

#### Known issues

* Blueprint, a dependency of OVE, declares global CSS (see [OVE issue #604](https://github.com/TeselaGen/openVectorEditor/issues/604)). This will for instance override some CSS declarations of BootsFaces. No workaround available at the moment.