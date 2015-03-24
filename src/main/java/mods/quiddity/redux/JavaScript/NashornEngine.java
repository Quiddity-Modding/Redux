package mods.quiddity.redux.JavaScript;

import jdk.nashorn.api.scripting.ClassFilter;
import jdk.nashorn.api.scripting.NashornScriptEngine;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptException;

public class NashornEngine implements JavascriptEngine {
    private NashornScriptEngine nashornScriptEngine;

    @Override
    public void init() {
        if (nashornScriptEngine != null)
            killEngine();
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        ccl = (ccl == null ? NashornScriptEngineFactory.class.getClassLoader() : ccl);
        nashornScriptEngine = (NashornScriptEngine) new NashornScriptEngineFactory().getScriptEngine(new String[]{"--no-java"}, ccl, new ReduxClassFilter());
    }

    @Override
    public void loadScript(String script) throws ScriptException {
        nashornScriptEngine.eval(script);
    }

    @Override
    public void killEngine() {
        nashornScriptEngine = null;
    }

    @Override
    public void restartEngine() {
        init();
    }

    @Override
    public void addJavaObject(String name, Object object) {
        nashornScriptEngine.put(name, object);
    }

    @Override
    public boolean hasMethod(String name) {
        return nashornScriptEngine.get(name) != null;
    }

    @Override
    public void callMethod(String name, Object... args) throws ScriptException, NoSuchMethodException {
        nashornScriptEngine.invokeFunction(name, args);
    }

    class ReduxClassFilter implements ClassFilter {
        @Override
        public boolean exposeToScripts(String s) {
            return s.contentEquals("mods.quiddity.redux.Javascript.ReduxJavascriptEngine.ReduxAPI");
        }
    }
}
