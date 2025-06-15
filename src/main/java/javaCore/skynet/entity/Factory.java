package javaCore.skynet.entity;

import javaCore.skynet.robot.detail.Detail;

public interface Factory extends Runnable {

    Detail takeDetail();

}
