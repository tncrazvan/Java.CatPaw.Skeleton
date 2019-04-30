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

import com.github.tncrazvan.elkserver.Elk;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author Razvan
 */
public class HttpHeader extends Elk{
    private final Map<String, String> header = new HashMap();
    private final Map<String, String[]> cookies = new HashMap();
    public HttpHeader(boolean createSuccessHeader) {
        if(createSuccessHeader){
            header.put("Status", "HTTP/1.1 200 OK");
            header.put("Date",httpDateFormat.format(time()));
            header.put("Cache-Control","no-store");
        }
    }
    public HttpHeader(){
        this(true);
    }
    
    public String fieldToString(String key){
        String value = header.get(key);
        
        if(key.equals("Resource") || key.equals("Status") || value.equalsIgnoreCase(key)){
            return value+"\r\n";
        }
        return key+": "+value+"\r\n";
    }
    
    public String cookieToString(String key){
        String[] c = cookies.get(key);
        LocalDateTime time = (c[3]==null?null:time(Integer.parseInt(c[3])*1000L));
        //Thu, 01 Jan 1970 00:00:00 GMT
        return c[4]+": "
                +key+"="+c[0]
                +(c[1]==null?"":"; path="+c[1])
                +(c[2]==null?"":"; domain="+c[2])
                +(c[3]==null?"":"; expires="+httpDateFormat.format(time))
                +"\r\n";
    }
    
    public String toString(){
        String str= "";
        for(String key : header.keySet()){
            str +=this.fieldToString(key);
        }
        
        for(String key : cookies.keySet()){
            str +=this.cookieToString(key);
        }
        return str;
    }
    
    public boolean isDefined(String key){
        if(header.get(key) == null)
            return false;
        return true;
    }
    
    public void set(String a, String b){
        header.put(a, b);
    }
    
    public String get(String key){
        if(!header.containsKey(key)){
            return null;
        }
        switch(key){
            case "Status":
            case "Resource":
                return header
                        .get(key)
                        .split(" ")[1].trim();
            case "Method":
                return header
                        .get(key)
                        .split(" ")[0].trim();
            default:
                return header.get(key).trim();
        }
    }
    
    public boolean issetCookie(String key){
        Iterator i = cookies.entrySet().iterator();
        Map.Entry pair;
        String tmp = "";
        while(i.hasNext()){
            pair = (Map.Entry)i.next();
            tmp = (String) pair.getKey();
            if(tmp.trim().equals(key.trim())){
                return true;
            }
        }
        return false;
    }
    
    public String getCookie(String key) throws UnsupportedEncodingException{
        String[] cookie = cookies.get(key);
        if(cookie == null) return null;
        return URLDecoder.decode(cookie[0], charset);
    }
    
    public void setCookie(String key, String v, String path, String domain, int expire) throws UnsupportedEncodingException{
        setCookie(key, v, path, domain, httpDateFormat.format(time(expire)));
    }
    public void setCookie(String key, String v, String path, String domain, String expire) throws UnsupportedEncodingException{
        if(path == null) path = "/";
        String [] b = new String[5];
        b[0] = URLEncoder.encode(v,charset);
        b[1] = path;
        b[2] = domain;
        b[3] = expire;
        b[4] = "Set-Cookie";
        cookies.put(key.trim(), b);
    }
    
    public void setCookie(String key,String v,String path, String domain) throws UnsupportedEncodingException{
        setCookie(key, v, path, domain,null);
    }
    
    
    public void setCookie(String key,String v, String path) throws UnsupportedEncodingException{
        setCookie(key, v, path, null, null);
    }
    
    public void setCookie(String key, String v) throws UnsupportedEncodingException{
        setCookie(key, v, "/", null, null);
    }
    
    public Map<String,String> getMap(){
        return header;
    }
    
    public static HttpHeader fromString(String string){
        HttpHeader header = new HttpHeader(false);
        String[] tmp = string.split("\\r\\n");
        boolean end = false;
        for(int i=0;i<tmp.length;i++){
            if(tmp[i].equals("")) continue;
            
            String[] item = tmp[i].split(":(?=\\s)");
            if(item.length>1){
                if(item[0].equals("Cookie")){
                    String[] c = item[1].split(";");
                    for(int j=0;j<c.length;j++){
                        String[] cookieInfo = c[j].split("=(?!\\s|\\s|$)");
                        
                        if(cookieInfo.length > 1){
                            String [] b = new String[5];
                            b[0] = cookieInfo[1];
                            b[1] = cookieInfo.length>2?cookieInfo[2]:null;
                            b[2] = cookieInfo.length>3?cookieInfo[3]:null;
                            b[3] = cookieInfo.length>3?cookieInfo[3]:null;
                            b[4] = "Cookie";
                            header.cookies.put(cookieInfo[0], b);
                        }
                        
                        
                    }
                }else{
                    header.set(item[0],item[1]);
                }
            }else{
                if(tmp[i].substring(0,3).equals("GET")){
                    header.set("Resource",tmp[i]);
                    header.set("Method","GET");
                }else if(tmp[i].substring(0,4).equals("POST")){
                    header.set("Resource",tmp[i]);
                    header.set("Method","POST");
                }else{
                    header.set(tmp[i],tmp[i]);
                }
            }
        }
        return header;
    }

    
}
