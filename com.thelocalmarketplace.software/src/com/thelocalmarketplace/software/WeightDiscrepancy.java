package com.thelocalmarketplace.software;

import com.jjjwelectronics.Item;
import com.jjjwelectronics.Mass;
import com.jjjwelectronics.scale.IElectronicScale;
import com.jjjwelectronics.scale.ElectronicScaleListener;

import java.util.ArrayList;
import java.util.List;

public abstract class WeightDiscrepancy implements ElectronicScaleListener {
    private IElectronicScale scale;
    private List<Item> orderItems = new ArrayList<>();
    private List<Item> itemsOnScale = new ArrayList<>();

    private boolean discrepancyCleared = true;

    public WeightDiscrepancy(IElectronicScale scale) {
        this.scale = scale;
        scale.listeners();
    }


    // Attempt to add an item by scanning
    public void addItemByScanning(Item item) {
        if (discrepancyCleared) {
            // Check if the item is part of the order
            if (orderItems.contains(item)) {
                itemsOnScale.add(item);
            }
        }else {
        	throw new IllegalStateException("Cannot add item until discrepancy is cleared.");
        }
    }

    // Pay with coins based on the total order mass
    public void payWithCoin(Mass totalOrderMass) {
        if (discrepancyCleared && totalOrderMass.equals(calculateTotalMassOnScale())) {
            // Payment successful, clear discrepancy
            discrepancyCleared = true;
        }else {
        	throw new IllegalStateException("Cannot pay with coin until discrepancy is cleared.");
        }
    }

    // Add a missing item to the scale
    public void addMissingItem(Item missingItem) {
        if (!discrepancyCleared) {
            itemsOnScale.add(missingItem);
        }
    }

    // Remove an extra item from the scale
    public void removeExtraItem(Item extraItem) {
        if (!discrepancyCleared) {
            itemsOnScale.remove(extraItem);
        }
    }

    // Clear the discrepancy
    public void clearDiscrepancy() {
        if (!discrepancyCleared) {
            itemsOnScale.clear();
            discrepancyCleared = true;
        }
    }

    // Calculate the total mass of items on the scale
    private Mass calculateTotalMassOnScale() {
        Mass totalMass = Mass.ZERO;
        for (Item item : itemsOnScale) {
            totalMass = totalMass.sum(item.getMass());
        }
        return totalMass;
    }

    @Override
    public void theMassOnTheScaleHasChanged(IElectronicScale scale, Mass mass) {
    	System.out.println("The mass on the scale has changed! ");
        // Check if the mass on the scale matches the total order mass
        if (!discrepancyCleared && mass.equals(calculateTotalMassOnScale())) {
            discrepancyCleared = true;
        }
    }

    @Override
    public void theMassOnTheScaleHasExceededItsLimit(IElectronicScale scale) {
    	System.out.println("The mass on the scale has exceeded its limits! ");
    }

    @Override
    public void theMassOnTheScaleNoLongerExceedsItsLimit(IElectronicScale scale) {
    	System.out.println("The mass on the scale returned below the limit! ");
    }
}
