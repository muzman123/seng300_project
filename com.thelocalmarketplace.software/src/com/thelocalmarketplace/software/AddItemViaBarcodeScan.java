package com.thelocalmarketplace.software;

import java.util.HashMap;
import java.util.Map;

import com.jjjwelectronics.Item;
import com.jjjwelectronics.Mass;
import com.jjjwelectronics.scale.ElectronicScale;
import com.jjjwelectronics.scanner.*;
import com.thelocalmarketplace.hardware.BarcodedProduct;
import com.thelocalmarketplace.hardware.external.*;

public class AddItemViaBarcodeScan {
	HashMap<Barcode, BarcodedProduct> BARCODED_PRODUCT_DATABASE;
	ElectronicScale scale;
	BarcodedProduct product;
	private Barcode barcode;
	private Mass mass;
	private double expectedWeightInGrams;
	private long price;
	private boolean isActiveSession;
	
	public AddItemViaBarcodeScan(BarcodedItem barcode) {
		this.barcode = barcode.getBarcode();
		
		// CHECK FOR SESSION
//		if(isActiveSession) {
//			getItemInformation();
//			
//		}
	}
	
	public void getItemInformation() {
		product = BARCODED_PRODUCT_DATABASE.get(barcode);
		if (product != null) {
			price = product.getPrice();
			expectedWeightInGrams = product.getExpectedWeight();
		} else {
			// Throw error?
		}
	}
	
	public void updateExpectedWeight(Item item) {
		mass = new Mass(expectedWeightInGrams);
		scale.addAnItem(item);
	}

}
