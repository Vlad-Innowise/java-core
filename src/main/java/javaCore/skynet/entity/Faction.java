package javaCore.skynet.entity;

import javaCore.skynet.robot.Robot;

import java.util.List;

public interface Faction extends Runnable{

    List<Robot> goToWar();

}
