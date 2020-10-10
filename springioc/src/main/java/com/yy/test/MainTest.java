package com.yy.test;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author ：hzz
 * @description：TODO
 * @date ：2020/10/10 10:29
 */
public class MainTest {
    public static void main(String[] args) throws InterruptedException {
        ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap();
        concurrentHashMap.put("test", 11);


        Object test = concurrentHashMap.get("test");
        ReentrantLock lock = new ReentrantLock();
        new Thread(() -> {
            try {
                lock.lockInterruptibly();
                try {
                    System.out.println("获取到锁，线程");
                    Thread.sleep(50000);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Thread thread = new Thread(() -> {
            try {
                lock.lockInterruptibly();
                try {
                    System.out.println("获取到锁");
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });
        thread.start();

        Thread.sleep(1000);
        thread.interrupt();
    }
}
