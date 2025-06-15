package javaCore.skynet.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public interface DaySyncronizer extends Runnable {

    Lock getLock();

    Condition nightCame();

    int currentDay();

    CountDownLatch readyForNight();

    boolean isNight();

    CountDownLatch startSimulation();

    boolean isSimulationStarted();

    boolean isSimulationOver();

    CountDownLatch readyForNewDay();

    CyclicBarrier startNewDay();

}
