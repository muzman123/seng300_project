package com.tdc.coin;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;

import com.tdc.PassiveSource;
import com.tdc.Sink;

import ca.ucalgary.seng300.simulation.InvalidArgumentSimulationException;
import ca.ucalgary.seng300.simulation.NullPointerSimulationException;
import ca.ucalgary.seng300.simulation.SimulationException;

/**
 * Represents a component for optically and/or physically validating coins.
 * Coins deemed valid are moved to storage; coins deemed invalid are ejected.
 * 
 * @author TDC, Inc.
 */
public final class CoinValidator extends AbstractCoinValidator {
	/**
	 * Creates a coin validator that recognizes coins of the specified denominations
	 * (i.e., values) and currency.
	 * 
	 * @param currency
	 *            The kind of currency to accept.
	 * @param coinDenominations
	 *            An array of the valid coin denominations (like $0.05, $0.10, etc.)
	 *            to accept. Each value must be &gt;0 and unique in this array.
	 * @throws SimulationException
	 *             If either argument is null.
	 * @throws SimulationException
	 *             If the denominations array does not contain at least one value.
	 * @throws SimulationException
	 *             If any value in the denominations array is non-positive.
	 * @throws SimulationException
	 *             If any value in the denominations array is non-unique.
	 */
	public CoinValidator(Currency currency, List<BigDecimal> coinDenominations) {
		super(currency, new HashMap<>());
		
		if(currency == null)
			throw new NullPointerSimulationException("currency");

		if(coinDenominations == null)
			throw new NullPointerSimulationException("denominations");

		if(coinDenominations.size() < 1)
			throw new InvalidArgumentSimulationException("There must be at least one denomination.");

		Collections.sort(coinDenominations);

		for(BigDecimal denomination : coinDenominations) {
			if(denomination == null)
				throw new NullPointerSimulationException("denomination instance");

			if(denomination.compareTo(BigDecimal.ZERO) <= 0)
				throw new InvalidArgumentSimulationException(
					"Non-positive denomination detected: " + denomination + ".");

			if(standardSinks.containsKey(denomination))
				throw new InvalidArgumentSimulationException(
					"Each denomination must be unique, but " + denomination + " is repeated.");

			standardSinks.put(denomination, null);
		}

		this.denominations = coinDenominations;
	}
}
