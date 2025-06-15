package javaCore.skynet.util;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class DaySynchronizer implements DaySyncronizer {

    private static int currentDay;
    private final Lock lock;
    private final Condition nightCame;
    private final CountDownLatch startSimulation;
    private final CyclicBarrier startNewDay;
    private final int days;
    private final int numberOfParties;
    private CountDownLatch readyForNight;
    private CountDownLatch readyForNewDay;
    private volatile boolean isNight;
    private volatile boolean simulationStarted;
    private volatile boolean simulationIsOver;


    public DaySynchronizer(int days, int numberOfParties) {
        this.days = days;
        this.numberOfParties = numberOfParties;
        this.lock = new ReentrantLock();
        this.nightCame = lock.newCondition();
        this.startNewDay = new CyclicBarrier(numberOfParties + 1);
        this.startSimulation = new CountDownLatch(1);
        this.readyForNight = new CountDownLatch(numberOfParties);
        this.readyForNewDay = new CountDownLatch(numberOfParties);
    }

    @Override
    public void run() {

        startingSimulation();

        for (int i = 0; i <= days; i++) {

            try {
                log.info("Day Syncronizer: Waiting for all parties ready for the night");
                readyForNight.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Day Syncronizer: Night came phase error: ", e);
                throw new RuntimeException(e);
            }

            lock.lock();
            try {
                log.info("Day Syncronizer: Night came: Notifying all parties");
                isNight = true;
                nightCame.signalAll();
            } finally {
                lock.unlock();
            }

            try {
                log.info("Day Syncronizer: Waiting for all parties finish night activities");
                readyForNewDay.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Day Syncronizer: Ready for the new day phase error: ", e);
                throw new RuntimeException(e);
            }

            lock.lock();
            try {
                log.info("Day Syncronizer: Preparations for the new day");

                readyForNight = new CountDownLatch(numberOfParties);
                log.debug("Day Syncronizer: Set a new ready for the Night sync countdown for parties: {}",
                          numberOfParties);

                readyForNewDay = new CountDownLatch(numberOfParties);
                log.debug("Day Syncronizer: Set a new ready for the New Day sync countdown for parties: {}",
                          numberOfParties);

                isNight = false;
                log.debug("Day Syncronizer: Set isNight flag to: {}", isNight);

                log.info("Day Syncronizer: Preparations for the new day are finished");
                startNewDay.await();

                log.info("Day Syncronizer: Current day [{}] is finished", currentDay);
                currentDay++;

                if (i == (days - 1)) {
                    simulationIsOver = true;
                    break;
                }

                log.info("Day Syncronizer: Day: [{}] started!", currentDay);

            } catch (InterruptedException | BrokenBarrierException e) {
                Thread.currentThread().interrupt();
                log.error("Day Syncronizer: Starting a new day phase error: ", e);
                throw new RuntimeException(e);
            } finally {
                lock.unlock();
            }

            log.info("Day Syncronizer: Finished simulation at the day [{}]", currentDay);

        }

    }

    private void startingSimulation() {
        log.info("Day Syncronizer: Starting simulation");
        lock.lock();
        try {
            log.debug("Day Syncronizer: Trying to catch a lock");
            log.debug("Day Syncronizer: Caught a lock");

            log.info("Day Syncronizer: Starting a simulation");
            startSimulation.countDown();
            simulationStarted = true;
            currentDay = 1;

            log.info("Day Syncronizer: Simulation started");
        } finally {
            log.debug("Day Syncronizer: Releasing a lock");
            lock.unlock();
        }
    }

    @Override
    public Lock getLock() {
        return lock;
    }

    @Override
    public Condition nightCame() {
        return nightCame;
    }

    @Override
    public int currentDay() {
        return currentDay;
    }

    @Override
    public CountDownLatch readyForNight() {
        return readyForNight;
    }

    @Override
    public boolean isNight() {
        return isNight;
    }

    @Override
    public CountDownLatch startSimulation() {
        return startSimulation;
    }

    @Override
    public boolean isSimulationStarted() {
        return simulationStarted;
    }

    @Override
    public boolean isSimulationOver() {
        return simulationIsOver;
    }

    @Override
    public CountDownLatch readyForNewDay() {
        return readyForNewDay;
    }

    @Override
    public CyclicBarrier startNewDay() {
        return startNewDay;
    }
}
