package com.caisheng.cheetah.common.condition;

import com.caisheng.cheetah.api.common.Condition;

import java.util.Map;

public class AwaysPassCondition implements Condition{
    public static AwaysPassCondition I = new AwaysPassCondition();

    @Override
    public boolean test(Map<String, Object> map) {
        return true;
    }
}
