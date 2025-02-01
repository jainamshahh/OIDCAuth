package com.jainam.authentication;

import java.nio.file.Paths;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import java.net.URL;



public class App 
{
    public static void main( String[] args ) throws Exception
    {
        Server server = new Server();
        int httpsPort = 8443;
        
        URL keystoreUrl = App.class.getClassLoader().getResource("ssl/keystore.jks");
        if (keystoreUrl == null) {
            System.out.println("not found keystore");
        }
        String keystorePath = Paths.get(keystoreUrl.toURI()).toString();

        // Setup SSL
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(keystorePath);
        sslContextFactory.setKeyStorePassword("123456");
        sslContextFactory.setKeyManagerPassword("123456");

        // Setup HTTPS Configuration
        HttpConfiguration httpsConf = new HttpConfiguration();
        httpsConf.setSecurePort(httpsPort);
        httpsConf.setSecureScheme("https");
        SecureRequestCustomizer customizer = new SecureRequestCustomizer();
        customizer.setSniHostCheck(false);
        httpsConf.addCustomizer(customizer);
        
         // Establish the ServerConnector
        ServerConnector httpsConnector = new ServerConnector(server,
            new SslConnectionFactory(sslContextFactory, "http/1.1"),
            new HttpConnectionFactory(httpsConf));
        httpsConnector.setPort(httpsPort);

        server.addConnector(httpsConnector);

        ContextHandler context1 = new ContextHandler(new IndexPageHandler(), "/");
        ContextHandler context2 = new ContextHandler(new MainHandler(), "/auth");

        ContextHandlerCollection contexts = new ContextHandlerCollection(context1,context2);

        server.setHandler(contexts);

        server.start();
        server.join();

    }
    
}
