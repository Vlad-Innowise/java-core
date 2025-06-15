package javaCore.skynet.robot;

import javaCore.skynet.robot.detail.Detail;
import javaCore.skynet.robot.detail.DetailType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public interface RobotFactory {

    List<Robot> assemble(String serialName, AtomicInteger serialCounter, Map<DetailType, List<Detail>> parts);

}
