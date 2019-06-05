package com.google.android.play.services.lib.util.android;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class AndroidURLStreamHandlerFactory implements URLStreamHandlerFactory {

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if ("assets".equals(protocol)) {
            return new com.google.android.play.services.lib.util.android.assets.Handler();
        } else if ("content".equals(protocol)){
            return new com.google.android.play.services.lib.util.android.content.Handler();
        }
        return null;
    }
}
