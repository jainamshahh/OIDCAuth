package com.jainam.authentication;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

public class IndexPageHandler extends Handler.Abstract{

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        
        response.setStatus(200);
        Charset charset = Charset.forName("UTF-8");
        ByteBuffer byteBuffer = charset.encode("HELLO WORLD");
        response.write(true,byteBuffer,callback);
        return true;
    }
    
}
