package studykit.springboot.autoconfigure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.JacksonUtils;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import studykit.springboot.autoconfigure.BootApplication;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

@EmbeddedKafka(
    topics = {"${kafka.topics.test-topic.name}"},
    bootstrapServersProperty = "kafka.topics.test-topic.bootstrap",
    partitions = 1
)
@DirtiesContext
@SpringBootTest(
    classes = {KafkaAutoConfiguration.class, KafkaAutoConfigurationTest.Config.class},
    args = {"--spring.config.location=classpath:kafka.yml"}
)
public class KafkaAutoConfigurationTest {

    @Autowired
    private TestListener listener;

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Value("${kafka.topics.test-topic.name}")
    private String topic;

    private static final ObjectMapper MAPPER = JacksonUtils.enhancedObjectMapper();

    @Test
    public void testConsumer() throws Exception {
        KafkaTemplate<Void, String> kafkaTemplate = new KafkaTemplate<>(
            new DefaultKafkaProducerFactory<>(KafkaTestUtils.producerProps(embeddedKafkaBroker))
        );

        kafkaTemplate.setDefaultTopic(topic);
        kafkaTemplate.sendDefault(MAPPER.writeValueAsString(Map.of("test", "test")));

        listener.latch.await();
    }


    @BootApplication
    public static class Config {
        @Bean
        public TestListener listener() {
            return new TestListener();
        }

    }

    public static class TestListener {
        public CountDownLatch latch = new CountDownLatch(1);

        @Listener(
            bootstrap = "${kafka.topics.test-topic.bootstrap}",
            groupId = "test",
            autoOffsetReset = "earliest",
            topics = {"${kafka.topics.test-topic.name}"}
        )
        public void listen(Map<String, String> record) {
            Assertions.assertTrue(record.containsKey("test"));
            Assertions.assertTrue(record.containsValue("test"));
            latch.countDown();
        }
    }
}



