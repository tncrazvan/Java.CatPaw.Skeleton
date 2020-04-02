package com.github.tncrazvan.arcano.WebSocket;

import static com.github.tncrazvan.arcano.SharedObject.LOGGER;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;

import com.github.tncrazvan.arcano.Http.HttpRequestReader;

/**
 *
 * @author Razvan Tanase
 */
public abstract class WebSocketController extends WebSocketEvent{

    protected String[] args;
    
    public void setArgs(final String[] args) {
        this.args=args;
    }
    
    public static final WebSocketGroupManager GROUP_MANAGER = new WebSocketGroupManager();
    
    
    public final WebSocketController install(final HttpRequestReader reader){
        try{
            this.reader = reader;
            this.args = reader.args;
            this.resolveRequestId();
            this.initEventManager();
            this.findRequestLanguages();
            return this;
        } catch (final UnsupportedEncodingException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
