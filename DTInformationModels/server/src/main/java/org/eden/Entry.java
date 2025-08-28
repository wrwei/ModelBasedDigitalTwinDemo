package org.eden;

import org.eden.javalin.Server;

public class Entry {
    public static Server server;

    public static void main(String[] args) {
        server = new Server();
    }
}