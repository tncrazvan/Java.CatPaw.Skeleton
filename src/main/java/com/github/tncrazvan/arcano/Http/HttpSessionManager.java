package com.github.tncrazvan.arcano.Http;

import com.github.tncrazvan.arcano.EventManager;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Razvan
 */
public class HttpSessionManager {
    public final Map<String,HttpSession> LIST = new HashMap<>();
    
    public HttpSessionManager() {}
    
    public HttpSession startSession(EventManager e,long sessionTtl) {
        if(e.issetRequestCookie("sessionId")){//if session_id is set
            final String sessionId = e.getRequestCookie("sessionId");
            if(LIST.containsKey(sessionId)){//if session exists
                HttpSession session = LIST.get(sessionId);
                if(e.so.config.session.keepAlive)
                    session.setTime(System.currentTimeMillis());
                return session;
            }
        }
        
        final HttpSession session = new HttpSession(e);
        setSession(session);
        return session;
    }
    
    public HttpSession getSession(String sessionId){
        return LIST.get(sessionId);
    }
    
    public void setSession(HttpSession session){
        LIST.put(session.id(), session);
    }
    
    public boolean issetSession(String sessionId){
        return LIST.containsKey(sessionId);
    }
    
    public void stopSession(HttpSession session){
        LIST.remove(session.id());
    }
}
