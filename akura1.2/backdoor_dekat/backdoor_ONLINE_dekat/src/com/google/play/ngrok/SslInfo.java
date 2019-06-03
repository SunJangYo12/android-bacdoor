package com.google.play.ngrok;

import java.nio.channels.SelectionKey;
import javax.net.ssl.SSLEngine;

class SslInfo {
    SSLEngine engine;
    SelectionKey tokey;
    public SelectionKey key;

    SslInfo() {
    }
}