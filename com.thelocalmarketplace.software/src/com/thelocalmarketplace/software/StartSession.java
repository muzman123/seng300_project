//Names & UCID
//Arvin Bolbolanardestani 30165484
//Zeyad Elrayes 30161958
//Dvij Raval 30024340
//Muzammil Saleem 30180889
//Ryan Wong 30171793
//Danish Sharma 30172600

package com.thelocalmarketplace.software;

/**
 * This class handles the "Start Session" use case.
 * It provides methods to display the initial screen, handle the customer's touch, and manage the session state.
 */
public class StartSession {

    // A flag to track if a session is currently active.
    private boolean isSessionActive;

    /**
     * Default constructor initializes with no active session.
     */
    public StartSession() {
        this.isSessionActive = false;
    }

    /**
     * Checks if a session is currently active.
     * @return true if a session is active, false otherwise.
     */
    public boolean isSessionActive() {
        return isSessionActive;
    }

    /**
     * Displays the initial splash screen message.
     */
    public void displayInitialScreen() {
        System.out.println("Touch Anywhere to Start.");
    }

    /**
     * Handles the touch event from the customer.
     * Initiates a session if no session is currently active.
     */
    public void handleTouch() {
        if(!isSessionActive) {
            isSessionActive = true;
            System.out.println("Session started. Ready for further customer interaction.");
        } else {
            System.out.println("Session already started.");
        }
    }

    /**
     * Simulates the entire "Start Session" process.
     * Displays the splash screen and handles the touch event.
     */
    public void simulateStartSession() {
        displayInitialScreen();
        handleTouch();
    }

/**
 * The main method to start the simulation (For use in future implementations)
 */
    /*
    public static void main(String[] args) {
        // Create the StartSession handler.
        StartSession startSession = new StartSession();

        // Simulate the "Start Session" use case.
        startSession.simulateStartSession();
    }
    */
}