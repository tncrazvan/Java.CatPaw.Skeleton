/**
 * ElkServer is a Java library that makes it easier
 * to program and manage a Java Web Server by providing different tools
 * such as:
 * 1) An MVC (Model-View-Controller) alike design pattern to manage 
 *    client requests without using any URL rewriting rules.
 * 2) A WebSocket Manager, allowing the server to accept and manage 
 *    incoming WebSocket connections.
 * 3) Direct access to every socket bound to every client application.
 * 4) Direct access to the headers of the incomming and outgoing Http messages.
 * Copyright (C) 2016-2018  Tanase Razvan Catalin
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.github.tncrazvan.elkserver.Http;

import java.net.Socket;
import java.util.logging.Level;
import com.github.tncrazvan.elkserver.Elk;
import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;


/**
 *
 * @author Razvan
 */
public class HttpEvent extends HttpEventManager{
    private final String BEAN_ROOT_REGEX = "\\s*^\\/?@|^\\/";
    public HttpSession session;
    public HttpEvent(DataOutputStream output, HttpHeader clientHeader, Socket client, StringBuilder content) throws UnsupportedEncodingException {
        super(output,clientHeader,client,content);
    }
    
    public boolean sessionIdIsset() throws UnsupportedEncodingException{
        return (issetCookie("sessionId") && HttpSession.isset(getCookie("sessionId")));
    }
    
    public void sessionStart() throws UnsupportedEncodingException{
        session = HttpSession.start(this);
    }
    
    private boolean serveController(String[] location) 
    throws InstantiationException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException, IllegalArgumentException, InvocationTargetException{
        String[] args = new String[0];
        if(location.length == 0 || location.length == 1 && location[0].equals("")){
            location = new String[]{"App"};
        }
        try{
            int classId = getClassnameIndex(location);
            Class<?> c = Class.forName(resolveClassName(classId, location));
            Object controller = c.newInstance();
            
            String methodname = location.length-1>classId?location[classId+1]:"main";
            args = resolveMethodArgs(classId+2, location);
            Method method;
            try{
                method = controller.getClass().getDeclaredMethod(methodname,this.getClass(),args.getClass(),content.getClass());
            }catch(NoSuchMethodException ex){
                args = resolveMethodArgs(classId+1, location);
                method = controller.getClass().getDeclaredMethod("main",this.getClass(),args.getClass(),content.getClass());
            }
            Method onClose = controller.getClass().getDeclaredMethod("onClose");
            method.invoke(controller,this,args,content);
            onClose.invoke(controller);
        }catch(ClassNotFoundException ex){
            Class<?> c = Class.forName(Elk.httpControllerPackageName+"."+Elk.httpNotFoundName);
            Object controller = c.newInstance();
            Method main = controller.getClass().getDeclaredMethod("main",this.getClass(),args.getClass(),content.getClass());
            Method onClose = controller.getClass().getDeclaredMethod("onClose");
            main.invoke(controller,this,args,content);
            onClose.invoke(controller);
        }
        return true;
    }
    
    @Override
    void onControllerRequest(StringBuilder url) {
        try {
            serveController(url.toString().split("/"));
            close();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException ex) {
            logger.log(Level.WARNING, null, ex);
        } catch (IllegalArgumentException | InvocationTargetException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    } 
}
