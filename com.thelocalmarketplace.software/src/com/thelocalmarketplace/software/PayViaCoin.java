package com.thelocalmarketplace.software;

import java.math.BigDecimal;

import com.tdc.coin.Coin;
import com.tdc.coin.CoinStorageUnit;
import com.tdc.coin.CoinValidator;

import ca.ucalgary.seng300.simulation.InvalidArgumentSimulationException;
import ca.ucalgary.seng300.simulation.NullPointerSimulationException;


public class PayViaCoin {
	
	private BigDecimal AMOUNT_DUE;
	private BigDecimal AMOUNT_PAID = BigDecimal.ZERO;
	private boolean amountPaidInFull = false;
	private CoinValidator coin_validator;
	private CoinStorageUnit storage_unit;
	
	public PayViaCoin(BigDecimal amountDue, Coin payment, CoinValidator validator, CoinStorageUnit storage)
	{
		if(amountDue == BigDecimal.ZERO)
			throw new InvalidArgumentSimulationException("Amount due argument cannot be zero");
		
		AMOUNT_DUE = amountDue;
		coin_validator = validator;
		storage_unit = storage;
		
		try {
			receiveCoin(payment);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
	}
	
	public PayViaCoin(BigDecimal amountDue, Coin[] payment, CoinValidator validator, CoinStorageUnit storage)
	{
		if(amountDue == BigDecimal.ZERO)
			throw new InvalidArgumentSimulationException("Amount due argument cannot be zero");
		
		AMOUNT_DUE = amountDue;
		coin_validator = validator;
		storage_unit = storage;
		
		for(Coin coin: payment)
			try {
				receiveCoin(coin);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
	}
	
	private void receiveCoin(Coin coin) throws Exception
	{
		if (coin == null)
			throw new NullPointerSimulationException("coin");
		
		if (AMOUNT_DUE.compareTo(BigDecimal.ZERO) > 0)
		{
			AMOUNT_DUE = AMOUNT_DUE.subtract(coin.getValue());
			AMOUNT_PAID = AMOUNT_PAID.add(coin.getValue());
			try 
			{
				coin_validator.receive(coin);
				storage_unit.receive(coin);
			}
			catch(Exception e)
			{
				throw e;
			}
				
			SignalCustomer();
		}
		else if (AMOUNT_DUE.equals(BigDecimal.ZERO))
		{
			amountPaidInFull = true;
			PrintReceipt();
		}
			
	}
	
	private void PrintReceipt() {
		// TODO implement print receipt functionality
		
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
		//TODO signal customer on GUI of updated amount due
	}
	
}
