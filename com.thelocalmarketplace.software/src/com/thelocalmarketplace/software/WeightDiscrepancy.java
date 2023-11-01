package com.thelocalmarketplace.software;

import java.util.ArrayList;
import java.util.List;

import com.jjjwelectronics.Item;
import com.jjjwelectronics.Mass;

public class WeightDiscrepancy {
    private boolean isDiscrepancyCleared;
    private List<Item> missingItems;
    private List<Item> extraItems;

    public WeightDiscrepancy() {
        isDiscrepancyCleared = true;
        missingItems = new ArrayList<>();
        extraItems = new ArrayList<>();
    }

    public boolean isDiscrepancyCleared() {
        return isDiscrepancyCleared;
    }

    public void addItemByScanning(Item item) {
        if (isDiscrepancyCleared) {
            // Add the item to the order as usual
        } else {
        	System.out.println("");
            extraItems.add(item);
        }
    }

    public void payWithCoin(Mass totalOrderMass) {
        if (isDiscrepancyCleared) {
            // Proceed with payment as usual
        } else {
            Mass actualOrderMass = calculateActualOrderMass();
            if (totalOrderMass.compareTo(actualOrderMass) == 0) {
                isDiscrepancyCleared = true;
            }
        }
    }

    public void clearDiscrepancy() {
        isDiscrepancyCleared = true;
        missingItems.clear();
        extraItems.clear();
    }

    public void addMissingItem(Item item) {
        if (!isDiscrepancyCleared) {
            missingItems.add(item);
        }
    }

    public void removeExtraItem(Item item) {
        if (!isDiscrepancyCleared) {
            extraItems.remove(item);
        }
    }

    private Mass calculateActualOrderMass() {
        // Calculate the actual order mass by summing the masses of added items
        Mass actualMass = Mass.ZERO;
        for (Item item : missingItems) {
            actualMass = actualMass.sum(item.getMass());
        }
        return actualMass;
    }
}
