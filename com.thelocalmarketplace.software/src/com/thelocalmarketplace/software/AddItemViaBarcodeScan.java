package com.thelocalmarketplace.software;

import com.jjjwelectronics.Mass;
import com.jjjwelectronics.scale.ElectronicScale;
import com.jjjwelectronics.scanner.BarcodeScanner;
import com.jjjwelectronics.scanner.BarcodedItem;
import com.thelocalmarketplace.hardware.BarcodedProduct;
import com.thelocalmarketplace.hardware.external.ProductDatabases;

import ca.ucalgary.seng300.simulation.InvalidArgumentSimulationException;
import ca.ucalgary.seng300.simulation.NullPointerSimulationException;
import powerutility.NoPowerException;

public class AddItemViaBarcodeScan {
	private boolean isSessionActive;
	private boolean isSessionBlocked;
	private double productWeight;
	private long productPrice;
	private Mass itemMass;
	private BarcodedItem barcodedItem;
	
	public void addItemViaScanning(BarcodedProduct product, BarcodeScanner scanner, ElectronicScale scale) {
		if(!scanner.isPoweredUp() || !scale.isPoweredUp())
			throw new NoPowerException();
	
		if(product == null)
			throw new NullPointerSimulationException("product was null");
		
		// Check if session is blocked
		if (!isSessionBlocked) {
			isSessionBlocked = true;
			if(isSessionActive) {
				// Scan only if the product is present in the database
				if (ProductDatabases.BARCODED_PRODUCT_DATABASE.containsKey(product.getBarcode())) {
					// Determine the weight and cost of the item
					infoAboutAddedProduct(product);
					
					// Convert BarcodedProduct from store hardware to BarcodedItem used by jjjwelectronics
					barcodedItem = convertToBarcodedItem(product);
					
					// Scan the item through the scanner (will notify listeners)
					scanner.scan(barcodedItem);
					
					// Update the weight on the scale (will notify listeners)
					scale.addAnItem(barcodedItem);
					
				} else {
					// Search if there is a dedicated exception for this
					throw new InvalidArgumentSimulationException("Barcode is not in the database");
				}
			}
			isSessionBlocked = false;
		}
	}
	
	public void setSessionActiveStatus(boolean active) {
		isSessionActive = active;
	}
	
	public void setSessionBlockStatus(boolean block) {
		isSessionBlocked = block;
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

}