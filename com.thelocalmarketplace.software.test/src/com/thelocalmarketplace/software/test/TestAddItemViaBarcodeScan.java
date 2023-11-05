//Names & UCID
//Arvin Bolbolanardestani 30165484
//Zeyad Elrayes 30161958
//Dvij Raval 30024340
//Muzammil Saleem 30180889
//Ryan Wong 30171793
//Danish Sharma 30172600

package com.thelocalmarketplace.software.test;

import com.jjjwelectronics.Numeral;
import com.jjjwelectronics.OverloadedDevice;
import com.jjjwelectronics.scale.ElectronicScale;
import com.jjjwelectronics.scale.ElectronicScaleListener;
import com.jjjwelectronics.scale.IElectronicScale;
import com.jjjwelectronics.scanner.Barcode;
import com.jjjwelectronics.scanner.BarcodeScanner;
import com.jjjwelectronics.scanner.BarcodeScannerListener;
import com.jjjwelectronics.scanner.BarcodedItem;
import com.thelocalmarketplace.hardware.BarcodedProduct;
import com.thelocalmarketplace.hardware.external.ProductDatabases;
import com.thelocalmarketplace.software.AddItemViaBarcodeScan;

import ca.ucalgary.seng300.simulation.InvalidArgumentSimulationException;
import ca.ucalgary.seng300.simulation.InvalidStateSimulationException;
import ca.ucalgary.seng300.simulation.NullPointerSimulationException;
import powerutility.NoPowerException;
import powerutility.PowerGrid;

import com.jjjwelectronics.DisabledDevice;
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
	private double expectedWeightInGrams1 = 100.00;
	private double expectedWeightInGrams2 = 500.42;
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
		ProductDatabases.BARCODED_PRODUCT_DATABASE.put(barcode1, product1);
		ProductDatabases.BARCODED_PRODUCT_DATABASE.put(barcode2, product2);
		
		scanner = new BarcodeScanner();
		scale = new ElectronicScale();
		
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
		
		// Create the testing instance of the class
		testAddItemClass = new AddItemViaBarcodeScan(scanner, scale);

		// Power up the scanner and scale
		PowerGrid.engageUninterruptiblePowerSource();
		scanner.plugIn(PowerGrid.instance());
		scale.plugIn(PowerGrid.instance());
		scanner.turnOn();
		scale.turnOn();
		scanner.enable();
		scale.enable();
	}
	
	// Tests
	@Test
	public void testScanningOneItem() throws OverloadedDevice, DisabledDevice {
		testAddItemClass.addItemViaScanning(product1);
		assertEquals(scannerStub1.numItemScanned, 1);
		assertEquals(scannerStub3.numItemScanned, 1);
		Mass product1Mass = new Mass(product1.getExpectedWeight());
		assertEquals(scaleStub1.massOnScale, product1Mass);
		assertEquals(scaleStub2.massOnScale, product1Mass);
	}
	
	@Test
	public void testScanningMultipleItem() throws OverloadedDevice, DisabledDevice {
		testAddItemClass.addItemViaScanning(product1);
		testAddItemClass.addItemViaScanning(product2);
		assertEquals(scannerStub2.numItemScanned, 2);
		assertEquals(scannerStub3.numItemScanned, 2);
		Mass product1Mass = new Mass(product1.getExpectedWeight());
		Mass product2Mass = new Mass(product2.getExpectedWeight());
		Mass sumMass = product1Mass.sum(product2Mass);
		assertEquals(scaleStub2.massOnScale, sumMass);
		assertEquals(scaleStub3.massOnScale, sumMass);
	}
	
	@Test
	public void testScanningSameItemMultipleTimes() throws OverloadedDevice, DisabledDevice {
		testAddItemClass.addItemViaScanning(product1);
		testAddItemClass.addItemViaScanning(product1);
		testAddItemClass.addItemViaScanning(product1);
		assertEquals(scannerStub2.numItemScanned, 3);
		assertEquals(scannerStub3.numItemScanned, 3);
		Mass product1Mass = new Mass(product1.getExpectedWeight());
		Mass sumMass = product1Mass.sum(product1Mass).sum(product1Mass);
		assertEquals(scaleStub2.massOnScale, sumMass);
		assertEquals(scaleStub3.massOnScale, sumMass);
	}
	
	@Test (expected = NoPowerException.class)
	public void testScannerNotPowered() throws OverloadedDevice, DisabledDevice {
		scanner.turnOff();
		testAddItemClass.addItemViaScanning(product1);
	}
	
	@Test (expected = NoPowerException.class)
	public void testScaleNotPowered() throws OverloadedDevice, DisabledDevice {
		scale.turnOff();
		testAddItemClass.addItemViaScanning(product1);
	}
	
	@Test (expected = DisabledDevice.class)
	public void testScannerDisabled() throws OverloadedDevice, DisabledDevice {
		scanner.disable();
		testAddItemClass.addItemViaScanning(product1);
	}
	
	@Test (expected = DisabledDevice.class)
	public void testScaleDisabled() throws OverloadedDevice, DisabledDevice {
		scale.disable();
		testAddItemClass.addItemViaScanning(product1);
	}
	
	@Test (expected = NullPointerSimulationException.class)
	public void testNullProduct() throws OverloadedDevice, DisabledDevice {
		testAddItemClass.addItemViaScanning(null);
	}
	
	
	@Test (expected = InvalidArgumentSimulationException.class)
	public void testScanningProductNotInDatabase() throws OverloadedDevice, DisabledDevice {
		Numeral[] numArray3 = new Numeral[]{num2, num1, num2};
		Barcode barcode3 = new Barcode(numArray3);
		BarcodedProduct product3 = new BarcodedProduct(barcode3, description1, price1, expectedWeightInGrams1);
		testAddItemClass.addItemViaScanning(product3);
	}
	
	@Test
	public void testGettingScannedItemPrice() throws OverloadedDevice, DisabledDevice {
		testAddItemClass.addItemViaScanning(product1);
		assertEquals(testAddItemClass.getAddedProductPrice(), product1.getPrice());
		testAddItemClass.addItemViaScanning(product2);
		assertEquals(testAddItemClass.getAddedProductPrice(), product2.getPrice());
	}
	
	@Test
	public void testGettingScannedItemWeight() throws OverloadedDevice, DisabledDevice {
		testAddItemClass.addItemViaScanning(product1);
		assertEquals(testAddItemClass.getAddedProductWeight(), product1.getExpectedWeight(), 0.0001);
		testAddItemClass.addItemViaScanning(product2);
		assertEquals(testAddItemClass.getAddedProductWeight(), product2.getExpectedWeight(), 0.0001);
	}
	
	@Test (expected = OverloadedDevice.class)
	public void testScanningItemBeyondOverloadWeight() throws OverloadedDevice, DisabledDevice {
		Numeral[] numArray5 = new Numeral[]{num2, num1, num2, num2, num1};
		Barcode barcode5 = new Barcode(numArray5);
		String description5 = "Pool";
		long price5 = 4000;
		double expectedWeightInGrams5 = 1000000000000000.00;
		BarcodedProduct product5 = new BarcodedProduct(barcode5, description5, price5, expectedWeightInGrams5);
		ProductDatabases.BARCODED_PRODUCT_DATABASE.put(barcode5, product5);
		testAddItemClass.addItemViaScanning(product5);
		assertEquals(scaleStub2.massExceeded, true);
		assertEquals(scaleStub3.massExceeded, true);
		scale.getCurrentMassOnTheScale();
	}
	
	@Test
	public void testScanningAndRemovingItemBeyondOverloadWeight() throws OverloadedDevice, DisabledDevice {
		Numeral[] numArray5 = new Numeral[]{num2, num1, num2, num2, num1};
		Barcode barcode5 = new Barcode(numArray5);
		String description5 = "Pool";
		long price5 = 4000;
		double expectedWeightInGrams5 = 1000000000000000.00;
		BarcodedProduct product5 = new BarcodedProduct(barcode5, description5, price5, expectedWeightInGrams5);
		ProductDatabases.BARCODED_PRODUCT_DATABASE.put(barcode5, product5);
		testAddItemClass.addItemViaScanning(product5);
		assertEquals(scaleStub3.massExceeded, true);
		scale.removeAnItem(testAddItemClass.getBarcodedItem());
		assertEquals(scaleStub3.massExceeded, false);
	}
	
	@Test (expected = InvalidStateSimulationException.class)
	public void testBlockingAfterDetectingWeightDiscrepancy() throws OverloadedDevice, DisabledDevice {
		Mass itemMass = new Mass(product2.getExpectedWeight());
		BarcodedItem testBarcodedItem = new BarcodedItem(product2.getBarcode(), itemMass);
		testAddItemClass.weightErrorDetector.addItemToOrder(testBarcodedItem);
		// Causes the discrepancy
		testAddItemClass.addItemViaScanning(product1);
		// Trying to add any more should throw the exception
		testAddItemClass.addItemViaScanning(product1);
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
