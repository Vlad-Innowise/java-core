package javaCore.skynet;

import javaCore.skynet.entity.DetailFactoryImpl;
import javaCore.skynet.entity.Faction;
import javaCore.skynet.entity.Factory;
import javaCore.skynet.entity.Wednesday;
import javaCore.skynet.entity.World;
import javaCore.skynet.robot.Robot;
import javaCore.skynet.robot.RobotFactory;
import javaCore.skynet.robot.RobotFactoryImpl;
import javaCore.skynet.util.DaySynchronizer;
import javaCore.skynet.util.DaySyncronizer;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class SkynetRunner {

    public static final String WEDNESDAY = "Wednesday";
    public static final String World = "World";
    public static final String FACTORY = "Factory";

    public static void main(String[] args) {

        log.info("Skynet Runner: Init phase");

        int numberOfParties = 3;
        int daysToSimulate = 100;

        DaySyncronizer daySyncronizer = new DaySynchronizer(daysToSimulate, numberOfParties);

        RobotFactory robotFactory = new RobotFactoryImpl();

        Factory detailFactory = new DetailFactoryImpl(daySyncronizer, 10);
        Faction wednesdays = new Wednesday(daySyncronizer, detailFactory, robotFactory, 5);
        Faction world = new World(daySyncronizer, detailFactory, robotFactory, 5);

        List<Runnable> tasks = new ArrayList<>();
        tasks.add(daySyncronizer);
        tasks.add(detailFactory);
        tasks.add(wednesdays);
        tasks.add(world);

        log.info("Skynet Runner: Execution phase");
        try (ExecutorService executorService = Executors.newFixedThreadPool(tasks.size())) {
            tasks.forEach(executorService::submit);
        }

        log.info("Skynet Runner: Control phase");

        checkResults(wednesdays, world);

    }

    public static void checkResults(Faction wednesdays, Faction world) {

        List<Robot> wednesdayRobots = wednesdays.goToWar();
        List<Robot> worldRobots = world.goToWar();

        log.info("Skynet Runner: Control phase: Wednesday robots size: {}", wednesdayRobots.size());
        log.info("Skynet Runner: Control phase: World robots size: {}", worldRobots.size());

        if (wednesdayRobots.size() > worldRobots.size()) {
            log.info("Skynet Runner: Control phase: Wednesday Faction wins!");
        } else if (wednesdayRobots.size() < worldRobots.size()) {
            log.info("Skynet Runner: Control phase: World Faction wins!");
        } else {
            log.info("Skynet Runner: Control phase: Deuce!");
        }

    }

}
