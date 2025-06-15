package javaCore.skynet.robot;

import javaCore.skynet.robot.detail.Feet;
import javaCore.skynet.robot.detail.Hand;
import javaCore.skynet.robot.detail.Head;
import javaCore.skynet.robot.detail.Torso;

public record RobotImpl(String id, Head head, Torso torso, Hand hand, Feet feet) implements Robot {
}
