package studykit.springboot.autoconfigure.kafka;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.JacksonUtils;
import org.springframework.kafka.support.converter.BatchMessagingMessageConverter;
import org.springframework.kafka.support.converter.ByteArrayJsonMessageConverter;

import java.util.Map;

@AutoConfiguration
@EnableKafka
@ConditionalOnProperty(prefix = "kafka", name = "enable", havingValue = "true")
@EnableConfigurationProperties(KafkaProperties.class)
public class KafkaAutoConfiguration {

    public static final String RECORD_CONTAINER_FACTORY = "recordContainerFactory";

    public static final String NO_RECORD_CONTAINER_FACTORY = "noRecordContainerFactory";


    @Bean
    public ByteArrayJsonMessageConverter recordJsonMessageConverter() {
        return new ByteArrayJsonMessageConverter(
            JacksonUtils.enhancedObjectMapper().setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        );
    }

    @Bean
    public ConsumerFactory<?, ?> containerConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(Map.of());
    }

    @Bean
    public ListenerAnnotationRegistry containerAnnotationRegistry() {
        return new ListenerAnnotationRegistry();
    }

    @Bean
    public ListenerAnnotationEnhancer containerAnnotationEnhancer(ListenerAnnotationRegistry listenerAnnotationRegistry) {
        return new ListenerAnnotationEnhancer(listenerAnnotationRegistry);
    }

    @Bean
    public ListenerAnnotationCustomizer containerAnnotationCustomizer(ListenerAnnotationRegistry listenerAnnotationRegistry) {
        return new ListenerAnnotationCustomizer(listenerAnnotationRegistry);
    }

    @Bean(NO_RECORD_CONTAINER_FACTORY)
    public KafkaListenerContainerFactory<?> noRecordContainerFactory(
        ConsumerFactory<Object, Object> containerConsumerFactory,
        ListenerAnnotationCustomizer listenerAnnotationCustomizer
    ) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(containerConsumerFactory);
        factory.setContainerCustomizer(listenerAnnotationCustomizer);
        return factory;
    }

    @Bean(RECORD_CONTAINER_FACTORY)
    public KafkaListenerContainerFactory<?> recordContainerFactory(
        ConsumerFactory<Object, Object> containerConsumerFactory,
        ByteArrayJsonMessageConverter recordJsonMessageConverter,
        ListenerAnnotationCustomizer listenerAnnotationCustomizer
    ) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(containerConsumerFactory);
        factory.setRecordMessageConverter(recordJsonMessageConverter);
        factory.setBatchMessageConverter(new BatchMessagingMessageConverter(recordJsonMessageConverter));
        factory.setContainerCustomizer(listenerAnnotationCustomizer);
        return factory;
    }
}
