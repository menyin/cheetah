package com.caisheng.cheetah.common.condition;

import com.caisheng.cheetah.api.common.Condition;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.util.Map;

public class ScriptCondition implements Condition {
    private static final ScriptEngineManager scriptEngineManager=new ScriptEngineManager();
    private static final ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("js");
    private final String script;

    public ScriptCondition(String script) {
        this.script = script;
    }

    @Override
    public boolean test(Map<String, Object> map) {
        try {
            return (boolean) scriptEngine.eval(script, new SimpleBindings(map));
        } catch (ScriptException e) {
            return false;
        }
    }
}
