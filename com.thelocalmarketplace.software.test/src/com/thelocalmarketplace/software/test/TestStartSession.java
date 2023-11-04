package com.thelocalmarketplace.software.test;
//Danish Sharma 30172600
import com.thelocalmarketplace.software.StartSession;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;


public class TestStartSession {
	private StartSession testStartSession;
	private boolean isSessionActive;
	//Using in-built Java function to check print steams.
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	//Initializing the test suite
	@Before
	public void setup() {
		testStartSession = new StartSession();
		System.setOut(new PrintStream(outContent));
	}
	//Testing basic getter method
	@Test
	public void testGetterMethod() {
		assertEquals(testStartSession.isSessionActive(), isSessionActive);
	}
	//Testing case where session is already active, and handleTouch is called again.
	@Test
	public void testActiveSessionHandleTouch() {
		testStartSession.handleTouch();
		testStartSession.handleTouch();
		assertTrue(outContent.toString().contains("Session already started."));
	}
	/*Testing case where session isn't active, and handleTouch is called, should print out a
 	different statement altogether.
	*/
	@Test
	public void testInactiveSessionHandleTouch() {
		//is SessionActive already initialized as false.
		testStartSession.handleTouch();
		//Tests if isSessionActive has been changed or not
		assertTrue(testStartSession.isSessionActive());
		//Finally, tests output stream.
		assertEquals("Session started. Ready for further customer interaction.\n", outContent.toString());		
	}
}
