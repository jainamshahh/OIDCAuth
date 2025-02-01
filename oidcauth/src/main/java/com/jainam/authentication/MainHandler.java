package com.jainam.authentication;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;

import org.apache.http.client.utils.URIBuilder;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.StringRequestContent;
import org.eclipse.jetty.client.internal.HttpContentResponse;
import org.eclipse.jetty.client.transport.HttpClientTransportDynamic;
import org.eclipse.jetty.client.transport.HttpRequest;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.Fields;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class MainHandler extends Handler.Abstract{

    final String clientId = "486921582196-m8c6398cmac9f9sqj6tbpiprgia17b7o.apps.googleusercontent.com";
    final String redirectUri = "https://localhost:8443/auth/callback";

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        
        
        
        String path = request.getHttpURI().getPath();
        System.out.println(path);
        
        if(path.equals("/auth/callback")){

            String clientSecret = getGoogleClientSecret();

            Fields parameters = Request.extractQueryParameters(request);
            String code = parameters.getValue("code");

            String reqContent = "code="+ code + "&" + "client_id=" + clientId + "&" + "client_secret="+ clientSecret + "&" + "redirect_uri="+ redirectUri + "&" + "grant_type=authorization_code";

            SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
            sslContextFactory.setIncludeProtocols("TLSv1.3");
            ClientConnector clientConnector = new ClientConnector();
            clientConnector.setSslContextFactory(sslContextFactory);

            try (HttpClient httpClient = new HttpClient(new HttpClientTransportDynamic(clientConnector))) {
            httpClient.start();

            HttpField host = new HttpField("Host", "oauth2.googleapis.com");
            HttpField contentType = new HttpField("Content-Type", "application/x-www-form-urlencoded");
            StringRequestContent body = new StringRequestContent("application/x-www-form-urlencoded", reqContent);
            HttpRequest req = (HttpRequest) httpClient.POST("https://oauth2.googleapis.com/token");
            HttpContentResponse res = (HttpContentResponse) req.addHeader(host).addHeader(contentType).body(body).send();

            Gson gson = new Gson();
            JsonObject resContent = gson.fromJson(res.getContentAsString(), JsonObject.class);
            

            String[] parts = resContent.get("id_token").getAsString().split("\\.");
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));

            JsonObject idTokenPayload = gson.fromJson(payloadJson, JsonObject.class);

            
            String issuer = idTokenPayload.get("iss").getAsString();
            String email = idTokenPayload.get("email").getAsString();
            String name = idTokenPayload.get("name").getAsString();
            
            
            
            response.setStatus(200);
            Charset charset = Charset.forName("UTF-8");
            ByteBuffer byteBuffer = charset.encode("Authenticated \n"+"Name : "+name+"\nEmail : "+email);
            response.write(true,byteBuffer,callback);
            
        }
            
            
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
        
        byte[] nonce = new byte[16]; // 16 bytes (128 bits) is a good default
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(nonce);
        String nonceBase64 = Base64.getEncoder().encodeToString(nonce);

        URIBuilder uri = new URIBuilder("https://accounts.google.com/o/oauth2/v2/auth")
        .addParameter("response_type", "code")
        .addParameter("client_id", clientId)
        .addParameter("scope", "openid email profile")
        .addParameter("redirect_uri", redirectUri)
        .addParameter("state", state)
        .addParameter("nonce", nonceBase64);
        
        return(uri.toString());
    }

    public String getGoogleClientSecret(){
         Properties properties = new Properties();
            try (InputStream input = Testt.class.getClassLoader().getResourceAsStream("secrets/googleauth.secret")) {
                properties.load(input);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load config file", e);
            }
            return(properties.getProperty("client_secret"));
    }
}

