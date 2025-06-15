package javaCore.skynet.robot.detail;

public record Hand(String id) implements Detail {

    @Override
    public DetailType getDetailType() {
        return DetailType.HAND;
    }
}
