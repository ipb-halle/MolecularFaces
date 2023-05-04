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
package de.ipb_halle.molecularfaces.converter;

import java.io.StringReader;
import java.io.StringWriter;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;
import org.openscience.cdk.io.MDLV2000Writer;

/**
 * This {@link FacesConverter} converts MDL Molfile V2000 strings to CDK's
 * {@link IAtomContainer} objects and vice versa.
 * 
 * @author flange
 */
@FacesConverter("molecularfaces.MDLV2000Converter")
public class MDLV2000Converter implements Converter {
	/**
	 * Converts a MDL Molfile V2000 string to an {@link IAtomContainer}.
	 * 
	 * @return valid {@link IAtomContainer} object or {@code null} if {@code value}
	 *         is {@code null} or empty.
	 * @throws ConverterException if reading of the Molfile fails
	 */
	@Override
	public IAtomContainer getAsObject(FacesContext context, UIComponent component, String value)
			throws ConverterException {
		if (value == null || value.isEmpty()) {
			return null;
		}

		try (MDLV2000Reader reader = new MDLV2000Reader(new StringReader(value))) {
			return reader.read(new AtomContainer());
		} catch (Exception e) {
			throw new ConverterException(new FacesMessage(e.getMessage()), e);
		}
	}

	/**
	 * Converts an {@link IAtomContainer} to a MDL Molfile V2000 string.
	 * 
	 * @return valid MDL Molfile V2000 or empty string if {@code value} is
	 *         {@code null} or not an {@link IAtomContainer}
	 * @throws ConverterException if writing of the Molfile fails
	 */
	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) throws ConverterException {
		if ((value == null) || !(value instanceof IAtomContainer)) {
			return "";
		}
		IAtomContainer mol = (IAtomContainer) value;

		StringWriter writer = new StringWriter();
		try (MDLV2000Writer molWriter = new MDLV2000Writer(writer)) {
			molWriter.write(mol);
		} catch (Exception e) {
			throw new ConverterException(new FacesMessage(e.getMessage()), e);
		}

		return writer.toString();
	}
}
