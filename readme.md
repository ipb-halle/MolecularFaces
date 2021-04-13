## MolecularFaces

MolecularFaces is a collection of reusable UI components for Java Server Faces (JSF) featuring input and output plugins for chemistry and biochemistry. It is a spin-off project of [CRIMSy (Cloud Resource & Information Managment System)](https://github.com/ipb-halle/CRIMSy).

### Security warning

Versions 0.2.0 and 0.1.0 contain a serious security flaw in the `<mol:molecule>` component, which allows JavaScript code execution in the client browser via malicious molfiles.

### Maven

The artifacts are available on Maven Central. To include MolecularFaces into your JSF project, include the following dependency into your project's pom.xml:

```xml
<dependency>
  <groupId>de.ipb-halle</groupId>
  <artifactId>molecularfaces</artifactId>
  <version>0.3.0</version>
</dependency>
```
 
### Usage

#### XML Namespace

```xml
xmlns:mol="http://github.com/ipb-halle/MolecularFaces"
```

#### Configuring resources via web.xml

MolecularFaces tries to include as much resources (JavaScript, CSS, images) of the third-party plugins in its jar as possible and loads them when required. Alternatively, you can configure the URL of the resources (relative to the application's context root) via a `context-param` in your project's web.xml.

Example:

```xml
<context-param>
  <param-name>de.ipb-halle.molecularfaces.OPENCHEMLIBJS_URL</param-name>
  <param-value>openchemlib-full.js</param-value>
</context-param>
```

### Components

#### Chemical structure editor and viewer

##### Supported plugins

Plugin | Version | License | Editor | Viewer | Molfile V2000 support | Molfile V3000 support
------ | ------- | ------- | ------ | ------ | --------------------- | ---------------------
[OpenChemLib JS](https://github.com/cheminfo/openchemlib-js) | [v7.3.0](https://github.com/cheminfo/openchemlib-js/releases/tag/v7.3.0) | [BSD-3-Clause](https://github.com/cheminfo/openchemlib-js/blob/master/LICENSE) | [x] | [x] | [x] | [x]
[MolPaintJS](https://github.com/ipb-halle/MolPaintJS) | [v0.3.5-alpha](https://github.com/ipb-halle/MolPaintJS/releases/tag/v0.3.5-alpha) | [Apache License 2.0](https://github.com/ipb-halle/MolPaintJS/blob/master/LICENSE) | [x] | [x] | [x] | [x]
[Marvin JS](https://chemaxon.com/products/marvin-js) | 21.1.0 | proprietary | [x] | [x] | [x] | requires web services

##### Usage example

JSF view:

```xml
<?xml version='1.0' encoding='UTF-8' ?>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
  xmlns:h="http://xmlns.jcp.org/jsf/html"
  xmlns:mol="http://github.com/ipb-halle/MolecularFaces">
  <h:head>
    <title>MolecularFaces</title>
  </h:head>
  <h:body>
    <h:form>
      <mol:molecule value="#{myBean.structure}" pluginType="OpenChemLibJS" />
    </h:form>
  </h:body>
</html>
```

JSF backing bean:

```Java
@Named
@RequestScoped
public class MyBean implements Serializable {

// structure in MDL Molfile V2000 format
private String structure = "";

// getter/setter
...
}
```

##### Use in iterations

The component `<mol:molecule>` uses the JSTL tag `<c:if test="...">` internally for switching the plugin type. Thus, it shall [not be used inside iterating JSF components](https://stackoverflow.com/a/3343681) like `<h:dataTable>` or `<ui:repeat>` if they iterate the `pluginType` attribute. The functionally identical component `<mol:moleculeRepeatable>` is available for this case, which uses `<ui:fragment rendered="...">` internally. Note: This component adds all possible plugins to the component tree (including JS and CSS resources), but renders only one of them.

##### Use in modals
Popular JSF component frameworks like [BootsFaces](https://github.com/TheCoder4eu/BootsFaces-OSP) and [PrimeFaces](https://github.com/primefaces/primefaces) offer modal components, which can include MolecularFaces plugin components. It might be necessary to execute the init() method of the JavaScript object provided via the `widgetVar` attribute to reinitialize the plugin.

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

##### OpenChemLib JS

All resource dependencies (openchemlib-full.js) are included in the jar build pipeline.

Context-params:
* `de.ipb_halle.molecularfaces.OPENCHEMLIBJS_URL` (optional): location of openchemlib-full.js relative to the application's context root

##### MolPaintJS

All resource dependencies (molpaint.js) are included in the jar build pipeline.

Context-params:
* `de.ipb_halle.molecularfaces.MOLPAINTJS_URL` (optional): location of molpaint.js relative to the application's context root

##### Marvin JS

The resource dependencies are not part of the jar build pipeline. It is necessary to download the platform independent core archive (e.g. marvinjs-21.1.0-core.zip) from [ChemAxon's website](https://chemaxon.com/products/marvin-js/download), extract it and supply its location via a context-param in web.xml.

Context-params:
* `de.ipb_halle.molecularfaces.MARVINJS_BASE_URL` (required): location of the extracted Marvin JS core archive
* `de.ipb_halle.molecularfaces.MARVINJS_LICENSE_URL` (required): location of Marvin JS' license file (marvin4js-license.cxl) relative to `de.ipb_halle.molecularfaces.MARVINJS_BASE_URL`
* `de.ipb_halle.molecularfaces.MARVINJS_WEBSERVICES` (optional): if set to "true", use JChem Webservice (embeds editorws.html)
