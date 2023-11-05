//Names & UCID
//Arvin Bolbolanardestani 30165484
//Zeyad Elrayes 30161958
//Dvij Raval 30024340
//Muzammil Saleem 30180889
//Ryan Wong 30171793
//Danish Sharma 30172600

package com.thelocalmarketplace.software;

import com.jjjwelectronics.IDevice;
import com.jjjwelectronics.IDeviceListener;
import com.jjjwelectronics.Item;
import com.jjjwelectronics.Mass;
import com.jjjwelectronics.scale.IElectronicScale;

import com.jjjwelectronics.scale.ElectronicScaleListener;

import java.util.ArrayList;
import java.util.List;

public class WeightDiscrepancy implements ElectronicScaleListener {
    private IElectronicScale scale;
    private List<Item> itemsInOrder = new ArrayList<>();
    
    private boolean discrepancy = false;
    public boolean overloaded = false;

    public WeightDiscrepancy(IElectronicScale inputScale) {
        this.scale = inputScale;
        this.scale.register(this);
    }
    
    public void addItemToOrder(Item item) {
    	itemsInOrder.add(item);
    }

    // Calculate the total mass of items expected on the scale
    public Mass calculateTotalMassExpectedOnScale() {
        Mass totalMass = Mass.ZERO;
        for (Item item : itemsInOrder) {
            totalMass = totalMass.sum(item.getMass());
        }
        return totalMass;
    }
    
    public boolean discrepancyStatus() {
    	return this.discrepancy;
    }

    @Override
    public void theMassOnTheScaleHasChanged(IElectronicScale scale, Mass mass) {
    	System.out.println("The mass on the scale has changed! ");
    	// If there was already a discrepancy and the new mass matches expectations then remove the discrepancy
    	if (discrepancy && mass.equals(calculateTotalMassExpectedOnScale())) {
    		discrepancy = false;
    	}
        // There was no discrepancy but the new mass doesn't match expected mass means there is now a discrepancy present
    	if (!discrepancy && !mass.equals(calculateTotalMassExpectedOnScale())) {
            discrepancy = true;
        } 
    }

    @Override
    public void theMassOnTheScaleHasExceededItsLimit(IElectronicScale scale) {
    	System.out.println("The mass on the scale has exceeded its limits! ");
    	// TODO In future implementations this will likely be a method call to inform other aspects of the program the scale has passed its limit
    	overloaded = true;
    }

    @Override
    public void theMassOnTheScaleNoLongerExceedsItsLimit(IElectronicScale scale) {
    	System.out.println("The mass on the scale returned below the limit! ");
    	// TODO In future implementations this will likely be a method call to inform other aspects of the program the scale is back below its limit
    	overloaded = false;
    }

	@Override
	public void aDeviceHasBeenEnabled(IDevice<? extends IDeviceListener> device) {
		// Not needed
	}

	@Override
	public void aDeviceHasBeenDisabled(IDevice<? extends IDeviceListener> device) {
		// Not needed
	}

	@Override
	public void aDeviceHasBeenTurnedOn(IDevice<? extends IDeviceListener> device) {
		// Not needed
	}

	@Override
	public void aDeviceHasBeenTurnedOff(IDevice<? extends IDeviceListener> device) {
		// Not needed
	}
}
