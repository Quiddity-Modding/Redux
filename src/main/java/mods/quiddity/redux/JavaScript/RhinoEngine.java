package mods.quiddity.redux.JavaScript;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class RhinoEngine implements JavascriptEngine {
    private ScriptEngine rhinoEngine;

    @Override
    public void init() {
        if (rhinoEngine != null)
            killEngine();
        rhinoEngine = ReduxJavascriptEngine.engineManager.getEngineByName("rhino");
        if (rhinoEngine == null)
            throw new AssertionError("Error making the Rhino Engine!");
    }

    @Override
    public void loadScript(final String script) throws ScriptException {
        rhinoEngine.eval(script);
    }

    @Override
    public void killEngine() {
        rhinoEngine = null;
    }

    @Override
    public void restartEngine() {
        init();
    }

    @Override
    public void addJavaObject(String name, Object object) {
        rhinoEngine.put(name, object);
    }

    @Override
    public boolean hasMethod(String name) {
        return rhinoEngine.get(name) != null;
    }

    @Override
    public void callMethod(String name, Object... args) throws ScriptException, NoSuchMethodException {
        if (rhinoEngine instanceof Invocable) {
            Invocable invokableEngine = (Invocable) rhinoEngine;
            invokableEngine.invokeFunction(name, args);
        } else {
            throw new AssertionError("Your Rhino engine cannot invoke javascript functions! Redux will not function.");
        }
    }
}
