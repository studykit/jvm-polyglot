package studykit.springboot.autoconfigure.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    classes = KafkaPropertiesTest.Config.class,
    args = {"--spring.config.location=classpath:kafka.yml"}
)
public class KafkaPropertiesTest {
    @Autowired
    private KafkaProperties kafkaProperties;

    @Test
    public void testKafkaProperties() {
        assertNotNull(kafkaProperties);
        assertTrue(kafkaProperties.enable());
        assertNotNull(kafkaProperties.bootstraps());
        assertEquals(1, kafkaProperties.bootstraps().size());
        assertNotNull(kafkaProperties.topics());
        assertEquals(1, kafkaProperties.topics().size());
    }

    @Configuration
    @EnableConfigurationProperties(KafkaProperties.class)
    public static class Config {
    }
}
