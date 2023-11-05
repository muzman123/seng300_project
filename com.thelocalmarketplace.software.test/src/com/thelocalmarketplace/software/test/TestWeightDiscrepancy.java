//Names & UCID
//Arvin Bolbolanardestani 30165484
//Zeyad Elrayes 30161958
//Dvij Raval 30024340
//Muzammil Saleem 30180889
//Ryan Wong 30171793
//Danish Sharma 30172600

package com.thelocalmarketplace.software.test;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.jjjwelectronics.Mass;
import com.jjjwelectronics.Numeral;
import com.jjjwelectronics.scale.ElectronicScale;
import com.jjjwelectronics.scanner.Barcode;
import com.jjjwelectronics.scanner.BarcodedItem;
import com.thelocalmarketplace.software.WeightDiscrepancy;

import powerutility.PowerGrid;

public class TestWeightDiscrepancy {
	private ElectronicScale scale;
	private WeightDiscrepancy weightErrorDetector;
	
	private Numeral num1 = Numeral.valueOf((byte)0);
	private Numeral num2 = Numeral.valueOf((byte)1);
	private Numeral[] numArray1 = new Numeral[]{num1, num2};
	private Numeral[] numArray2 = new Numeral[]{num2, num1};
	private Barcode barcode1 = new Barcode(numArray1);
	private Barcode barcode2 = new Barcode(numArray2);
	Mass itemMass1 = new Mass(100000);
	Mass itemMass2 = new Mass(1000000);
	private BarcodedItem testBarcodedItem1 = new BarcodedItem(barcode1, itemMass1);
	private BarcodedItem testBarcodedItem2 = new BarcodedItem(barcode2, itemMass2);
	
	@Before
	public void setup() {
		scale = new ElectronicScale();
		// Power up the scanner and scale
		PowerGrid.engageUninterruptiblePowerSource();
		scale.plugIn(PowerGrid.instance());
		scale.turnOn();
		scale.enable();
		
		// Create the detector instance
		weightErrorDetector = new WeightDiscrepancy(scale);
	}
	
	@Test
	public void testAddItemToExpectedOrder() {
		weightErrorDetector.addItemToOrder(testBarcodedItem1);
		assertEquals(weightErrorDetector.calculateTotalMassExpectedOnScale(), itemMass1);
	}

	@Test
	public void testAddItemDoesNotCreateFalseDiscrepancy() {
		weightErrorDetector.addItemToOrder(testBarcodedItem1);
		assertEquals(weightErrorDetector.discrepancyStatus(), false);
	}
	
	@Test
	public void testCreatingDiscrepancy() {
		weightErrorDetector.addItemToOrder(testBarcodedItem1);
		scale.addAnItem(testBarcodedItem2);
		assertEquals(weightErrorDetector.discrepancyStatus(), true);
	}
	
	@Test
	public void testCorrectingDiscrepancy() {
		weightErrorDetector.addItemToOrder(testBarcodedItem1);
		scale.addAnItem(testBarcodedItem2);
		assertEquals(weightErrorDetector.discrepancyStatus(), true);
		scale.removeAnItem(testBarcodedItem2);
		scale.addAnItem(testBarcodedItem1);
		assertEquals(weightErrorDetector.calculateTotalMassExpectedOnScale(), itemMass1);
		assertEquals(weightErrorDetector.discrepancyStatus(), false);
	}
	
	@Test
	public void testExceedingScaleLimit() {
		Mass itemMass3 = new Mass(100000000000000.00);
		BarcodedItem testBarcodedItem3 = new BarcodedItem(barcode1, itemMass3);
		weightErrorDetector.addItemToOrder(testBarcodedItem3);
		scale.addAnItem(testBarcodedItem3);
		assertEquals(weightErrorDetector.overloaded, true);
	}
	
	@Test
	public void testRecoveringFromExceedingScaleLimit() {
		Mass itemMass3 = new Mass(100000000000000.00);
		BarcodedItem testBarcodedItem3 = new BarcodedItem(barcode1, itemMass3);
		weightErrorDetector.addItemToOrder(testBarcodedItem3);
		scale.addAnItem(testBarcodedItem3);
		assertEquals(weightErrorDetector.overloaded, true);
		scale.removeAnItem(testBarcodedItem3);
		assertEquals(weightErrorDetector.overloaded, false);
	}
}
