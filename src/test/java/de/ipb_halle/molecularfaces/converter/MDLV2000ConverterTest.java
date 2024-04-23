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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;

import org.junit.Test;
import org.openscience.cdk.Atom;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.Bond;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond.Order;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

/**
 * 
 * @author flange
 */
public class MDLV2000ConverterTest {
	private FacesContext context = null;
	private UIComponent component = null;

	private Converter converter = new MDLV2000Converter();

	@Test
	public void testGetAsObjectReturnsNull() {
		assertNull(converter.getAsObject(context, component, ""));
	}

	@Test
	public void testGetAsObjectThrowsConverterException() {
		assertThrows(ConverterException.class, () -> converter.getAsObject(context, component, "a")).getMessage();
	}

	@Test
	public void testGetAsObjectWithValidMolfile() {
		String benzene = "\n" + "Actelion Java MolfileCreator 1.0\n" + "\n"
				+ "  6  6  0  0  0  0  0  0  0  0999 V2000\n"
				+ "    5.9375  -10.0000   -0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
				+ "    5.9375  -11.5000   -0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
				+ "    7.2365  -12.2500   -0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
				+ "    8.5356  -11.5000   -0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
				+ "    8.5356  -10.0000   -0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n"
				+ "    7.2365   -9.2500   -0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0\n" + "  1  2  2  0  0  0  0\n"
				+ "  2  3  1  0  0  0  0\n" + "  3  4  2  0  0  0  0\n" + "  4  5  1  0  0  0  0\n"
				+ "  5  6  2  0  0  0  0\n" + "  6  1  1  0  0  0  0\n" + "M  END";

		IAtomContainer mol = (IAtomContainer) converter.getAsObject(context, component, benzene);
		assertNotNull(mol);

		// hydrogens are implicit
		assertEquals(6, mol.getAtomCount());
		assertEquals(6, mol.getBondCount());

		IMolecularFormula formula = MolecularFormulaManipulator.getMolecularFormula(mol);
		assertEquals("C6H6", MolecularFormulaManipulator.getString(formula));
		assertEquals(78.11, MolecularFormulaManipulator.getMass(formula), 0.01);
	}

	@Test
	public void testGetAsStringReturnsEmptyString() {
		assertEquals("", converter.getAsString(context, component, null));
		assertEquals("", converter.getAsString(context, component, new Object()));
	}

	@Test
	public void testGetAsStringThrowsConverterException() {
		/*
		 * From CDK's testUnsupportedBondOrder() in MDLV2000WriterTest, see
		 * https://github.com/cdk/cdk/blob/5eebd3a58604b7fd0fad9ccd6681e1cdbcae845c/
		 * storage/ctab/src/test/java/org/openscience/cdk/io/MDLV2000WriterTest.java#
		 * L320
		 */
		IAtomContainer molecule = new AtomContainer();
		molecule.addAtom(new Atom("C"));
		molecule.addAtom(new Atom("C"));
		molecule.addBond(new Bond(molecule.getAtom(0), molecule.getAtom(1), Order.QUADRUPLE));

		assertThrows(ConverterException.class, () -> converter.getAsString(context, component, molecule));
	}

	@Test
	public void testOutAndIn() {
		IAtomContainer benzene = new AtomContainer();
		for (int i = 0; i < 6; i++) {
			benzene.addAtom(new Atom("C"));
		}
		benzene.addBond(new Bond(benzene.getAtom(0), benzene.getAtom(1), Order.SINGLE));
		benzene.addBond(new Bond(benzene.getAtom(1), benzene.getAtom(2), Order.DOUBLE));
		benzene.addBond(new Bond(benzene.getAtom(2), benzene.getAtom(3), Order.SINGLE));
		benzene.addBond(new Bond(benzene.getAtom(3), benzene.getAtom(4), Order.DOUBLE));
		benzene.addBond(new Bond(benzene.getAtom(4), benzene.getAtom(5), Order.SINGLE));
		benzene.addBond(new Bond(benzene.getAtom(5), benzene.getAtom(0), Order.DOUBLE));

		String molfile = converter.getAsString(context, component, benzene);
		IAtomContainer mol = (IAtomContainer) converter.getAsObject(context, component, molfile);

		assertEquals(6, mol.getAtomCount());
		assertEquals(6, mol.getBondCount());

		IMolecularFormula formula = MolecularFormulaManipulator.getMolecularFormula(mol);
		assertEquals("C6H6", MolecularFormulaManipulator.getString(formula));
		assertEquals(78.11, MolecularFormulaManipulator.getMass(formula), 0.01);
	}
}
