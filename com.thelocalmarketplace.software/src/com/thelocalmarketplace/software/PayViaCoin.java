package com.thelocalmarketplace.software;

import java.math.BigDecimal;

import com.tdc.coin.Coin;

import ca.ucalgary.seng300.simulation.NullPointerSimulationException;


public class PayViaCoin {
	
	private BigDecimal AMOUNT_DUE;
	private BigDecimal AMOUNT_PAID;
	private boolean amountPaidInFull = false;
	
	public PayViaCoin(BigDecimal amountDue)
	{
		AMOUNT_DUE = amountDue;
	}
	
	public void receiveCoin(Coin coin)
	{
		if (coin.getValue() == null)
			throw new NullPointerSimulationException("coin value");
		
		if (AMOUNT_DUE.compareTo(BigDecimal.ZERO) > 0)
		{
			AMOUNT_DUE = AMOUNT_DUE.subtract(coin.getValue());
			AMOUNT_PAID = AMOUNT_PAID.add(coin.getValue());
		}
		else if (AMOUNT_DUE.equals(BigDecimal.ZERO))
		{
			amountPaidInFull = true;
		}
			
	}
	
	public BigDecimal getAmountDue()
	{
		return AMOUNT_DUE;
	}
	
	public BigDecimal getAmountPaid()
	{
		return AMOUNT_PAID;
	}
	
	public boolean isAmountPaid()
	{
		return amountPaidInFull;
	}
	
}
