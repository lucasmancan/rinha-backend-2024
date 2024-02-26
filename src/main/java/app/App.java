package app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.squareup.okhttp.*;
import io.jooby.Jooby;
import io.jooby.hikari.HikariModule;
import io.jooby.jackson.JacksonModule;
import io.jooby.netty.NettyServer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class App extends Jooby {

    {

        ObjectMapper mapper = new ObjectMapper();

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE_TIME));

        mapper.registerModule(javaTimeModule);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        install(new JacksonModule(mapper));
        install(new NettyServer());
        install(new HikariModule("db"));
        install(ClienteRouter::new);

        onStarted(() -> {
//
//            for (int i = 0; i < 100; i++) {
//                warmUp();
//            }
//
//            resetDb();
//
//            System.out.println("Completed");

        });

    }

    public static void main(final String[] args) {
        System.setProperty("org.jooq.no-logo", "true");
        System.setProperty("org.jooq.no-tips", "true");
        runApp(args, App::new);


    }

    private static void warmUp() throws IOException {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("text/plain");
        Request request = new Request.Builder()
                .url("http://localhost:8080/clientes/2/extrato")
                .method("GET", null)
                .build();
        Response response = client.newCall(request).execute();

        RequestBody body = RequestBody.create(mediaType, "{\n    \"valor\": 1,\n    \"tipo\": \"d\",\n    \"descricao\":\"123\"\n}");
        Request request2 = new Request.Builder()
                .url("http://localhost:8080/clientes/2/transacoes")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
         client.newCall(request).execute();
         client.newCall(request).execute();
    }

    private static void resetDb() throws IOException {
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("text/plain");
        Request request = new Request.Builder()
                .url("http://localhost:8080/health-check")
                .method("GET", null)
                .build();
        Response response = client.newCall(request).execute();
    }
}
