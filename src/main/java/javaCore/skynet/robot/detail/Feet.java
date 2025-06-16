package javaCore.skynet.robot.detail;

public record Feet(String id) implements Detail {

    @Override
    public DetailType getDetailType() {
        return DetailType.FEET;
    }
}
