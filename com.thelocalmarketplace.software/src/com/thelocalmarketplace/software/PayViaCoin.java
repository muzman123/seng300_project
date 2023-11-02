package com.thelocalmarketplace.software;

import java.math.BigDecimal;

import com.tdc.coin.Coin;

import ca.ucalgary.seng300.simulation.NullPointerSimulationException;


public class PayViaCoin {
	
	private BigDecimal AMOUNT_DUE;
	private BigDecimal AMOUNT_PAID = BigDecimal.ZERO;
	private boolean amountPaidInFull = false;
	
	public PayViaCoin(BigDecimal amountDue, Coin payment)
	{
		AMOUNT_DUE = amountDue;
		
		receiveCoin(payment);
	}
	
	public PayViaCoin(BigDecimal amountDue, Coin[] payment)
	{
		AMOUNT_DUE = amountDue;
		
		for(Coin coin: payment)
			receiveCoin(coin);
	}
	
	private void receiveCoin(Coin coin)
	{
		if (coin == null)
			throw new NullPointerSimulationException("coin");
		
		if (AMOUNT_DUE.compareTo(BigDecimal.ZERO) > 0)
		{
			AMOUNT_DUE = AMOUNT_DUE.subtract(coin.getValue());
			AMOUNT_PAID = AMOUNT_PAID.add(coin.getValue());
			SignalCustomer();
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
	
	private void SignalCustomer()
	{
		//TODO signal customer on gui of updated amount due
	}
	
}
