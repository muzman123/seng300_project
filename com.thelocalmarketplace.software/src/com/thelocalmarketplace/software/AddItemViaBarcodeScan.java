package com.thelocalmarketplace.software;

import java.util.HashMap;
import com.jjjwelectronics.Item;
import com.jjjwelectronics.Mass;
import com.jjjwelectronics.scale.ElectronicScale;
import com.jjjwelectronics.scanner.*;
import com.thelocalmarketplace.hardware.BarcodedProduct;

import ca.ucalgary.seng300.simulation.NullPointerSimulationException;

public class AddItemViaBarcodeScan {
	HashMap<Barcode, BarcodedProduct> BARCODED_PRODUCT_DATABASE;
	ElectronicScale scale;
	BarcodedProduct product;
	private Barcode barcode;
	private Mass mass;
	private double expectedWeightInGrams;
	private long price;
	private boolean isSessionActive;
	private boolean isSessionBlocked;
	
	public AddItemViaBarcodeScan(BarcodedItem barcode) {
		this.barcode = barcode.getBarcode();
		
		// CHECK FOR SESSION
		if(!isSessionBlocked) {
			isSessionBlocked = true;
			if(isSessionActive) {
				getItemInformation();
			}
		} else {
			// Throw session blocked exception
		}
		
	}
	
	private void getItemInformation() {
		product = BARCODED_PRODUCT_DATABASE.get(barcode);
		if (product != null) {
			price = product.getPrice();
			expectedWeightInGrams = product.getExpectedWeight();
		} else {
			throw new NullPointerSimulationException("product");
		}
	}
	
	public long getItemPrice() {
		return price;
	}
	
	public double getExpectedWeight() {
		return expectedWeightInGrams;
	}
	
 	public void updateExpectedWeight(Item item) {
		mass = new Mass(expectedWeightInGrams);
		scale.addAnItem(item);
		isSessionBlocked = false;
	}

}
