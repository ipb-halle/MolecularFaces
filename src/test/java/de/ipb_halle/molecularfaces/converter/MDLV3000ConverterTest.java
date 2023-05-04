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
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond.Order;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

/**
 * 
 * @author flange
 */
public class MDLV3000ConverterTest {
	private FacesContext context = null;
	private UIComponent component = null;

	private Converter converter = new MDLV3000Converter();

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
		String benzene = "\n" + "Actelion Java MolfileCreator 2.0\n" + "\n"
				+ "  0  0  0  0  0  0              0 V3000\n" + "M  V30 BEGIN CTAB\n" + "M  V30 COUNTS 6 6 0 0 0\n"
				+ "M  V30 BEGIN ATOM\n" + "M  V30 1 C 11.5625 -11.5 0 0\n" + "M  V30 2 C 11.5625 -13 0 0\n"
				+ "M  V30 3 C 12.8615 -13.75 0 0\n" + "M  V30 4 C 14.1605 -13 0 0\n" + "M  V30 5 C 14.1605 -11.5 0 0\n"
				+ "M  V30 6 C 12.8615 -10.75 0 0\n" + "M  V30 END ATOM\n" + "M  V30 BEGIN BOND\n" + "M  V30 1 2 1 2\n"
				+ "M  V30 2 1 2 3\n" + "M  V30 3 2 3 4\n" + "M  V30 4 1 4 5\n" + "M  V30 5 2 5 6\n" + "M  V30 6 1 6 1\n"
				+ "M  V30 END BOND\n" + "M  V30 END CTAB\n" + "M  END";

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
	public void testOutAndIn() throws CDKException {
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

		/*
		 * We need to take care for hydrogens or all carbons will have VAL=3 in the
		 * molfile and the test for the molecular formula will fail. Thanks to fbroda
		 * for pointing out this solution.
		 */
		AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(benzene);
		IChemObjectBuilder builder = DefaultChemObjectBuilder.getInstance();
		CDKHydrogenAdder.getInstance(builder).addImplicitHydrogens(benzene);
		// Kekulization.kekulize(benzene);

		String molfile = converter.getAsString(context, component, benzene);
		IAtomContainer mol = (IAtomContainer) converter.getAsObject(context, component, molfile);

		assertEquals(6, mol.getAtomCount());
		assertEquals(6, mol.getBondCount());

		IMolecularFormula formula = MolecularFormulaManipulator.getMolecularFormula(mol);
		assertEquals("C6H6", MolecularFormulaManipulator.getString(formula));
		assertEquals(78.11, MolecularFormulaManipulator.getMass(formula), 0.01);
	}
}
