package mods.quiddity.redux.Engines;

import mods.quiddity.redux.json.model.Pack;

import javax.script.ScriptException;
import java.io.InputStream;

public interface Engine {
    public void init();
    public void loadScript(String scriptName, InputStream scriptInput) throws ScriptException;
    public void killEngine();
    public void restartEngine();
    public void addJavaObject(String name, Object object);
    public boolean respondsToMethod(String name);
    public boolean globalObjectExists(String name);
    public void callMethod(String name, Object... args) throws ScriptException, NoSuchMethodException;
    public Object getJavaObject(String name, Class<?> javaType);
    public Pack getPackReference();
}
