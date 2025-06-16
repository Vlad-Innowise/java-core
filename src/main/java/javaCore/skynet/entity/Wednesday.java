package javaCore.skynet.entity;

import javaCore.skynet.robot.Robot;
import javaCore.skynet.robot.RobotFactory;
import javaCore.skynet.robot.detail.Detail;
import javaCore.skynet.robot.detail.DetailType;
import javaCore.skynet.util.DaySyncronizer;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class Wednesday implements Faction {

    public static final String SERIAL_NAME = "WednesdayR";
    private final DaySyncronizer daySyncronizer;
    private final Factory detailFactory;
    private final RobotFactory robotFactory;
    private final Map<DetailType, List<Detail>> details;
    private final List<Robot> robots;
    private final AtomicInteger robotsCounter;
    private int maxDetailsPerDay;

    {
        details = Arrays.stream(DetailType.values())
                        .collect(Collectors.toMap(Function.identity(),
                                                  dt -> new ArrayList<>()));
        robots = new ArrayList<>();
    }

    public Wednesday(DaySyncronizer daySyncronizer, Factory detailFactory, RobotFactory robotFactory,
                     int maxDetailsPerDay) {
        this.daySyncronizer = daySyncronizer;
        this.detailFactory = detailFactory;
        this.robotFactory = robotFactory;
        this.maxDetailsPerDay = maxDetailsPerDay;
        robotsCounter = new AtomicInteger(0);
    }

    @Override
    public void run() {

        while (!daySyncronizer.isSimulationOver()) {

            simulationStartedPhase();

            dayPhase();

            nightPhase();
        }

        log.info("Wednesday: simulation finished");

    }

    private void nightPhase() {

        log.info("Wednesday: Starting taking details from Factory");
        takingDetails();
        log.info("Wednesday: Finished taking details from Factory");

        log.info("Wednesday: Assembling robots in the Robot Factory");
        List<Robot> assembled = robotFactory.assemble(SERIAL_NAME, robotsCounter, details);
        robots.addAll(assembled);
        log.info("Wednesday: Assembled: {} robots", assembled.size());

        try {
            log.info("Wednesday: Finished all night activities: Ready for a new day countdown");
            daySyncronizer.readyForNewDay().countDown();

            log.info("Wednesday: Awaiting at the Start new day barrier");
            int currentDay = daySyncronizer.currentDay();
            daySyncronizer.startNewDay().await();

            log.debug("Wednesday: Passed barrier: finished [{}] day", currentDay);
        } catch (InterruptedException | BrokenBarrierException e) {
            Thread.currentThread().interrupt();
            log.error("Wednesday: Starting a new day phase error: ", e);
            throw new RuntimeException(e);
        }

    }

    private void takingDetails() {
        int taken = 0;
        while (taken < maxDetailsPerDay) {
            Detail detail = detailFactory.takeDetail();
            taken++;
            log.info("Took {} detail: {}", taken, detail);

            log.debug("Wednesday: Adding a detail {} to inner storage", detail);
            details.computeIfPresent(detail.getDetailType(), (k, v) -> {
                v.add(detail);
                return v;
            });
        }
    }

    private void dayPhase() {
        Lock lock = daySyncronizer.getLock();
        lock.lock();
        log.debug("Wednesday: Day: [{}] started!", daySyncronizer.currentDay());
        try {

            log.info("Wednesday: Finished all day activities: Ready for a night countdown");
            daySyncronizer.readyForNight().countDown();

            while (!daySyncronizer.isNight()) {

                log.info("Wednesday: Awaiting at the night came condition");
                daySyncronizer.nightCame().await();

                log.info("Wednesday: Night Came: Awakening");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<Robot> goToWar() {
        return robots;
    }

    private void simulationStartedPhase() {
        while (!daySyncronizer.isSimulationStarted()) {
            try {
                log.info("Wednesday: Waiting for the simulation to be started");
                daySyncronizer.startSimulation().await();
                log.info("Wednesday: Simulation started: Awakening!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Wednesday: Simulation started phase error: ", e);
                throw new RuntimeException(e);
            }
        }
    }
}
