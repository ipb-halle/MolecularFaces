# MolecularFaces

MolecularFaces is a collection of reusable UI components for Java Server Faces (JSF) featuring input and output plugins, validators and converters for chemistry and biochemistry data. It is a spin-off project of [CRIMSy (Cloud Resource & Information Management System)](https://github.com/ipb-halle/CRIMSy).

## [UI library for chemistry](docs/chemistry.md)

## [UI library for biology and biochemistry](docs/bio.md)

## Maven coordinates

The artifacts are available on Maven Central. To include MolecularFaces into your JSF project, include the following dependency into your project's pom.xml:

```xml
<dependency>
  <groupId>de.ipb-halle</groupId>
  <artifactId>molecularfaces</artifactId>
  <version>0.3.0</version>
</dependency>
```

Snapshots can be included via

```xml
<repository>
  <id>snapshots-repo</id>
  <url>https://s01.oss.sonatype.org/content/repositories/snapshots/</url>
  <releases><enabled>false</enabled></releases>
  <snapshots><enabled>true</enabled></snapshots>
</repository>
...
<dependency>
  <groupId>de.ipb-halle</groupId>
  <artifactId>molecularfaces</artifactId>
  <version>0.4.0-SNAPSHOT</version>
</dependency>
```

## Usage

### XML Namespace

```xml
xmlns:mol="http://github.com/ipb-halle/MolecularFaces"
```

### Usage example

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
      <br />
      <mol:openVectorEditor value="#{myBean.sequence}" />
    </h:form>
  </h:body>
</html>
```

JSF backing bean:

```Java
@Named
@RequestScoped
public class MyBean {

// structure in MDL Molfile V2000 format
private String structure = "";

// DNA/RNA sequence in JSON format
private String sequence = "";

// getters/setters
...
}
```

### Configuring resources via web.xml

MolecularFaces tries to include as much resources (JavaScript, CSS, images) of the third-party plugins in its jar as possible and loads them when required. Alternatively, you can configure the URL of the resources (relative to the application's context root) via a `context-param` in your project's web.xml.

Example:

```xml
<context-param>
  <param-name>de.ipb-halle.molecularfaces.OPENCHEMLIBJS_URL</param-name>
  <param-value>openchemlib-full.js</param-value>
</context-param>
```