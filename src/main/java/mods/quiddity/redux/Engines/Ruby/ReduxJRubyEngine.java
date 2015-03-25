package mods.quiddity.redux.Engines.Ruby;

import mods.quiddity.redux.Engines.Engine;
import mods.quiddity.redux.Engines.ReduxAPI;
import mods.quiddity.redux.json.model.Pack;
import org.jruby.embed.LocalContextScope;
import org.jruby.embed.ScriptingContainer;

import javax.script.ScriptException;
import java.io.InputStream;

public class ReduxJRubyEngine implements Engine {
    private ScriptingContainer engineContainer;
    private Pack pack;

    public ReduxJRubyEngine(Pack pack) {
        this.pack = pack;
    }

    public void init() {
        engineContainer = new ScriptingContainer(LocalContextScope.SINGLETON);
        addJavaObject("ReduxAPI", new ReduxAPI(this));
    }

    @Override
    public void loadScript(String scriptName, InputStream scriptInput) throws ScriptException {
        engineContainer.runScriptlet(scriptInput, scriptName);
    }

    @Override
    public void killEngine() {
        engineContainer.terminate();
    }

    @Override
    public void restartEngine() {
        engineContainer.terminate();
        init();
    }

    @Override
    public void addJavaObject(String name, Object object) {
        engineContainer.put(name, object);
    }

    @Override
    public boolean respondsToMethod(String name) {
        return engineContainer.getProvider().getRuntime().getTopSelf().respondsTo(name);
    }

    @Override
    public boolean globalObjectExists(String name) {
        return engineContainer.get(name) != null;
    }

    @Override
    public void callMethod(String name, Object... args) throws ScriptException, NoSuchMethodException {
        engineContainer.callMethod(null, name, args);
    }

    @Override
    public Object getJavaObject(String name, Class<?> javaType) {
        Object javaObject = engineContainer.get(name);
        if (!javaType.isAssignableFrom(javaObject.getClass())) {
            return null;
        }
        return javaObject;
    }

    @Override
    public Pack getPackReference() {
        return pack;
    }
}
