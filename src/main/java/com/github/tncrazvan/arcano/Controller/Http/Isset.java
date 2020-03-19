package com.github.tncrazvan.arcano.Controller.Http;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import com.github.tncrazvan.arcano.Http.HttpController;
import static com.github.tncrazvan.arcano.Tool.Http.Status.STATUS_FOUND;
import static com.github.tncrazvan.arcano.Tool.Http.Status.STATUS_NOT_FOUND;
import com.github.tncrazvan.arcano.Bean.Http.HttpService;

/**
 *
 * @author Razvan
 */

@HttpService(path = "/@isset")
public class Isset extends HttpController{
    @HttpService(path="/file")
    public void file() throws FileNotFoundException, IOException{
        final String url = String.join("/", reader.args);
        final File f = new File(reader.so.config.webRoot, url);
        if (f.exists()) {
            setResponseStatus(STATUS_FOUND);
        } else {
            setResponseStatus(STATUS_NOT_FOUND);
        }
        flush();
    }

    @HttpService(path = "/cookie")
    public void cookie() {
        final String name = String.join("/", reader.args);
        if(issetRequestCookie(name)){
            setResponseStatus(STATUS_FOUND);
        }else{
            setResponseStatus(STATUS_NOT_FOUND);
        }
       flush();
    }
}
