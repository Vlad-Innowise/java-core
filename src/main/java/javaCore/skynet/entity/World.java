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
public class World implements Faction {

    public static final String SERIAL_NAME = "WorldR";
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

    public World(DaySyncronizer daySyncronizer, Factory detailFactory, RobotFactory robotFactory,
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

        log.info("World: simulation finished");

    }

    private void nightPhase() {

        log.info("World: Starting taking details from Factory");
        takingDetails();
        log.info("World: Finished taking details from Factory");

        log.info("World: Assembling robots in the Robot Factory");
        List<Robot> assembled = robotFactory.assemble(SERIAL_NAME, robotsCounter, details);
        robots.addAll(assembled);
        log.info("World: Assembled: {} robots", assembled.size());

        try {
            log.info("World: Finished all night activities: Ready for a new day countdown");
            daySyncronizer.readyForNewDay().countDown();

            log.info("World: Awaiting at the Start new day barrier");
            int currentDay = daySyncronizer.currentDay();
            daySyncronizer.startNewDay().await();

            log.debug("World: Passed barrier: finished [{}] day", currentDay);
        } catch (InterruptedException | BrokenBarrierException e) {
            Thread.currentThread().interrupt();
            log.error("World: Starting a new day phase error: ", e);
            throw new RuntimeException(e);
        }

    }

    private void takingDetails() {
        int taken = 0;
        while (taken < maxDetailsPerDay) {
            Detail detail = detailFactory.takeDetail();
            taken++;
            log.info("Took {} detail: {}", taken, detail);

            log.debug("World: Adding a detail {} to inner storage", detail);
            details.computeIfPresent(detail.getDetailType(), (k, v) -> {
                v.add(detail);
                return v;
            });
        }
    }

    private void dayPhase() {
        Lock lock = daySyncronizer.getLock();
        lock.lock();
        log.debug("World: Day: [{}] started!", daySyncronizer.currentDay());
        try {

            log.info("World: Finished all day activities: Ready for a night countdown");
            daySyncronizer.readyForNight().countDown();

            while (!daySyncronizer.isNight()) {

                log.info("World: Awaiting at the night came condition");
                daySyncronizer.nightCame().await();

                log.info("World: Night Came: Awakening");
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
                log.info("World: Waiting for the simulation to be started");
                daySyncronizer.startSimulation().await();
                log.info("World: Simulation started: Awakening!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("World: Simulation started phase error: ", e);
                throw new RuntimeException(e);
            }
        }
    }
}
