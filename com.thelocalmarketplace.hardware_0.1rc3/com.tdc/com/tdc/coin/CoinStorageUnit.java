package com.tdc.coin;

import com.tdc.Sink;

import ca.ucalgary.seng300.simulation.InvalidArgumentSimulationException;
import ca.ucalgary.seng300.simulation.SimulationException;

/**
 * Represents components that store coins. They only receive coins, not dispense
 * them. To access the coins inside, a human operator needs to physically remove
 * the coins, simulated with the {@link #unload()} method. A
 * {@link #load(Coin...)} method is provided for symmetry.
 *            
 * @author TDC, Inc.
 */
public class CoinStorageUnit extends AbstractCoinStorageUnit {
	/**
	 * Creates a coin storage unit that can hold the indicated number of coins.
	 * 
	 * @param capacity
	 *            The maximum number of coins that the unit can hold.
	 * @throws SimulationException
	 *             If the capacity is not positive.
	 */
	public CoinStorageUnit(int capacity) {
		super();
		if(capacity <= 0)
			throw new InvalidArgumentSimulationException("The capacity must be positive.");

		storage = new Coin[capacity];
	}
}
