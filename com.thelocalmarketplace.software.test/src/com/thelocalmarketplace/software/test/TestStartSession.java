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
	private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
	
	@Before
	public void setup() {
		testStartSession = new StartSession();
		System.setOut(new PrintStream(outContent));
	}
	@Test
	public void testGetterMethod() {
		assertEquals(testStartSession.isSessionActive(), isSessionActive);
	}
	@Test
	public void activeSessionHandleTouch() {
		testStartSession.handleTouch();
		testStartSession.handleTouch();
		assertTrue(outContent.toString().contains("Session already started."));
	}
	@Test
	public void InactiveSessionHandleTouch() {
		testStartSession.handleTouch();
		assertTrue(testStartSession.isSessionActive());
		assertEquals("Session started. Ready for further customer interaction.\n", outContent.toString());		
	}
}
