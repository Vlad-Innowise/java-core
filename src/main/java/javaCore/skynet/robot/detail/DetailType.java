package javaCore.skynet.robot.detail;

public enum DetailType {

    HEAD("Head"),
    TORSO("Torso"),
    HAND("Hand"),
    FEET("Feet");

    private final String name;

    DetailType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
