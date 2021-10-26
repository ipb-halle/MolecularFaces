# Chemistry

## Components

### Chemical structure editor and viewer

Supported plugins:

Plugin | Version | License | Editor | Viewer | Molfile V2000 support | Molfile V3000 support | JSF Component
------ | ------- | ------- | ------ | ------ | --------------------- | --------------------- | ---------
[OpenChemLib JS](https://github.com/cheminfo/openchemlib-js) | [v7.4.1](https://github.com/cheminfo/openchemlib-js/releases/tag/v7.4.1) | [BSD-3-Clause](https://github.com/cheminfo/openchemlib-js/blob/master/LICENSE) | [x] | [x] | [x] | [x] | `<mol:openChemLibJSPlugin>`
[MolPaintJS](https://github.com/ipb-halle/MolPaintJS) | [v0.3.5-alpha](https://github.com/ipb-halle/MolPaintJS/releases/tag/v0.3.5-alpha) | [Apache License 2.0](https://github.com/ipb-halle/MolPaintJS/blob/master/LICENSE) | [x] | [x] | [x] | [x] | `<mol:molPaintJSPlugin>`
[Marvin JS](https://chemaxon.com/products/marvin-js) | 21.1.0 | proprietary | [x] | [x] | [x] | requires web services | `<mol:marvinJSPlugin>`

#### Supported attributes

* `border` (boolean, default: false): render with a border
* `converter` (java.faces.convert.Converter, no default): FacesConverter for the component
* `format` (String, default: "MDLV2000"): chemical file format used by the component; possible values: "MDLV2000" and "MDLV3000"
* `height` (int, default: 400): height of the plugin in pixels
* `readonly` (boolean, default: false): render in view-only mode or as structure editor; do not decode the submitted component value if set to true
* `widgetVar` (String, no default): client-side variable name of a Promise object that embeds the plugin's JavaScript instance
* `width` (int, default: 400): width of the plugin in pixels

#### Composite components `<mol:molecule>` and `<mol:moleculeRepeatable>`

In case you want to switch between the plugin types dynamically, you can use these two composite components. They pass all the attributes mentioned above to the chosen component.

##### Additional attribute: #####

* `pluginType` (String, default: "OpenChemLibJS"): type of the plugin; possible values: "OpenChemLibJS", "MolPaintJS" and "MarvinJS"

##### Use in iterations: #####

The component `<mol:molecule>` uses the JSTL tag `<c:if test="...">` internally for switching the plugin type. Thus, it shall [not be used inside iterating JSF components](https://stackoverflow.com/a/3343681) like `<h:dataTable>` or `<ui:repeat>` if they iterate the `pluginType` attribute. The functionally identical component `<mol:moleculeRepeatable>` is available for this case, which uses `<ui:fragment rendered="...">` internally. Note: This component adds all possible plugin components to the component tree (including JS and CSS resources rendered via JSF), but renders only one of them.

##### Known issues: #####

* Passthrough attributes added via the component attribute `pt:myattribute="value"` and the XML namespace `xmlns:pt="http://xmlns.jcp.org/jsf/passthrough"` are not applied to composite components. You can use [`<f:passThroughAttribute name="myattribute" value="value">`](https://docs.oracle.com/javaee/7/javaserver-faces-2-2/vdldocs-facelets/f/passThroughAttribute.html) instead.
* (Mojarra only, tested with Glassfish 5.0) When using other composite components on the same facelet page, you will receive an error like `javax.faces.view.facelets.FaceletException: components/molecule.xhtml @0,0 <> Cannot create composite component tag handler for composite-source element in taglib.xml file`.

#### Use in modals

Popular JSF component frameworks such as [BootsFaces](https://github.com/TheCoder4eu/BootsFaces-OSP) and [PrimeFaces](https://github.com/primefaces/primefaces) offer modal components, which can include MolecularFaces' plugin components. It might be necessary to execute the init() method of the JavaScript object provided via the `widgetVar` attribute to reinitialize the plugin.

Example using a BootsFaces modal:

```xml
<b:container>
  <b:form>
    <b:commandButton value="open modal" ajax="true" update="modalform" oncomplete="$('.modalPseudoClass').modal('show');editorInModal.then(e => e.init());" />
  </b:form>
</b:container>
<b:modal styleClass="modalPseudoClass">
  <b:form id="modalform">
    <mol:molecule id="editorInModal" value="#{testBean.structure}" pluginType="OpenChemLibJS" widgetVar="editorInModal" />
  </b:form>
</b:modal>
```

Known issues:
* The Marvin JS editor component drops an exception via a JavaScript alert() as soon as it initializes in a hidden modal - not very user friendly.

#### OpenChemLib JS

All resource dependencies (openchemlib-full.js) are included in the jar build pipeline.

Context-params:
* `de.ipb_halle.molecularfaces.OPENCHEMLIBJS_URL` (optional): location of openchemlib-full.js relative to the application's context root

#### MolPaintJS

All resource dependencies (molpaint.js) are included in the jar build pipeline.

Context-params:
* `de.ipb_halle.molecularfaces.MOLPAINTJS_URL` (optional): location of molpaint.js relative to the application's context root

#### Marvin JS

The resource dependencies are not part of the jar build pipeline. It is necessary to download the platform independent core archive (e.g. marvinjs-21.1.0-core.zip) from [ChemAxon's website](https://chemaxon.com/products/marvin-js/download), extract it and supply its location via a context-param in web.xml.

Marvin JS does not support import/export via the MDL Molfile V3000 format natively. This extended functionality requires the installation of [additional webservices](https://marvinjs-demo.chemaxon.com/latest/docs/dev/webservices.html). Note: This has not been tested extensively.

Context-params:
* `de.ipb_halle.molecularfaces.MARVINJS_BASE_URL` (required): location of the extracted Marvin JS core archive
* `de.ipb_halle.molecularfaces.MARVINJS_LICENSE_URL` (required): location of Marvin JS' license file (marvin4js-license.cxl) relative to `de.ipb_halle.molecularfaces.MARVINJS_BASE_URL`
* `de.ipb_halle.molecularfaces.MARVINJS_WEBSERVICES` (optional): if set to "true", use webservices (embeds editorws.html or loads webservices.js)

## Converters

The FacesConverters `molecularfaces.MDLV2000Converter` and `molecularfaces.MDLV3000Converter` may be used to convert Molfile V2000 and V3000 strings to [CDK's](https://cdk.github.io) [IAtomContainer](http://cdk.github.io/cdk/latest/docs/api/org/openscience/cdk/interfaces/IAtomContainer.html) objects. The converters use CDK's reader and writer classes for conversion.

## Bean validators

`@Molfile`: validate Molfiles
* parameters: `format` (Molfile version; possible values: `Format.V2000` or `Format.V3000`) and `mode` (strictness mode of the CDK reader used for validation; possible values: `Mode.RELAXED` or `Mode.STRICT`)