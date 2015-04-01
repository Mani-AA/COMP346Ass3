/**
 * Class Monitor
 * To synchronize dining philosophers.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 * 
 * Submission by: Michael Bilinsky 26992358
 * COMP 346 Assignment 3
 */
public class Monitor
{
	/*
	 * ------------
	 * Data members
	 * ------------
	 */

	//chopsticks around the table
	Chopstick[] chopsticks;
	//number of philosophers
	int noP;
	boolean someonetalking;
		
	/**
	 * Class to represent a chopstick
	 * Stores whether it is picked up and the
	 * philosopher who last picked it up
	 * @author Michael
	 *
	 */
	private class Chopstick
	{
		public boolean pickedUp;
		public int lastPickedUpBy;
		
		public Chopstick()
		{
			pickedUp = false;
			lastPickedUpBy = 0;
		}
		
		/**
		 * Determines whether a chopstick was last picked up by a philosopher
		 * @param piTID Thread ID of the philosopher
		 * @return Whether the philosopher last picked by the chopstick
		 */
		public boolean lastPickedUpByMe(final int piTID)
		{
			return lastPickedUpBy == piTID;
		}
		
		/**
		 * Determines whether a chopstick is currently picked up by another philosopher 
		 * @param piTID Thread ID of the philosopher
		 * @return True if the chopstick is picked up and the philosopher ID does not match
		 */
		public boolean pickedUpByAnother(final int piTID)
		{
			return lastPickedUpBy != piTID && pickedUp;
		}
		
		/**
		 * Pick up a chopstick and set the new philosopher
		 * @param piTID  Thread ID of the philosopher
		 */
		public void pickUp(final int piTID)
		{
			pickedUp = true;
			lastPickedUpBy = piTID;
		}
		
		/**
		 * Put a chopstick down
		 */
		public void putDown()
		{
			pickedUp = false;
		}
	}
	
	/**
	 * Constructor
	 */
	public Monitor(int piNumberOfPhilosophers)
	{
		noP = piNumberOfPhilosophers;
		chopsticks = new Chopstick[piNumberOfPhilosophers];
		
		//initialize all the chopsticks
		for(int i = 0; i < chopsticks.length; i++)
		{
			chopsticks[i] = new Chopstick();
		}
		
	}

	/*
	 * -------------------------------
	 * User-defined monitor procedures
	 * -------------------------------
	 */

	/**
	 * Grants request (returns) to eat when both chopsticks/forks are available.
	 * Else forces the philosopher to wait()
	 */
	public synchronized void pickUp(final int piTID)
	{
		//chopstick id of a corresponding philosopher Thread ID
		int pid = piTID-1;
		
		while(true)
		{
			if(chopsticks[pid].pickedUpByAnother(piTID) || chopsticks[(pid + 1) % noP].pickedUpByAnother(piTID)) 
			{
				//atleast one chopstick is picked up by someone else 
				//prioritize hungry philosophers by allowing philosophers who were not
				//the last users of available chopsticks to pick them up while waiting
				//for the other chopstick to become available.
				if(!chopsticks[pid].pickedUp && !chopsticks[pid].lastPickedUpByMe(piTID))
				{
					chopsticks[pid].pickUp(piTID);
				} 
				else if(!chopsticks[(pid + 1) % noP].pickedUp && !chopsticks[(pid + 1) % noP].lastPickedUpByMe(piTID))
				{
					chopsticks[pid].pickUp(piTID);
				}
			} 
			else
			{
				//both chopsticks are available to you, pick them up (even though you may already have them) and leave the loop
				chopsticks[pid].pickUp(piTID);
				chopsticks[(pid + 1) % noP].pickUp(piTID);
				break;
			}
			
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println("Monitor.pickUp():");
				DiningPhilosophers.reportException(e);
				System.exit(1);
			}
		}
	}

	/**
	 * When a given philosopher's done eating, they put the chopstiks/forks down
	 * and let others know they are available.
	 */
	public synchronized void putDown(final int piTID)
	{
		//put down chopsticks
		chopsticks[piTID - 1].putDown();
		chopsticks[(piTID) % noP].putDown();
		
		notifyAll();
	}

	/**
	 * Only one philopher at a time is allowed to philosophy
	 * (while she is not eating).
	 */
	public synchronized void requestTalk()
	{
		//wait while someone is talking
		while(someonetalking) 
		{
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println("Monitor.requestTalk():");
				DiningPhilosophers.reportException(e);
				System.exit(1);
			}
		}
		
		//requester is now talking
		someonetalking = true;
	}

	/**
	 * When one philosopher is done talking stuff, others
	 * can feel free to start talking.
	 */
	public synchronized void endTalk()
	{
		//requester is nolonger talking
		someonetalking = false;
		notifyAll();
	}
}

// EOF
