package app;

import io.jooby.Jooby;
import io.jooby.hikari.HikariModule;
import io.jooby.jackson.JacksonModule;
import io.jooby.netty.NettyServer;


public class App extends Jooby {

    {

        install(new JacksonModule());
        install(new NettyServer());
        install(new HikariModule("db"));
        install(ClienteRouter::new);
    }

    public static void main(final String[] args) {
        System.setProperty("org.jooq.no-logo", "true");
        System.setProperty("org.jooq.no-tips", "true");

        runApp(args, App::new);
    }
}
