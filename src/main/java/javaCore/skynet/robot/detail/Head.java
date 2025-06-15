package javaCore.skynet.robot.detail;

public record Head(String id) implements Detail {

    @Override
    public DetailType getDetailType() {
        return DetailType.HEAD;
    }

}
