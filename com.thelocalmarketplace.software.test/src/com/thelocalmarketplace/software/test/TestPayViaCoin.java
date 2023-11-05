//Names & UCID
//Arvin Bolbolanardestani 30165484
//Zeyad Elrayes 30161958
//Dvij Raval 30024340
//Muzammil Saleem 30180889
//Ryan Wong 30171793
//Danish Sharma 30172600

package com.thelocalmarketplace.software.test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.jjjwelectronics.Mass;
import com.jjjwelectronics.Numeral;
import com.jjjwelectronics.scale.ElectronicScale;
import com.jjjwelectronics.scanner.Barcode;
import com.jjjwelectronics.scanner.BarcodedItem;
import com.tdc.CashOverloadException;
import com.tdc.DisabledException;
import com.tdc.IComponent;
import com.tdc.IComponentObserver;
import com.tdc.Sink;
import com.tdc.coin.Coin;
import com.tdc.coin.CoinStorageUnit;
import com.tdc.coin.CoinStorageUnitObserver;
import com.tdc.coin.CoinValidator;
import com.tdc.coin.CoinValidatorObserver;
import com.thelocalmarketplace.software.PayViaCoin;

import ca.ucalgary.seng300.simulation.InvalidArgumentSimulationException;
import ca.ucalgary.seng300.simulation.InvalidStateSimulationException;
import ca.ucalgary.seng300.simulation.NullPointerSimulationException;

import static java.util.Map.entry;
import static org.junit.Assert.assertEquals;

import powerutility.NoPowerException;
import powerutility.PowerGrid;

public class TestPayViaCoin {
	// Fields for initializing CoinValidator
	private CoinValidator coinValidator;
	private Coin coin1;
	private Coin coin2;
	private Coin coin3;
	private Coin coin4;
	private Currency canadianDollar = Currency.getInstance("CAD");
	private CoinValidatorObserverStub validatorStub1;
	private CoinValidatorObserverStub validatorStub2;
	BigDecimal coinValue1 = new BigDecimal("0.01");
	BigDecimal coinValue2 = new BigDecimal("0.10");
	BigDecimal coinValue3 = new BigDecimal("0.25");
	BigDecimal coinValue4 = new BigDecimal("1.00");
	List<BigDecimal> coinDenominations = Arrays.asList(coinValue1, coinValue2, coinValue3, coinValue4);
	private SinkStub rejectionSinkStub;
	private SinkStub overflowSinkStub;
	private SinkStub centSinkStub;
	private SinkStub dimeDollarSinkStub;
	private SinkStub quarterSinkStub;
	private SinkStub dollarSinkStub;
	private Map<BigDecimal, Sink<Coin>> standardSinkStubs;
	
	// Fields for initializing CoinStorageUnit
	private CoinStorageUnit storageUnit;
	private CoinStorageUnitObserverStub storageStub1;
	private CoinStorageUnitObserverStub storageStub2;
	
	private BigDecimal AMOUNT_DUE;
	private PayViaCoin testPayViaCoin;
	
	// The scale for detecting weight discrepancy
	private ElectronicScale scale;

	@Before
	public void setup() {
		// Setup for coin validator
		coinValidator = new CoinValidator(canadianDollar, coinDenominations);
		// Create the coins
		coin1 = new Coin(canadianDollar, coinValue1);
		coin2 = new Coin(canadianDollar, coinValue2);
		coin3 = new Coin(canadianDollar, coinValue3);
		coin4 = new Coin(canadianDollar, coinValue4);
		// Create the stub sinks used for the setup() method
		rejectionSinkStub = new SinkStub();
		overflowSinkStub = new SinkStub();
		centSinkStub = new SinkStub();
		dimeDollarSinkStub = new SinkStub();
		quarterSinkStub = new SinkStub();
		dollarSinkStub = new SinkStub();
		standardSinkStubs = Map.ofEntries(entry(coinValue1, centSinkStub), 
				entry(coinValue2, dimeDollarSinkStub), 
				entry(coinValue3, quarterSinkStub), 
				entry(coinValue4, dollarSinkStub));
		coinValidator.setup(rejectionSinkStub, standardSinkStubs, overflowSinkStub);
		
		// Setup for coin storage unit
		storageUnit = new CoinStorageUnit(3);

		// Create observer stubs for the validator and storage unit
		validatorStub1 = new CoinValidatorObserverStub();
		validatorStub2 = new CoinValidatorObserverStub();
		coinValidator.attach(validatorStub1);
		coinValidator.attach(validatorStub2);
		storageStub1 = new CoinStorageUnitObserverStub();
		storageStub2 = new CoinStorageUnitObserverStub();
		storageUnit.attach(storageStub1);
		storageUnit.attach(storageStub2);
		
		// Setup an instance of the PayViaCoin class
		scale = new ElectronicScale();
		AMOUNT_DUE = new BigDecimal("0.75");
		testPayViaCoin = new PayViaCoin(AMOUNT_DUE, coinValidator, storageUnit, scale);
		
		//Connect the component to the power grid which can not surge
		PowerGrid.engageUninterruptiblePowerSource();
		coinValidator.connect(PowerGrid.instance());
		coinValidator.activate();
		coinValidator.enable();
		storageUnit.connect(PowerGrid.instance());
		storageUnit.activate();
		storageUnit.enable();
		scale.plugIn(PowerGrid.instance());
		scale.turnOn();
		scale.enable();
	}
	
	// Tests
	@Test (expected = InvalidArgumentSimulationException.class)
	public void testAmountZero() throws DisabledException, CashOverloadException {
		BigDecimal zeroAmount = BigDecimal.ZERO;
		testPayViaCoin = new PayViaCoin(zeroAmount, coinValidator, storageUnit, scale);
	}
	
	@Test (expected = NoPowerException.class)
	public void testCoinValidatorNotPowered() throws DisabledException, CashOverloadException {
		coinValidator.disactivate();
		testPayViaCoin.coinAdded(coin1);
	}
	
	@Test (expected = NoPowerException.class)
	public void testStorageUnitNotPowered() throws DisabledException, CashOverloadException {
		storageUnit.disactivate();
		testPayViaCoin.coinAdded(coin2);
	}
	
	@Test (expected = DisabledException.class)
	public void testCoinValidatorDisabled() throws DisabledException, CashOverloadException{
		coinValidator.disable();
		testPayViaCoin.coinAdded(coin3);
	}
	
	@Test (expected = DisabledException.class)
	public void testStorageUnitDisabled() throws DisabledException, CashOverloadException {
		storageUnit.disable();
		testPayViaCoin.coinAdded(coin4);
	}
	
	@Test (expected = NullPointerSimulationException.class)
	public void testNullCoin() throws DisabledException, CashOverloadException{
		Coin coin5 = null;
		testPayViaCoin.coinAdded(coin5);
	}
	
	@Test (expected = NullPointerSimulationException.class)
	public void testNullCoins() throws DisabledException, CashOverloadException{
		Coin coin5 = null;
		Coin[] testCoins = new Coin[] {coin1, coin5};
		testPayViaCoin.coinsAdded(testCoins);
	}
	
	@Test
	public void testPayOneCoin() throws DisabledException, CashOverloadException{
		testPayViaCoin.coinAdded(coin1);
		BigDecimal testAmountDue = new BigDecimal("0.74");
		BigDecimal testAmountPaid = new BigDecimal("0.01");
		BigDecimal testChaingeOwed = BigDecimal.ZERO;
		assertEquals(testPayViaCoin.getAmountDue(), testAmountDue);
		assertEquals(testPayViaCoin.getAmountPaid(), testAmountPaid);
		assertEquals(testPayViaCoin.getChangeOwed(), testChaingeOwed);
	}
	
	@Test
	public void testPayTwoCoins() throws DisabledException, CashOverloadException{
		testPayViaCoin.coinAdded(coin1);
		testPayViaCoin.coinAdded(coin2);
		BigDecimal testAmountDue = new BigDecimal("0.64");
		BigDecimal testAmountPaid = new BigDecimal("0.11");
		BigDecimal testChaingeOwed = BigDecimal.ZERO;
		assertEquals(testPayViaCoin.getAmountDue(), testAmountDue);
		assertEquals(testPayViaCoin.getAmountPaid(), testAmountPaid);
		assertEquals(testPayViaCoin.getChangeOwed(), testChaingeOwed);
	}
	
	@Test
	public void testPayCoins() throws DisabledException, CashOverloadException{
		Coin[] coins = new Coin[]{coin1, coin2};
		testPayViaCoin.coinsAdded(coins);
		BigDecimal testAmountDue = new BigDecimal("0.64");
		BigDecimal testAmountPaid = new BigDecimal("0.11");
		BigDecimal testChaingeOwed = BigDecimal.ZERO;
		assertEquals(testPayViaCoin.getAmountDue(), testAmountDue);
		assertEquals(testPayViaCoin.getAmountPaid(), testAmountPaid);
		assertEquals(testPayViaCoin.getChangeOwed(), testChaingeOwed);
	}
	
	@Test
	public void testPayFullAmmount() throws DisabledException, CashOverloadException{
		testPayViaCoin.coinAdded(coin4);
		BigDecimal testAmountDue = BigDecimal.ZERO;
		BigDecimal testAmountPaid = new BigDecimal("1.00");
		BigDecimal testChaingeOwed = new BigDecimal("0.25");
		assertEquals(testPayViaCoin.getAmountDue(), testAmountDue);
		assertEquals(testPayViaCoin.getAmountPaid(), testAmountPaid);
		assertEquals(testPayViaCoin.getChangeOwed(), testChaingeOwed);
		assertEquals(testPayViaCoin.isAmountPaid(), true);
	}

	@Test
	public void testOverPaying() throws DisabledException, CashOverloadException{
		Coin[] coins = new Coin[]{coin4, coin1};
		testPayViaCoin.coinsAdded(coins);
		BigDecimal testAmountDue = BigDecimal.ZERO;
		BigDecimal testAmountPaid = new BigDecimal("1.01");
		BigDecimal testChaingeOwed = new BigDecimal("0.26");
		assertEquals(testPayViaCoin.getAmountDue(), testAmountDue);
		assertEquals(testPayViaCoin.getAmountPaid(), testAmountPaid);
		assertEquals(testPayViaCoin.getChangeOwed(), testChaingeOwed);
	}
	
	@Test (expected = CashOverloadException.class)
	public void testOverliadingSink() throws DisabledException, CashOverloadException{
		testPayViaCoin.coinAdded(coin1);
		testPayViaCoin.coinAdded(coin1);
		testPayViaCoin.coinAdded(coin1);
		testPayViaCoin.coinAdded(coin1);
	}
	
	@Test (expected = InvalidStateSimulationException.class)
	public void testAddingCoinWithDiscrepancyPresent() throws DisabledException, CashOverloadException{
		Numeral num1 = Numeral.valueOf((byte)0);
		Numeral num2 = Numeral.valueOf((byte)1);
		Numeral[] numArray1 = new Numeral[]{num1, num2};
		Numeral[] numArray2 = new Numeral[]{num2, num1};
		Barcode barcode1 = new Barcode(numArray1);
		Barcode barcode2 = new Barcode(numArray2);
		Mass itemMass1 = new Mass(1000);
		Mass itemMass2 = new Mass(100000);
		BarcodedItem testBarcodedItem1 = new BarcodedItem(barcode1, itemMass1);
		BarcodedItem testBarcodedItem2 = new BarcodedItem(barcode2, itemMass2);
		testPayViaCoin.weightErrorDetector.addItemToOrder(testBarcodedItem1);
		// Will case discrepancy
		scale.addAnItem(testBarcodedItem2);
		// Adding coin to a blocked session will throw exception
		testPayViaCoin.coinAdded(coin1);
	}
	
	
	
	// Stubs
	// Stub for sink
	public class SinkStub implements Sink<Coin> {
		public int coinCount = 0;
		public int coinCountLimit = 5;
		public int spaceLimit = 5;
		
		@Override
		public void receive(Coin cash) throws CashOverloadException, DisabledException {
			if (coinCount >= coinCountLimit) {
				throw new CashOverloadException();
			}
			coinCount = coinCount + 1;
		}

		@Override
		public boolean hasSpace() {
			if (coinCount < spaceLimit) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	// Stub for the coin validator observer
	public class CoinValidatorObserverStub implements CoinValidatorObserver {
		public boolean enabled;
		public boolean turnedOn;
		public boolean validCoinDetected = false;
		public boolean invalidCoinDetected = false;
		@Override
		public void enabled(IComponent<? extends IComponentObserver> component) {
			enabled = true;
		}
		@Override
		public void disabled(IComponent<? extends IComponentObserver> component) {
			enabled = false;
		}
		@Override
		public void turnedOn(IComponent<? extends IComponentObserver> component) {
			turnedOn = true;
		}
		@Override
		public void turnedOff(IComponent<? extends IComponentObserver> component) {
			turnedOn = false;
		}
		@Override
		public void validCoinDetected(CoinValidator validator, BigDecimal value) {
			validCoinDetected = true;
		}
		@Override
		public void invalidCoinDetected(CoinValidator validator) {
			invalidCoinDetected = true;
		}
	}
	
	// Stub for the coin storage observer
	public class CoinStorageUnitObserverStub implements CoinStorageUnitObserver {
		public boolean enabled;
		public boolean turnedOn;
		public boolean full;
		public int coinTracker = 0;
		public boolean coinsLoaded = false;
		public int coinsLoadedTracker = 0;
		public boolean coinsUnloaded = false;
		public int coinsUnloadedTracker = 0;
		@Override
		public void enabled(IComponent<? extends IComponentObserver> component) {
			enabled = true;
		}
		@Override
		public void disabled(IComponent<? extends IComponentObserver> component) {
			enabled = false;
		}
		@Override
		public void turnedOn(IComponent<? extends IComponentObserver> component) {
			turnedOn = true;
		}
		@Override
		public void turnedOff(IComponent<? extends IComponentObserver> component) {
			turnedOn = false;
		}
		@Override
		public void coinsFull(CoinStorageUnit unit) {
			full = true;
		}
		@Override
		public void coinAdded(CoinStorageUnit unit) {
			coinTracker = coinTracker + 1;
		}
		@Override
		public void coinsLoaded(CoinStorageUnit unit) {
			coinsLoaded = true;
			coinsLoadedTracker++;
		}
		@Override
		public void coinsUnloaded(CoinStorageUnit unit) {
			coinsUnloaded = true;
			coinsUnloadedTracker++;
		}
	}
}