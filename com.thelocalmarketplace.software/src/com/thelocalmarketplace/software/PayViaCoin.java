//Names & UCID
//Arvin Bolbolanardestani 30165484
//Zeyad Elrayes 30161958
//Dvij Raval 30024340
//Muzammil Saleem 30180889
//Ryan Wong 30171793
//Danish Sharma 30172600

package com.thelocalmarketplace.software;

import java.math.BigDecimal;

import com.jjjwelectronics.scale.ElectronicScale;
import com.tdc.CashOverloadException;
import com.tdc.DisabledException;
import com.tdc.IComponent;
import com.tdc.IComponentObserver;
import com.tdc.coin.Coin;
import com.tdc.coin.CoinStorageUnit;
import com.tdc.coin.CoinStorageUnitObserver;
import com.tdc.coin.CoinValidator;
import com.tdc.coin.CoinValidatorObserver;

import ca.ucalgary.seng300.simulation.InvalidArgumentSimulationException;
import ca.ucalgary.seng300.simulation.InvalidStateSimulationException;
import ca.ucalgary.seng300.simulation.NullPointerSimulationException;
import powerutility.NoPowerException;


public class PayViaCoin implements CoinValidatorObserver, CoinStorageUnitObserver {
	
	private BigDecimal AMOUNT_DUE;
	private BigDecimal AMOUNT_PAID = BigDecimal.ZERO;
	private BigDecimal CHANGE_OWED = BigDecimal.ZERO;
	private boolean amountPaidInFull = false;
	private CoinValidator coin_validator;
	private CoinStorageUnit storage_unit;
	
	public WeightDiscrepancy weightErrorDetector;
	
	// Constructor to create an instance of pay via coin (reflects the idea that the customer selected
	// this as their mode of payment and then will begin to input coin/coins)
	public PayViaCoin(BigDecimal amountDue, CoinValidator validator, CoinStorageUnit storage, ElectronicScale inputScale) {
		if(amountDue.equals(BigDecimal.ZERO)) {
			throw new InvalidArgumentSimulationException("Amount due argument cannot be zero");
		}
		AMOUNT_DUE = amountDue;
		coin_validator = validator;
		storage_unit = storage;
		coin_validator.attach(this);
		storage_unit.attach(this);
		weightErrorDetector = new WeightDiscrepancy(inputScale);
	}
	
	// A single coin was added to the payment by the customer
	public void coinAdded(Coin coin) throws DisabledException, CashOverloadException {
		if (coin == null) {
			throw new NullPointerSimulationException("coin");
		}
		else {
			receiveCoin(coin);
		}
	}
	
	// Multiple coins was added to the payment by the customer
	public void coinsAdded(Coin[] coins) throws DisabledException, CashOverloadException {
		for(Coin coin: coins) {
			if (coin == null) {
				throw new NullPointerSimulationException("coin");
			}
			else {
				receiveCoin(coin);
			}
		}
	}
	
	private void receiveCoin(Coin coin) throws DisabledException, CashOverloadException {
		if(!coin_validator.isActivated() || !storage_unit.isActivated()) {
			throw new NoPowerException();
		}
		// Check if session is blocked due to a discrepancy
		if (weightErrorDetector.discrepancyStatus()) {
			CHANGE_OWED = CHANGE_OWED.add(coin.getValue());
			// TODO Implement behavior for scanning when a discrepancy is detected (likely a thrown exception which produces a effect elsewhere)
			throw new InvalidStateSimulationException("Can not pay by coin as session is blocked");
		}
		// If amount is not paid in full then take the payment and subtract it from the AMOUNT_DUE
		if (!amountPaidInFull) {
			AMOUNT_DUE = AMOUNT_DUE.subtract(coin.getValue());
			AMOUNT_PAID = AMOUNT_PAID.add(coin.getValue());
			try {
				coin_validator.receive(coin);
				// If a valid coin is detected then send it to storage
				if (validCoinDetected) {
					validCoinDetected = false; // Reset it for the next coin
					storage_unit.receive(coin);
					// Check if the storage received the coin
					if (coinAddedToStorage) {
						coinAddedToStorage = false; // Reset it for the next coin
						// TODO notify that a valid coin has been added to storage (Update the GUI with the new amount_due and amount_paid)
					} // else {} TODO Inform the customer over the GUI the coin did not go to storage, will require employee assistance
				} 
				// if (invalidCoinDetected) TODO In later iterations, the customer will be informed where their invalid coin is routed
				
			} catch (DisabledException e) {
				throw new DisabledException(); // TODO Implement behavior relevant to other classes invoking this method
			} catch (CashOverloadException e) {
				throw new CashOverloadException(); // TODO Implement behavior relevant to other classes invoking this method
			}
			// signalCustomer(); (To implement later with GUI)
			// if after adding the most recent payment the amount due is less then or equal to zero then
			// set that as the amount of baseline changed owed and change the boolean flag to true
			if (AMOUNT_DUE.compareTo(BigDecimal.ZERO) <= 0) {
				CHANGE_OWED = CHANGE_OWED.add(AMOUNT_DUE.abs());
				AMOUNT_DUE = BigDecimal.ZERO;
				amountPaidInFull = true;
				// dispenseChange(); To implement later, for now we just keep the over payment
				// printReceipt(); To implement later, this call will end in the conclusion of this checkout session
			}
		} 
		// else add the coins to the change owed
		else {
			AMOUNT_PAID = AMOUNT_PAID.add(coin.getValue());
			CHANGE_OWED = CHANGE_OWED.add(coin.getValue());
		}
	}
	
	// TODO signal customer on GUI of updated amount due in a signalCustomer() method
	// TODO implement print receipt functionality in a PrintReceipt class

	public BigDecimal getAmountDue() {
		return AMOUNT_DUE;
	}
	public BigDecimal getAmountPaid() {
		return AMOUNT_PAID;
	}
	public BigDecimal getChangeOwed() {
		return CHANGE_OWED;
	}
	public boolean isAmountPaid() {
		return amountPaidInFull;
	}

	
	
	private boolean storageFull;
	private boolean coinAddedToStorage;
	private boolean validCoinDetected;
	
	// Implementing the interfaces
	@Override
	public void enabled(IComponent<? extends IComponentObserver> component) {
		// Not needed
	}

	@Override
	public void disabled(IComponent<? extends IComponentObserver> component) {
		// Not needed
	}

	@Override
	public void turnedOn(IComponent<? extends IComponentObserver> component) {
		// Not needed
	}

	@Override
	public void turnedOff(IComponent<? extends IComponentObserver> component) {
		// Not needed
	}

	@Override
	public void coinsFull(CoinStorageUnit unit) {
		storageFull = true;
	}

	@Override
	public void coinAdded(CoinStorageUnit unit) {
		coinAddedToStorage = true;
	}

	@Override
	public void coinsLoaded(CoinStorageUnit unit) {
		// Not needed
	}

	@Override
	public void coinsUnloaded(CoinStorageUnit unit) {
		// Not needed
	}

	@Override
	public void validCoinDetected(CoinValidator validator, BigDecimal value) {
		validCoinDetected = true;
	}

	@Override
	public void invalidCoinDetected(CoinValidator validator) {
		// Not needed
	}
	
}