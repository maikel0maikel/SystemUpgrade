package com.sinohb.system.upgrade.pool;


import com.sinohb.logger.LogTools;

import java.util.LinkedList;
import java.util.List;

public class ThreadPool {
    private static final String TAG = "ThreadPool";
    private static ThreadPool pool = null;
    private static final int POOL_SIZE = 3;
    private List<Runnable> mWorkerQueue = new LinkedList<>();
    private Worker[] mWorkers;

    public static ThreadPool getPool() {
        if (pool == null) {
            synchronized (ThreadPool.class) {
                if (pool == null) {
                    pool = new ThreadPool();
                }
            }
        }
        return pool;
    }

    private ThreadPool() {
        mWorkers = new Worker[POOL_SIZE];
        for (int i = 0; i < POOL_SIZE; i++) {
            mWorkers[i] = new Worker();
            new Thread(mWorkers[i]).start();
        }
    }

    public void execute(Runnable r) {

        synchronized (mWorkerQueue) {
            mWorkerQueue.add(r);
            mWorkerQueue.notify();
        }
    }

    public void execute(List<Runnable> tasks) {
        synchronized (mWorkerQueue) {
            for (Runnable r : tasks) {
                mWorkerQueue.add(r);
            }
            mWorkerQueue.notify();
        }
    }

    public void execute(Runnable[] tasks) {
        synchronized (mWorkerQueue) {
            for (Runnable r : tasks) {
                mWorkerQueue.add(r);
            }
            mWorkerQueue.notify();
        }
    }

    public synchronized void destroy() {
        while (!mWorkerQueue.isEmpty()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (Worker worker : mWorkers) {
            worker.stopWork();
            worker = null;
        }
        mWorkerQueue.clear();
        pool = null;
    }

    private class Worker implements Runnable {
        private boolean isRun = true;

        @Override
        public void run() {
            Runnable r = null;
            LogTools.d(TAG,"pool开始工作："+Thread.currentThread().getName());
            while (isRun) {
                synchronized (mWorkerQueue) {
                    while (isRun && mWorkerQueue.isEmpty()) {
                        try {
                            mWorkerQueue.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if (!mWorkerQueue.isEmpty()) {
                        r = mWorkerQueue.remove(0);
                    }
                }
                if (r != null) {
                    r.run();
                }
                r = null;
            }
        }

        public void stopWork() {
            synchronized (mWorkerQueue) {
                isRun = false;
                mWorkerQueue.notifyAll();
            }
        }
    }


}
