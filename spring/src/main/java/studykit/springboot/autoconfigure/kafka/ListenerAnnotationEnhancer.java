package studykit.springboot.autoconfigure.kafka;

import jakarta.annotation.Nullable;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.kafka.annotation.KafkaListenerAnnotationBeanPostProcessor.AnnotationEnhancer;
import org.springframework.util.StringUtils;

import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.lang.String.join;


public class ListenerAnnotationEnhancer implements BeanFactoryAware, AnnotationEnhancer {
    private static final Logger logger = LoggerFactory.getLogger(ListenerAnnotationEnhancer.class);

    /**
     * @see org.springframework.kafka.annotation.KafkaListener#properties()
     */
    private static final String PROPERTIES_FORMAT = "%s=%s";

    /**
     * @see org.springframework.kafka.annotation.KafkaListener#properties()
     */
    private static final String PROPERTIES = "properties";

    /**
     * @see org.springframework.kafka.annotation.KafkaListener#containerFactory()
     */
    private static final String CONTAINER_FACTORY = "containerFactory";


    public ListenerAnnotationEnhancer(@NotNull ListenerAnnotationRegistry registry) {
        this.registry = registry;
    }

    @NotNull
    private final ListenerAnnotationRegistry registry;


    @Nullable
    private Resolver resolver;


    @Override
    public void setBeanFactory(@NotNull BeanFactory beanFactory) throws BeansException {
        if (!(beanFactory instanceof ConfigurableBeanFactory bf)) {
            return;
        }

        this.resolver = new Resolver(bf);
    }

    @Override
    public Map<String, Object> apply(Map<String, Object> props, AnnotatedElement annotatedElement) {
        // props is not a map. It's actually AnnotationAttributes.
        if (resolver == null) {
            return props;
        }

        final var container = AnnotationUtils.getAnnotation(annotatedElement, Listener.class);
        if (container == null) {
            return props;
        }

        if (container.bootstrap().isEmpty()) {
            throw new IllegalArgumentException("bootstrap should be specified {annotatedElement=" + annotatedElement + "}");
        }

        String groupId = resolver.resolve(container.groupId());
        if (!StringUtils.hasText(groupId)) {
            throw new IllegalArgumentException("groupId should not be empty {annotatedElement=" + annotatedElement + "}");
        }

        if (registry.has(groupId)) {
            throw new IllegalArgumentException("groupId already exists {annotatedElement=" + annotatedElement + "}");
        }

        props.put(PROPERTIES, consumerProperties(container).toArray(new String[0]));
        var cf = (container.valueDeser() == ByteArrayDeserializer.class) ?
            KafkaAutoConfiguration.RECORD_CONTAINER_FACTORY :
            KafkaAutoConfiguration.NO_RECORD_CONTAINER_FACTORY;

        props.put(CONTAINER_FACTORY, cf);
        registry.put(groupId, container);

        logger.debug("Container Properties {}", props);
        return props;
    }

    @NotNull
    private List<String> consumerProperties(Listener listener) {
        final List<String> p = new ArrayList<>(Arrays.asList(listener.properties()));

        assert resolver != null;

        String bootstrap = resolver.resolve(listener.bootstrap());
        if (!StringUtils.hasText(bootstrap)) {
            throw new IllegalArgumentException("the reolved value of bootstrap should not be empty {container=" + listener + "}");
        }

        p.add(format(PROPERTIES_FORMAT, ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap));
        p.add(format(PROPERTIES_FORMAT, ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, listener.autoCommit()));

        p.add(format(PROPERTIES_FORMAT, ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, listener.autoOffsetReset()));
        p.add(format(PROPERTIES_FORMAT, ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, listener.keyDeser().getCanonicalName()));
        p.add(format(PROPERTIES_FORMAT, ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, listener.valueDeser().getCanonicalName()));

        if (listener.partitionAssignmentStrategy().length == 0) {
            return p;
        }

        var pstraties = Arrays.stream(listener.partitionAssignmentStrategy())
            .map(Class::getCanonicalName)
            .toList();

        p.add(format(PROPERTIES_FORMAT, ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG, join(",", pstraties)));
        return p;
    }

    private static class Resolver {
        private final BeanExpressionResolver resolver;
        private final BeanExpressionContext context;
        private final ConfigurableBeanFactory beanFactory;

        public Resolver(ConfigurableBeanFactory bf) {
            this.resolver = bf.getBeanExpressionResolver() == null ?
                new StandardBeanExpressionResolver() : bf.getBeanExpressionResolver();

            this.context = new BeanExpressionContext(bf, null);
            this.beanFactory = bf;
        }

        @SuppressWarnings("unchecked")
        @Nullable
        public <T> T resolve(String expr) {
            if (!StringUtils.hasText(expr)) {
                return (T) expr;
            }

            return (T) resolver.evaluate(beanFactory.resolveEmbeddedValue(expr), context);
        }
    }
}
