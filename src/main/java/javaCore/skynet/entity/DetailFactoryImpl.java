package javaCore.skynet.entity;

import javaCore.skynet.robot.detail.Detail;
import javaCore.skynet.robot.detail.DetailType;
import javaCore.skynet.robot.detail.Feet;
import javaCore.skynet.robot.detail.Hand;
import javaCore.skynet.robot.detail.Head;
import javaCore.skynet.robot.detail.Torso;
import javaCore.skynet.util.DaySyncronizer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@Slf4j
public class DetailFactoryImpl implements Factory {

    private final DaySyncronizer daySyncronizer;
    @Getter
    private final Map<DetailType, Integer> idGenerators;
    private final Map<DetailType, Supplier<Detail>> suppliedDetails;
    private final Random random = new Random();
    private final int detailsProductionPerDay;
    private final BlockingQueue<Detail> details;

    {
        idGenerators = Arrays.stream(DetailType.values())
                             .collect(Collectors.toMap(Function.identity(),
                                                       e -> 0));
        suppliedDetails = Map.of(
                DetailType.HEAD, () -> new Head(generateId(DetailType.HEAD)),
                DetailType.TORSO, () -> new Torso(generateId(DetailType.TORSO)),
                DetailType.HAND, () -> new Hand(generateId(DetailType.HAND)),
                DetailType.FEET, () -> new Feet(generateId(DetailType.FEET))
        );
    }

    public DetailFactoryImpl(DaySyncronizer daySyncronizer, int maxDetails) {
        this.daySyncronizer = daySyncronizer;
        this.detailsProductionPerDay = maxDetails;
        this.details = new ArrayBlockingQueue<>(maxDetails, true);
    }

    @Override
    public void run() {


        while (!daySyncronizer.isSimulationOver()) {

            simulationStartedPhase();

            dayPhase();

            nightPhase();
        }

        log.info("Detail Factory: Simulation Finished");

    }

    private void nightPhase() {

        try {
            log.info("Detail Factory: Finished all night activities: Ready for a new day countdown");
            daySyncronizer.readyForNewDay().countDown();

            log.info("Detail Factory: Awaiting at the Start new day barrier");
            int currentDay = daySyncronizer.currentDay();
            daySyncronizer.startNewDay().await();

            log.debug("Detail Factory: Passed barrier: finished [{}] day", currentDay);

        } catch (InterruptedException | BrokenBarrierException e) {
            Thread.currentThread().interrupt();
            log.error("Detail Factory: Starting a new day phase error: ", e);
            throw new RuntimeException(e);
        }
    }

    private void dayPhase() {
        Lock lock = daySyncronizer.getLock();
        lock.lock();
        log.debug("Detail Factory: Day: [{}] started!", daySyncronizer.currentDay());
        try {
            produceDetails();

            log.info("Detail Factory: Finished all day activities: Ready for a night countdown");
            daySyncronizer.readyForNight().countDown();

            while (!daySyncronizer.isNight()) {
                log.info("Detail Factory: Awaiting at the night came condition");
                daySyncronizer.nightCame().await();

                log.info("Detail Factory: Night Came: Awakening");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Detail takeDetail() {
        try {
            return details.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private void produceDetails(DetailType type) throws InterruptedException {
        for (int i = 0; i < detailsProductionPerDay; i++) {
            Detail detail = supplyDetail(suppliedDetails.get(type));
            details.put(detail);
        }
    }

    private void produceDetails() throws InterruptedException {
        log.info("Detail Factory: Started producing details");

        int detailNum = 0;
        for (int i = 0; i < detailsProductionPerDay; i++) {

            Detail detail = supplyRandomDetail();
            detailNum++;
            log.info("Detail Factory: Detail [{}] produced: {}", detailNum, detail);
            details.put(detail);
            log.debug("Detail Factory: Put detail [{}] into queue", detail.id());
        }

        log.info("Detail Factory: Finished producing details");
    }

    private Detail supplyRandomDetail() {
        List<DetailType> supportedDetails = Arrays.stream(DetailType.values()).toList();
        DetailType detailType = supportedDetails.get(random.nextInt(supportedDetails.size()));
        log.info("Detail Factory: Generated detail: {}", detailType.getName());
        return supplyDetail(suppliedDetails.get(detailType));
    }

    private Detail supplyDetail(Supplier<Detail> supplier) {
        return supplier.get();
    }

    public String generateId(DetailType type) {
        Integer id = idGenerators.compute(type, (k, v) -> (v == null) ? 0 : v + 1);
        return String.format("%s_%d", type.getName(), id);
    }

    private void simulationStartedPhase() {
        while (!daySyncronizer.isSimulationStarted()) {
            try {
                log.info("Detail Factor: Waiting for the simulation to be started");
                daySyncronizer.startSimulation().await();

                log.info("Detail Factor: Simulation started: Awakening!");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Detail Factor: Simulation started phase error: ", e);
                throw new RuntimeException(e);
            }
        }
    }

}
