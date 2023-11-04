package com.thelocalmarketplace.software.test;

import com.jjjwelectronics.Numeral;
import com.jjjwelectronics.scale.ElectronicScale;
import com.jjjwelectronics.scale.ElectronicScaleListener;
import com.jjjwelectronics.scale.IElectronicScale;
import com.jjjwelectronics.scanner.Barcode;
import com.jjjwelectronics.scanner.BarcodeScanner;
import com.jjjwelectronics.scanner.BarcodeScannerListener;
import com.thelocalmarketplace.hardware.BarcodedProduct;
import com.thelocalmarketplace.hardware.external.ProductDatabases;
import com.thelocalmarketplace.software.AddItemViaBarcodeScan;

import ca.ucalgary.seng300.simulation.InvalidArgumentSimulationException;
import ca.ucalgary.seng300.simulation.NullPointerSimulationException;
import powerutility.NoPowerException;
import powerutility.PowerGrid;

import com.jjjwelectronics.IDevice;
import com.jjjwelectronics.IDeviceListener;
import com.jjjwelectronics.Mass;
import com.jjjwelectronics.scanner.IBarcodeScanner;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;


public class TestAddItemViaBarcodeScan {
	private Numeral num1 = Numeral.valueOf((byte)0);
	private Numeral num2 = Numeral.valueOf((byte)1);
	private Numeral[] numArray1 = new Numeral[]{num1, num2};
	private Numeral[] numArray2 = new Numeral[]{num2, num1};
	private Barcode barcode1 = new Barcode(numArray1);
	private Barcode barcode2 = new Barcode(numArray2);
	private String description1 = "4L milk jug";
	private String description2 = "Chocolate cake";
	private long price1 = 12;
	private long price2 = 55;
	private double expectedWeightInGrams1 = 10.00;
	private double expectedWeightInGrams2 = 5.42;
	private BarcodedProduct product1 = new BarcodedProduct(barcode1, description1, price1, expectedWeightInGrams1);
	private BarcodedProduct product2 = new BarcodedProduct(barcode2, description2, price2, expectedWeightInGrams2);
	
	private BarcodeScanner scanner;
	private ElectronicScale scale;
	
	private BarcodeScannerListenerStub scannerStub1;
	private BarcodeScannerListenerStub scannerStub2;
	private BarcodeScannerListenerStub scannerStub3;
	
	private ElectronicScaleListenerStub scaleStub1;
	private ElectronicScaleListenerStub scaleStub2;
	private ElectronicScaleListenerStub scaleStub3;
	
	private AddItemViaBarcodeScan testAddItemClass;
	
	@Before
	public void setup() {
		testAddItemClass = new AddItemViaBarcodeScan();
		
		ProductDatabases.BARCODED_PRODUCT_DATABASE.put(barcode1, product1);
		ProductDatabases.BARCODED_PRODUCT_DATABASE.put(barcode2, product2);
		
		scanner = new BarcodeScanner();
		scale = new ElectronicScale();
		
		PowerGrid.engageUninterruptiblePowerSource();
		scanner.plugIn(PowerGrid.instance());
		scale.plugIn(PowerGrid.instance());
		scanner.turnOn();
		scale.turnOn();
		
		// Create scanner listener stubs
		scannerStub1 = new BarcodeScannerListenerStub();
		scannerStub2 = new BarcodeScannerListenerStub();
		scannerStub3 = new BarcodeScannerListenerStub();
		scanner.register(scannerStub1);
		scanner.register(scannerStub2);
		scanner.register(scannerStub3);
		
		// Create scale listener stubs
		scaleStub1 = new ElectronicScaleListenerStub();
		scaleStub2 = new ElectronicScaleListenerStub();
		scaleStub3 = new ElectronicScaleListenerStub();
		scale.register(scaleStub1);
		scale.register(scaleStub2);
		scale.register(scaleStub3);
		
		testAddItemClass.setSessionActiveStatus(true);
		testAddItemClass.setSessionBlockStatus(false);
	}
	
	// Tests
	@Test
	public void testScanningOneItem() {
		testAddItemClass.addItemViaScanning(product1, scanner, scale);
		assertEquals(scannerStub1.numItemScanned, 1);
		assertEquals(scannerStub3.numItemScanned, 1);
		Mass product1Mass = new Mass(product1.getExpectedWeight());
		assertEquals(scaleStub1.massOnScale, product1Mass);
		assertEquals(scaleStub2.massOnScale, product1Mass);
	}

	@Test
	public void testScanningMultipleItem() {
		testAddItemClass.addItemViaScanning(product1, scanner, scale);
		testAddItemClass.addItemViaScanning(product2, scanner, scale);
		assertEquals(scannerStub2.numItemScanned, 2);
		assertEquals(scannerStub3.numItemScanned, 2);
		Mass product1Mass = new Mass(product1.getExpectedWeight());
		Mass product2Mass = new Mass(product2.getExpectedWeight());
		Mass sumMass = product1Mass.sum(product2Mass);
		assertEquals(scaleStub2.massOnScale, sumMass);
		assertEquals(scaleStub3.massOnScale, sumMass);
	}
	
	@Test
	public void testScanningSameItemMultipleTimes() {
		testAddItemClass.addItemViaScanning(product1, scanner, scale);
		testAddItemClass.addItemViaScanning(product1, scanner, scale);
		testAddItemClass.addItemViaScanning(product1, scanner, scale);
		assertEquals(scannerStub2.numItemScanned, 3);
		assertEquals(scannerStub3.numItemScanned, 3);
		Mass product1Mass = new Mass(product1.getExpectedWeight());
		Mass sumMass = product1Mass.sum(product1Mass).sum(product1Mass);
		assertEquals(scaleStub2.massOnScale, sumMass);
		assertEquals(scaleStub3.massOnScale, sumMass);
	}
	
	@Test (expected = NoPowerException.class)
	public void testScannerNotPowered() {
		scanner.turnOff();
		testAddItemClass.addItemViaScanning(product1, scanner, scale);
	}
	
	@Test (expected = NoPowerException.class)
	public void testScaleNotPowered() {
		scale.turnOff();
		testAddItemClass.addItemViaScanning(product1, scanner, scale);
	}
	
	@Test (expected = NullPointerSimulationException.class)
	public void testNullProduct() {
		testAddItemClass.addItemViaScanning(null, scanner, scale);
	}
	
	@Test
	public void testSessionBlocked() {
		testAddItemClass.setSessionBlockStatus(true);
		testAddItemClass.addItemViaScanning(product1, scanner, scale);
		testAddItemClass.addItemViaScanning(product2, scanner, scale);
		assertEquals(scannerStub2.numItemScanned, 0);
		assertEquals(scannerStub3.numItemScanned, 0);
		assertEquals(scaleStub2.massOnScale, Mass.ZERO);
		assertEquals(scaleStub3.massOnScale, Mass.ZERO);
	}
	
	@Test
	public void testSessionNotActive() {
		testAddItemClass.setSessionActiveStatus(false);
		testAddItemClass.addItemViaScanning(product1, scanner, scale);
		testAddItemClass.addItemViaScanning(product2, scanner, scale);
		assertEquals(scannerStub2.numItemScanned, 0);
		assertEquals(scannerStub3.numItemScanned, 0);
		assertEquals(scaleStub2.massOnScale, Mass.ZERO);
		assertEquals(scaleStub3.massOnScale, Mass.ZERO);
	}
	
	@Test (expected = InvalidArgumentSimulationException.class)
	public void testScanningProductNotInDatabase() {
		Numeral[] numArray3 = new Numeral[]{num2, num1, num2};
		Barcode barcode3 = new Barcode(numArray3);
		BarcodedProduct product3 = new BarcodedProduct(barcode3, description1, price1, expectedWeightInGrams1);
		testAddItemClass.addItemViaScanning(product3, scanner, scale);
	}
	
	@Test
	public void testGettingScannedItemPrice() {
		testAddItemClass.addItemViaScanning(product1, scanner, scale);
		assertEquals(testAddItemClass.getAddedProductPrice(), product1.getPrice());
		testAddItemClass.addItemViaScanning(product2, scanner, scale);
		assertEquals(testAddItemClass.getAddedProductPrice(), product2.getPrice());
	}
	
	@Test
	public void testGettingScannedItemWeight() {
		testAddItemClass.addItemViaScanning(product1, scanner, scale);
		assertEquals(testAddItemClass.getAddedProductWeight(), product1.getExpectedWeight(), 0.0001);
		testAddItemClass.addItemViaScanning(product2, scanner, scale);
		assertEquals(testAddItemClass.getAddedProductWeight(), product2.getExpectedWeight(), 0.0001);
	}
	
	
	
	// Stub implementation
	public class BarcodeScannerListenerStub implements BarcodeScannerListener {
		public boolean enabled;
		public boolean turnedOn;
		public int numItemScanned = 0;

		@Override
		public void aDeviceHasBeenEnabled(IDevice<? extends IDeviceListener> device) {
			enabled = true;
		}

		@Override
		public void aDeviceHasBeenDisabled(IDevice<? extends IDeviceListener> device) {
			enabled = false;
		}

		@Override
		public void aDeviceHasBeenTurnedOn(IDevice<? extends IDeviceListener> device) {
			turnedOn = true;
		}

		@Override
		public void aDeviceHasBeenTurnedOff(IDevice<? extends IDeviceListener> device) {
			turnedOn = false;
		}

		@Override
		public void aBarcodeHasBeenScanned(IBarcodeScanner barcodeScanner, Barcode barcode) {
			numItemScanned = numItemScanned + 1; 
		}
	}
	
	
	
	public class ElectronicScaleListenerStub implements ElectronicScaleListener {
		public boolean enabled;
		public boolean turnedOn;
		public Mass massOnScale = Mass.ZERO;
		public boolean massExceeded;
		
		@Override
		public void aDeviceHasBeenEnabled(IDevice<? extends IDeviceListener> device) {
			enabled = true;
		}

		@Override
		public void aDeviceHasBeenDisabled(IDevice<? extends IDeviceListener> device) {
			enabled = false;
		}

		@Override
		public void aDeviceHasBeenTurnedOn(IDevice<? extends IDeviceListener> device) {
			turnedOn = true;
		}

		@Override
		public void aDeviceHasBeenTurnedOff(IDevice<? extends IDeviceListener> device) {
			turnedOn = false;
		}

		@Override
		public void theMassOnTheScaleHasChanged(IElectronicScale scale, Mass mass) {
			massOnScale = mass;
		}

		@Override
		public void theMassOnTheScaleHasExceededItsLimit(IElectronicScale scale) {
			massExceeded = true;
		}

		@Override
		public void theMassOnTheScaleNoLongerExceedsItsLimit(IElectronicScale scale) {
			massExceeded = false;
		}
		
	}
}
