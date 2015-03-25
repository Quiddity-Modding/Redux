package mods.quiddity.redux.Engines.JavaScript;

import mods.quiddity.redux.Engines.Engine;
import mods.quiddity.redux.Engines.ReduxAPI;
import mods.quiddity.redux.json.model.Pack;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ReduxJavascriptEngine implements Engine {
    public static final ScriptEngineManager engineManager = new ScriptEngineManager();
    private ScriptEngine engine;
    private final Pack packRefrence;

    public ReduxJavascriptEngine(Pack pack) {
        this.packRefrence = pack;
    }

    @Override
    public void init() {
        if (engineManager.getEngineByName("nashorn") != null) {
            engine = engineManager.getEngineByName("nashorn");
        } else if (engineManager.getEngineByName("rhino") != null) {
            engine = engineManager.getEngineByName("rhino");
        } else if (engineManager.getEngineByName("javascript") != null) {
            engine = engineManager.getEngineByName("javascript");
        }
        if (engine == null) {
            throw new AssertionError("Your Java Runtime Environment does not have a JSR-223 Javascript runtime!");
        }
        addJavaObject("ReduxAPI", new ReduxAPI(this));
    }

    @Override
    public void loadScript(final String scriptName, InputStream scriptInput) throws ScriptException {
        engine.eval(new InputStreamReader(scriptInput));
    }

    @Override
    public void killEngine() {
        engine = null;
    }

    @Override
    public void restartEngine() {
        init();
    }

    @Override
    public void addJavaObject(String name, Object object) {
        engine.put(name, object);
    }

    @Override
    public boolean respondsToMethod(String name) {
        return globalObjectExists(name);
    }

    @Override
    public boolean globalObjectExists(String name) {
        return engine.get(name) != null;
    }

    @Override
    public void callMethod(String name, Object... args) throws ScriptException, NoSuchMethodException {
        if (engine instanceof Invocable) {
            Invocable invokableEngine = (Invocable) engine;
            invokableEngine.invokeFunction(name, args);
        } else {
            throw new AssertionError("Your Rhino engine cannot invoke javascript functions! Redux will not function.");
        }
    }

    @Override
    public Object getJavaObject(String name, Class<?> javaType) {
        return engine.get(name);
    }

    @Override
    public Pack getPackReference() {
        return packRefrence;
    }

}
