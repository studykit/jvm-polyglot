package studykit.springboot.autoconfigure.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;


@ConfigurationProperties(prefix = "kafka")
public record KafkaProperties(
    boolean enable,
    Map<String /* bootstrap name */, String /*bootstrap uri */> bootstraps,
    Map<String /* logical name */, KafkaTopic> topics
) {
}
