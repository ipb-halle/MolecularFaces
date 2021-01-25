## MolecularFaces

MolecularFaces is a collection of reusable UI components for Java Server Faces (JSF) featuring input and output plugins for chemistry and biochemistry. It is a spin-off project of [CRIMSy (Cloud Resource & Information Managment System)](https://github.com/ipb-halle/CRIMSy).

### Maven

The artifacts have not been submitted to Maven Central yet. To include MolecularFaces into your JSF project, run `mvn clean install` to install the artifact into your local Maven repository and include the following dependency into your project's pom.xml:

```xml
<dependency>
  <groupId>de.ipb-halle</groupId>
  <artifactId>molecularfaces</artifactId>
  <version>0.1-SNAPSHOT</version>
</dependency>
```
 
### Usage

#### XML Namespace

```xml
xmlns:mol="http://ipb-halle.de/nwc"
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

Plugin | Version | License | Editor | Viewer
------ | ------- | ------- | ------ | ------
[OpenChemLib JS](https://github.com/cheminfo/openchemlib-js) | [v7.2.3](https://github.com/cheminfo/openchemlib-js/releases/tag/v7.2.3) | BSD-3-Clause | [x] | [x]

##### Usage example

JSF view:

```xml
<mol:molecule value="#{myBean.structure}" pluginType="OpenChemLibJS" />
```

JSF backing bean:

```Java
@RequestScoped
public class MyBean implements Serializable {

// structure in MDL Molfile V2000 format
private String structure = "";

// getter/setter
...
}
```