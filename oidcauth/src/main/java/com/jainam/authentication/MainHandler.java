package com.jainam.authentication;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Base64;

import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.Fields;

public class MainHandler extends Handler.Abstract{

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        
        
        
        String path = request.getHttpURI().getPath();
        System.out.println(path);
        
        if(path.equals("/auth/callback")){

            //temporary testing code
            response.setStatus(200);
            Charset charset = Charset.forName("UTF-8");
            Fields paramaeters = Request.extractQueryParameters(request);
            String param = paramaeters.toString();
            ByteBuffer byteBuffer = charset.encode(path+" "+param);
            response.write(true,byteBuffer,callback);
            
        }
        else if(path.equals("/auth/")){//https://stackoverflow.com/questions/77719967/avoid-301-re-directs-with-http-post-requests-in-jetty-12
            try {
                String redirectLocation = getGoogleAuthServer();
                
                Response.sendRedirect(request, response, callback, redirectLocation);
            } catch (Exception e) {
                Response.writeError(request, response, callback, 500);
            }  

        }


        return true;

    }
    public String getGoogleAuthServer() throws URISyntaxException{
        String state = new BigInteger(130, new SecureRandom()).toString(32);
        final String clientId = "486921582196-m8c6398cmac9f9sqj6tbpiprgia17b7o.apps.googleusercontent.com";
        byte[] nonce = new byte[16]; // 16 bytes (128 bits) is a good default
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(nonce);
        String nonceBase64 = Base64.getEncoder().encodeToString(nonce);

        URIBuilder uri = new URIBuilder("https://accounts.google.com/o/oauth2/v2/auth")
        .addParameter("response_type", "code")
        .addParameter("client_id", clientId)
        .addParameter("scope", "openid email profile")
        .addParameter("redirect_uri", "https://localhost:8443/auth/callback")
        .addParameter("state", state)
        .addParameter("nonce", nonceBase64);
        System.out.println(uri.toString());
        return(uri.toString());
    }
    
}
