package studykit.springboot.autoconfigure;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.lang.annotation.*;

@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnableAutoConfiguration(excludeName = {
    "org.springframework.boot.autoconfigure.jdbc.XADataSourceAutoConfiguration",
    "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
})
@SpringBootApplication
public @interface BootApplication {
}
