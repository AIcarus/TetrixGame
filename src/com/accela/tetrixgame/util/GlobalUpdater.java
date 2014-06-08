package com.accela.tetrixgame.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * 游戏中，每场游戏，以及每个Bot，都需要不断刷新，才能使游戏进行下去。 这样就会产生大量用于刷新的线程。其实刷新工作全部都可以交给一个线程
 * 来做，这个类就是那个线程。
 * 
 */
public class GlobalUpdater extends Thread {
	private static final GlobalUpdater staticInstance = new GlobalUpdater();

	static {
		staticInstance.start();
	}

	public static final long UPDATE_INTERVAL = 20;

	/**
	 * 
	 * 刷新任务必须实现这个接口。GlobalUpdater会重复地调用 run方法，直到isOver()返回true。
	 * 
	 */
	public static interface UpdateRunnable extends Runnable {
		public boolean isOver();
	}

	public static void submitTask(UpdateRunnable task)
			throws InterruptedException {
		assert (staticInstance != null);
		assert (staticInstance.isAlive());

		staticInstance.submit(task);
	}

	public static void submitTaskUnBlocked(UpdateRunnable task) {
		assert (staticInstance != null);
		assert (staticInstance.isAlive());

		staticInstance.submitUnBlocked(task);
	}

	private List<UpdateRunnable> taskList = Collections
			.synchronizedList(new LinkedList<UpdateRunnable>());

	private GlobalUpdater() {
		super("Static - GlobalUpdater");
	}

	/**
	 * 提交任务。 提交任务者会一直等待，直到任务完成。 用来更新的任务，应该是不会阻塞的，因为这会影响会须任务的执行。
	 * 
	 * @param task
	 * @throws InterruptedException
	 */
	public void submit(UpdateRunnable task) throws InterruptedException {
		if (null == task) {
			throw new NullPointerException("task should not be null");
		}
		if (taskList.contains(task)) {
			throw new IllegalArgumentException("the task has been added before");
		}

		taskList.add(task);
		try {
			synchronized (task) {
				task.wait();
			}
		} finally {
			taskList.remove(task);
		}
	}

	public void submitUnBlocked(UpdateRunnable task) {
		if (null == task) {
			throw new NullPointerException("task should not be null");
		}
		if (taskList.contains(task)) {
			throw new IllegalArgumentException("the task has been added before");
		}

		taskList.add(task);
	}

	public void run() {
		while (true) {
			try {
				long start = System.currentTimeMillis();
				synchronized (taskList) {
					Iterator<UpdateRunnable> itr = taskList.iterator();
					while (itr.hasNext()) {
						UpdateRunnable runnable = itr.next();

						assert (runnable != null);

						try {
							if (runnable.isOver()) {
								synchronized (runnable) {
									runnable.notifyAll();
								}

								itr.remove();

								continue;
							}

							runnable.run();

						} catch (Exception ex) {
							ex.printStackTrace();
							assert (false);
						}

					}
				}
				long end = System.currentTimeMillis();

				try {
					Thread.sleep(Math.max(0, (UPDATE_INTERVAL - (end - start))));
				} catch (InterruptedException ex) {
					ex.printStackTrace();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

}
