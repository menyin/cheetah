package com.caisheng.cheetah.common.qps;

import java.util.Objects;

public interface FlowControl {
    void reset();

    int total();

    boolean checkQps() throws OverFlowException;

    default void end(Object results) {
    }

    long getDelay();

    int qps();

    String report();
}
