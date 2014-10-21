package com.tvs.signaltracker;

/**
 * @author Lucas Teske
 *  _   _                       ____  _                   _ 
 * | | | |_   _ _ __   ___ _ __/ ___|(_) __ _ _ __   __ _| |
 * | |_| | | | | '_ \ / _ \ '__\___ \| |/ _` | '_ \ / _` | |
 * |  _  | |_| | |_) |  __/ |   ___) | | (_| | | | | (_| | |
 * |_| |_|\__, | .__/ \___|_|  |____/|_|\__, |_| |_|\__,_|_|
 *       |___/|_|                      |___/               
 *  ____  _                   _ _____               _             
 * / ___|(_) __ _ _ __   __ _| |_   _| __ __ _  ___| | _____ _ __ 
 * \___ \| |/ _` | '_ \ / _` | | | || '__/ _` |/ __| |/ / _ \ '__|
 * _ __) | | (_| | | | | (_| | | | || | | (_| | (__|   <  __/ |   
 * |____/|_|\__, |_| |_|\__,_|_| |_||_|  \__,_|\___|_|\_\___|_|   
 *         |___/                                                 
 * 
 * Created by: Lucas Teske from Teske Virtual System
 * Package: com.tvs.signaltracker
 */


import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.SSLSocketFactory;

public class EasySSLSocketFactory extends SSLSocketFactory {
    SSLContext sslContext = SSLContext.getInstance("TLS");

    public EasySSLSocketFactory(KeyStore truststore)
                    throws NoSuchAlgorithmException, KeyManagementException,
                    KeyStoreException, UnrecoverableKeyException {
            super(truststore);

            TrustManager tm = new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] chain,
                                    String authType) throws CertificateException {
                    }

                    public void checkServerTrusted(X509Certificate[] chain,
                                    String authType) throws CertificateException {
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                            return null;
                    }
            };

            sslContext.init(null, new TrustManager[] { tm }, null);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port,
                    boolean autoClose) throws IOException, UnknownHostException {
            return sslContext.getSocketFactory().createSocket(socket, host, port,
                            autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
            return sslContext.getSocketFactory().createSocket();
    }

}