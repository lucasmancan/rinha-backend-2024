package app;

import app.registrartransacao.RegistrarTransacaoUsecaseImpl;
import app.registrartransacao.Transacao;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import io.jooby.Jooby;
import io.jooby.hikari.HikariModule;
import io.jooby.jackson.JacksonModule;
import io.jooby.netty.NettyServer;

import javax.sql.DataSource;
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
       // final var registrarTransacaoUsecase = new RegistrarTransacaoUsecaseImpl(require(DataSource.class));

//        for (int i = 0; i < 2000; i++) {
//            registrarTransacaoUsecase.registrar(new Transacao(1, 1, "d", "warmup"));
//        }
//
//        for (int i = 0; i < 10 ; i++) {
//
//        }

    }

    public static void main(final String[] args) {
        System.setProperty("org.jooq.no-logo", "true");
        System.setProperty("org.jooq.no-tips", "true");

        runApp(args, App::new);


    }
}
