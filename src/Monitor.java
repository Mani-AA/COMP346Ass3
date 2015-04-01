/**
 * Class Monitor
 * To synchronize dining philosophers.
 *
 * @author Serguei A. Mokhov, mokhov@cs.concordia.ca
 */
public class Monitor
{
	/*
	 * ------------
	 * Data members
	 * ------------
	 */

	Chopstick[] chopsticks;
	int noP;
	boolean someonetalking;
		
	private class Chopstick
	{
		public boolean pickedUp;
		public int lastPickedUpBy;
		
		public Chopstick()
		{
			pickedUp = false;
			lastPickedUpBy = 0;
		}
		
		public boolean pickedUpByMe(final int piTID)
		{
			return lastPickedUpBy == piTID && pickedUp;
		}
		
		public boolean lastPickedUpByMe(final int piTID)
		{
			return lastPickedUpBy == piTID;
		}
		
		public boolean pickedUpByAnother(final int piTID)
		{
			return lastPickedUpBy != piTID && pickedUp;
		}
		
		public void pickUp(final int piTID)
		{
			pickedUp = true;
			lastPickedUpBy = piTID;
		}
		
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
		int pid = piTID-1;
		
		while(true)
		{
			if(chopsticks[pid].pickedUpByAnother(piTID) || chopsticks[(pid + 1) % noP].pickedUpByAnother(piTID)) 
			{
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
			/*
			if(!chopsticks[pid].pickedUp && !chopsticks[(pid + 1) % noP].pickedUp)
			{
				chopsticks[pid].pickUp(piTID);
				chopsticks[(pid + 1) % 5].pickUp(piTID);
			}
			
			
			if(!chopsticks[pid].pickedUp && !chopsticks[pid].lastPickedUpByMe(piTID))
			{
				chopsticks[pid].pickUp(piTID);
			}
			
			if(!chopsticks[(pid + 1) % noP].pickedUp && !chopsticks[(pid + 1) % noP].lastPickedUpByMe(piTID))
			{
				chopsticks[(pid + 1) % noP].pickUp(piTID);
			}*/
		}
		
	}

	/**
	 * When a given philosopher's done eating, they put the chopstiks/forks down
	 * and let others know they are available.
	 */
	public synchronized void putDown(final int piTID)
	{
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
		
		someonetalking = true;
	}

	/**
	 * When one philosopher is done talking stuff, others
	 * can feel free to start talking.
	 */
	public synchronized void endTalk()
	{
		someonetalking = false;
		notifyAll();
	}
}

// EOF
