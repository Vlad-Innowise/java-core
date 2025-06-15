package javaCore.skynet.robot.detail;

public record Torso(String id) implements Detail {

    @Override
    public DetailType getDetailType() {
        return DetailType.TORSO;
    }

}
