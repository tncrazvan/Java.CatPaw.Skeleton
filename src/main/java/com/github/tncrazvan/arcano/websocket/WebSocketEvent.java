package com.github.tncrazvan.arcano.websocket;

import static com.github.tncrazvan.arcano.SharedObject.LOGGER;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;

import com.github.tncrazvan.arcano.InvalidControllerConstructorException;
import com.github.tncrazvan.arcano.WebObject;
import com.github.tncrazvan.arcano.http.HttpRequestReader;


/**
 *
 * @author Razvan Tanase
 */
public abstract class WebSocketEvent extends WebSocketEventManager{
    
    public static final void serve(HttpRequestReader reader){
        if(reader.location.length == 0 || "".equals(reader.location[0]))
            reader.location = new String[]{"/"};
        for (int i = reader.location.length; i > 0; i--) {
            String path = "/" + String.join("/", Arrays.copyOf(reader.location, i)).toLowerCase();
            instantPack(reader, reader.so.WEB_SOCKET_ROUTES.get(path));
        }
    }
    
    private static void instantPack(HttpRequestReader reader,WebObject wo) {
        if(wo != null){
            try{
                Class<?> cls = Class.forName(wo.getClassName());
                Constructor<?> constructor = cls.getDeclaredConstructor();
                if (constructor == null) throw new InvalidControllerConstructorException(
                    String.format(
                        "\nController %s does not contain a valid constructor.\n"
                        + "A valid constructor for your controller is a constructor that has no parameters.\n"
                        + "Perhaps your class is an inner class and it's not static or public? Try make it a \"static public class\"!",
                        cls.getName()
                    )
                );
                WebSocketController controller = (WebSocketController) constructor.newInstance();
                controller.install(reader);
                controller.execute();
            }catch(ClassNotFoundException e){
                instantPack(reader, reader.so.WEB_SOCKET_ROUTES_NOT_FOUND);
            }catch(InvalidControllerConstructorException | NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
                LOGGER.log(Level.SEVERE, null, e);
            }
        }
    }

    protected abstract void onOpen();

    protected abstract void onMessage(WebSocketCommit payload);

    protected abstract void onClose();

    @Override
    protected final void manageOnOpen() {
        if (reader.so.WEB_SOCKET_EVENTS.get(this.getClass().getName()) == null) {
            final ArrayList<WebSocketEvent> tmp = new ArrayList<>();
            tmp.add(this);
            reader.so.WEB_SOCKET_EVENTS.put(this.getClass().getName(), tmp);
        } else {
            reader.so.WEB_SOCKET_EVENTS.get(this.getClass().getName()).add(this);
        }
        this.onOpen();
    }

    @Override
    protected final void manageOnMessage(final WebSocketCommit payload) {
        this.onMessage(payload);
    }
    
    @Override
    protected final void manageOnClose() {
        reader.so.WEB_SOCKET_EVENTS.get(this.getClass().getName()).remove(this);
        this.onClose();
    }
    
}