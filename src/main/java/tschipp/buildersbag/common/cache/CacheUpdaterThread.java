package tschipp.buildersbag.common.cache;

import java.util.ArrayDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CacheUpdaterThread extends Thread
{

	public Lock lock = new ReentrantLock();
	public Condition needsProgress = lock.newCondition();

	public CacheUpdaterThread(String name)
	{
		super(name);
	}

	private ArrayDeque<Runnable> queue = new ArrayDeque<Runnable>();

	public void enqueueRunnable(Runnable run)
	{
		queue.push(run);

		lock.lock();
		needsProgress.signalAll();
		lock.unlock();
	}

	@Override
	public void run()
	{
		while (true)
		{
			try
			{
				lock.lock();
				needsProgress.await();
				lock.unlock();
			}
			catch (InterruptedException e1)
			{
			}

			if (!queue.isEmpty())
			{
				Runnable run = queue.poll();
				if (run != null)
					run.run();
			}
		}
	}

}
