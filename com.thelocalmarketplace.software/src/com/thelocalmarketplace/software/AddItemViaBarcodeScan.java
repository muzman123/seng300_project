//Names & UCID
//Arvin Bolbolanardestani 30165484
//Zeyad Elrayes 30161958
//Dvij Raval 30024340
//Muzammil Saleem 30180889
//Ryan Wong 30171793
//Danish Sharma 30172600

package com.thelocalmarketplace.software;

import com.jjjwelectronics.DisabledDevice;
import com.jjjwelectronics.IDevice;
import com.jjjwelectronics.IDeviceListener;
import com.jjjwelectronics.Mass;
import com.jjjwelectronics.OverloadedDevice;
import com.jjjwelectronics.scale.ElectronicScale;
import com.jjjwelectronics.scale.ElectronicScaleListener;
import com.jjjwelectronics.scale.IElectronicScale;
import com.jjjwelectronics.scanner.Barcode;
import com.jjjwelectronics.scanner.BarcodeScanner;
import com.jjjwelectronics.scanner.BarcodeScannerListener;
import com.jjjwelectronics.scanner.BarcodedItem;
import com.jjjwelectronics.scanner.IBarcodeScanner;
import com.thelocalmarketplace.hardware.BarcodedProduct;
import com.thelocalmarketplace.hardware.external.ProductDatabases;

import ca.ucalgary.seng300.simulation.InvalidArgumentSimulationException;
import ca.ucalgary.seng300.simulation.InvalidStateSimulationException;
import ca.ucalgary.seng300.simulation.NullPointerSimulationException;
import powerutility.NoPowerException;

public class AddItemViaBarcodeScan implements ElectronicScaleListener, BarcodeScannerListener {
	private double productWeight;
	private long productPrice;
	private Mass itemMass;
	private BarcodedItem barcodedItem;
	
	private BarcodeScanner scanner;
	private ElectronicScale scale;
	
	public WeightDiscrepancy weightErrorDetector;
	
	public AddItemViaBarcodeScan(BarcodeScanner inputScanner, ElectronicScale inputScale) {
		scanner = inputScanner;
		scale = inputScale;
		scanner.register(this);
		scale.register(this);
		weightErrorDetector = new WeightDiscrepancy(scale);
	}
	
	public void addItemViaScanning(BarcodedProduct product) throws OverloadedDevice, DisabledDevice {
		// Neither of the scanner or scale can be without power
		if(!scannerPower || !scalePower) {
			throw new NoPowerException();
		}
		// Neither of the scanner or scale can be disabled
		if (!scannerEnabled || !scaleEnabled) {
			throw new DisabledDevice();
		}
		// Product can not be null
		if(product == null) {
			throw new NullPointerSimulationException("product was null");
		}
		// Check if session is blocked due to a discrepancy
		if (weightErrorDetector.discrepancyStatus()) {
			// TODO Implement behavior for scanning when a discrepancy is detected (likely a thrown exception which produces a effect elsewhere)
			throw new InvalidStateSimulationException("Can not scan item as session is blocked");
		}

		// Scan only if the product is present in the database
		if (ProductDatabases.BARCODED_PRODUCT_DATABASE.containsKey(product.getBarcode())) {
			// Determine the weight and cost of the item
			this.infoAboutAddedProduct(product);
			// Convert BarcodedProduct from store hardware to BarcodedItem used by jjjwelectronics
			barcodedItem = convertToBarcodedItem(product);
			weightErrorDetector.addItemToOrder(barcodedItem);
			// Scan the item through the scanner (will notify listeners)
			scanner.scan(barcodedItem);
			// Get current weight on the scale
			Mass currentWeight = scale.getCurrentMassOnTheScale();
			Mass expectedWeight = currentWeight.sum(barcodedItem.getMass());
				
			// Update the weight on the scale (will notify listeners)
			scale.addAnItem(barcodedItem);
			
			if (scaleOverloaded) {
				// TODO inform other aspects of the program about the scale being overloaded in future implementations
			}
			if (updatedMass.equals(expectedWeight)) {
				// TODO Notify the session that an item has been successfully scanned (behavior depends on future implementations)
			} // else {} TODO Once there is a GUI/Mechanism for the item not be added to the scale
		} else {
			// Search if there is a dedicated exception for this
			throw new InvalidArgumentSimulationException("Barcode is not in the database");
		}
	}

	
	private void infoAboutAddedProduct(BarcodedProduct product) {
		productPrice = product.getPrice();
		productWeight = product.getExpectedWeight();
	}
	
	public long getAddedProductPrice() {
		return productPrice;
	}
	
	public double getAddedProductWeight() {
		return productWeight;
	}
	
	private BarcodedItem convertToBarcodedItem(BarcodedProduct product) {
		itemMass = new Mass(product.getExpectedWeight());
		barcodedItem = new BarcodedItem(product.getBarcode(), itemMass);
		return barcodedItem;
	}
	
	public BarcodedItem getBarcodedItem() {
		return this.barcodedItem;
	}

	
	
	private boolean scannerPower;
	private boolean scalePower;
	
	private boolean scannerEnabled;
	private boolean scaleEnabled;
	
	private Barcode barcodeScanned;
	private boolean scaleOverloaded;
	private Mass updatedMass;
	
	// Implementing the interfaces
	@Override
	public void aDeviceHasBeenEnabled(IDevice<? extends IDeviceListener> device) {
		if (device instanceof BarcodeScanner) {
			scannerEnabled = true;
		} if (device instanceof ElectronicScale) {
			scaleEnabled = true;
		}
	}

	@Override
	public void aDeviceHasBeenDisabled(IDevice<? extends IDeviceListener> device) {
		if (device instanceof BarcodeScanner) {
			scannerEnabled = false;
		} if (device instanceof ElectronicScale) {
			scaleEnabled = false;
		}
	}

	@Override
	public void aDeviceHasBeenTurnedOn(IDevice<? extends IDeviceListener> device) {
		if (device instanceof BarcodeScanner) {
			scannerPower = true;
		} if (device instanceof ElectronicScale) {
			scalePower = true;
		}
	}

	@Override
	public void aDeviceHasBeenTurnedOff(IDevice<? extends IDeviceListener> device) {
		if (device instanceof BarcodeScanner) {
			scannerPower = false;
		} if (device instanceof ElectronicScale) {
			scalePower = false;
		}
	}

	@Override
	public void theMassOnTheScaleHasChanged(IElectronicScale scale, Mass mass) {
		updatedMass = mass;
	}

	@Override
	public void theMassOnTheScaleHasExceededItsLimit(IElectronicScale scale) {
		scaleOverloaded = true;
	}

	@Override
	public void theMassOnTheScaleNoLongerExceedsItsLimit(IElectronicScale scale) {
		scaleOverloaded = false;
	}

	@Override
	public void aBarcodeHasBeenScanned(IBarcodeScanner barcodeScanner, Barcode barcode) {
		barcodeScanned = barcode;
	}

}