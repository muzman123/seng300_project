package com.tdc.coin;

import java.util.Arrays;
import java.util.List;

import com.tdc.AbstractComponent;
import com.tdc.CashOverloadException;
import com.tdc.DisabledException;
import com.tdc.Sink;

import ca.ucalgary.seng300.simulation.NullPointerSimulationException;
import ca.ucalgary.seng300.simulation.SimulationException;
import powerutility.NoPowerException;

/**
 * Abstract base class for components that store coins.
 * 
 * @author TDC, Inc.
 */
public abstract class AbstractCoinStorageUnit extends AbstractComponent<CoinStorageUnitObserver> implements Sink<Coin> {
	protected Coin[] storage;
	protected int nextIndex = 0;

	protected AbstractCoinStorageUnit() {
		super();
	}

	/**
	 * Gets the maximum number of coins that this storage unit can hold. Does not
	 * require power.
	 * 
	 * @return The capacity.
	 */
	public int getCapacity() {
		return storage.length;
	}

	/**
	 * Gets the current count of coins contained in this storage unit. Requires
	 * power.
	 * 
	 * @return The current count.
	 */
	public synchronized int getCoinCount() {
		if(!isActivated())
			throw new NoPowerException();

		return nextIndex + 1;
	}

	/**
	 * Allows a set of coins to be loaded into the storage unit directly. Existing
	 * coins in the dispenser are not removed. Announces "coinsLoaded" event.
	 * Disabling has no effect on loading/unloading. Requires power.
	 * 
	 * @param coins
	 *            A sequence of coins to be added. Each cannot be null.
	 * @throws SimulationException
	 *             if the number of coins to be loaded exceeds the capacity of the
	 *             unit.
	 * @throws SimulationException
	 *             If coins is null.
	 * @throws SimulationException
	 *             If any coin is null.
	 * @throws CashOverloadException
	 *             If too many coins are loaded.
	 */
	public synchronized void load(Coin... coins) throws SimulationException, CashOverloadException {
		if(!isActivated())
			throw new NoPowerException();

		if(coins == null)
			throw new NullPointerSimulationException("coins");

		if(coins.length + nextIndex > storage.length)
			throw new CashOverloadException("You tried to cram too many coins in the storage unit.");

		for(Coin coin : coins)
			if(coin == null)
				throw new NullPointerSimulationException("coin instance");

		System.arraycopy(coins, 0, storage, nextIndex, coins.length);
		nextIndex += coins.length;
	}

	/**
	 * Unloads coins from the storage unit directly. Announces "coinsUnloaded"
	 * event. Requires power.
	 * 
	 * @return A list of the coins unloaded. May be empty. Will never be null.
	 */
	public synchronized List<Coin> unload() {
		if(!isActivated())
			throw new NoPowerException();

		List<Coin> coins = Arrays.asList(storage);

		storage = new Coin[storage.length];
		nextIndex = 0;
		notifyCoinsUnloaded();

		return coins;
	}

	/**
	 * Causes the indicated coin to be added to the storage unit. If successful,
	 * announces "coinAdded" event. If a successful coin addition instead causes the
	 * unit to become full, announces "coinsFull" event. Requires power.
	 * 
	 * @param coin
	 *            The coin to add to this unit.
	 * @throws DisabledException
	 *             If the unit is currently disabled.
	 * @throws SimulationException
	 *             If coin is null.
	 * @throws CashOverloadException
	 *             If the unit is already full.
	 */
	public synchronized void receive(Coin coin) throws DisabledException, CashOverloadException {
		if(!isActivated())
			throw new NoPowerException();

		if(isDisabled())
			throw new DisabledException();

		if(coin == null)
			throw new NullPointerSimulationException("coin");

		if(nextIndex < storage.length) {
			storage[nextIndex++] = coin;

			notifyCoinAdded();

			if(nextIndex == storage.length)
				notifyCoinsFull();
		}
		else
			throw new CashOverloadException();
	}

	@Override
	public synchronized boolean hasSpace() {
		if(!isActivated())
			throw new NoPowerException();

		return nextIndex < storage.length;
	}

	protected void notifyCoinsLoaded() {
		for(CoinStorageUnitObserver observer : observers)
			observer.coinsLoaded(this);
	}

	protected void notifyCoinsUnloaded() {
		for(CoinStorageUnitObserver observer : observers)
			observer.coinsUnloaded(this);
	}

	protected void notifyCoinsFull() {
		for(CoinStorageUnitObserver observer : observers)
			observer.coinsFull(this);
	}

	protected void notifyCoinAdded() {
		for(CoinStorageUnitObserver observer : observers)
			observer.coinsLoaded(this);
	}
}