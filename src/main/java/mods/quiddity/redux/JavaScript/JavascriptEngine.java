package mods.quiddity.redux.JavaScript;

import javax.script.ScriptException;

public interface JavascriptEngine {
    public void init();
    public void loadScript(String script) throws ScriptException;
    public void killEngine();
    public void restartEngine();
    public void addJavaObject(String name, Object object);
    public boolean hasObject(String name);
    public void callMethod(String name, Object... args) throws ScriptException, NoSuchMethodException;
    public Object getObject(String name);
}
