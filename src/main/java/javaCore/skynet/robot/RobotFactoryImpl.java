package javaCore.skynet.robot;

import javaCore.skynet.robot.detail.Detail;
import javaCore.skynet.robot.detail.DetailType;
import javaCore.skynet.robot.detail.Feet;
import javaCore.skynet.robot.detail.Hand;
import javaCore.skynet.robot.detail.Head;
import javaCore.skynet.robot.detail.Torso;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Data
public class RobotFactoryImpl implements RobotFactory {

    private final Map<DetailType, Integer> requiredPerRobot = Map.of(
            DetailType.HEAD, 1,
            DetailType.TORSO, 1,
            DetailType.HAND, 1,
            DetailType.FEET, 1
    );

    @Override
    public List<Robot> assemble(String serialName, AtomicInteger serialCounter, Map<DetailType, List<Detail>> parts) {
        int maxRobotsToAssemble = countRobotsNCanBeAssembled(parts);
        return Stream.generate(() -> {
                         String id = assignId(serialName, serialCounter);
                         return buildRobot(id, parts);
                     })
                     .limit(maxRobotsToAssemble)
                     .toList();
    }

    private Robot buildRobot(String id, Map<DetailType, List<Detail>> parts) {

        Head head = (Head) parts.get(DetailType.HEAD).removeLast();
        Torso torso = (Torso) parts.get(DetailType.TORSO).removeLast();
        Hand hand = (Hand) parts.get(DetailType.HAND).removeLast();
        Feet feet = (Feet) parts.get(DetailType.FEET).removeLast();
        return new RobotImpl(id, head, torso, hand, feet);
    }

    private String assignId(String serialName, AtomicInteger serialCounter) {
        return String.format("%s_%d", serialName, serialCounter.incrementAndGet());
    }

    private int countRobotsNCanBeAssembled(Map<DetailType, List<Detail>> parts) {
        return parts.entrySet()
                    .stream()
                    .map(e -> {
                        DetailType type = e.getKey();
                        Integer reqPerRobot = requiredPerRobot.get(type);
                        int actualNofParts = e.getValue().size();
                        return reqPerRobot != 0 ? actualNofParts / reqPerRobot : 0;
                    }).min(Integer::compare)
                    .orElse(0);
    }

}
