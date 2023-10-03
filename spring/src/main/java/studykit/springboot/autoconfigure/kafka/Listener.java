package studykit.springboot.autoconfigure.kafka;


import org.apache.kafka.clients.consumer.ConsumerPartitionAssignor;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.core.annotation.AliasFor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.ContainerProperties.AssignmentCommitOption;

import java.lang.annotation.*;


@SuppressWarnings("unused")
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@KafkaListener
public @interface Listener {
    /**
     * @see org.apache.kafka.clients.consumer.ConsumerConfig#BOOTSTRAP_SERVERS_CONFIG
     */
    String bootstrap();


    @AliasFor(annotation = KafkaListener.class, attribute = "groupId")
    String groupId();

    @AliasFor(annotation = KafkaListener.class, attribute = "topics")
    String[] topics() default {};

    @AliasFor(annotation = KafkaListener.class, attribute = "topicPattern")
    String topicPattern() default "";

    @AliasFor(annotation = KafkaListener.class, attribute = "concurrency")
    String concurrency() default "1";

    @AliasFor(annotation = KafkaListener.class, attribute = "errorHandler")
    String errorHandler() default "";

    @AliasFor(annotation = KafkaListener.class, attribute = "filter")
    String filter() default "";

    @AliasFor(annotation = KafkaListener.class, attribute = "properties")
    String[] properties() default {};

    /**
     * @see org.apache.kafka.clients.consumer.ConsumerConfig#PARTITION_ASSIGNMENT_STRATEGY_CONFIG
     */
    Class<? extends ConsumerPartitionAssignor>[] partitionAssignmentStrategy() default {};

    /**
     * @see org.apache.kafka.clients.consumer.ConsumerConfig#KEY_DESERIALIZER_CLASS_CONFIG
     */
    Class<? extends Deserializer<?>> keyDeser() default StringDeserializer.class;

    /**
     * @see org.apache.kafka.clients.consumer.ConsumerConfig#VALUE_DESERIALIZER_CLASS_CONFIG
     */
    Class<? extends Deserializer<?>> valueDeser() default ByteArrayDeserializer.class;


    /**
     * @see org.apache.kafka.clients.consumer.ConsumerConfig#AUTO_OFFSET_RESET_CONFIG
     */
    String autoOffsetReset();

    /**
     * @see org.apache.kafka.clients.consumer.ConsumerConfig#ENABLE_AUTO_COMMIT_CONFIG
     */
    boolean autoCommit() default false;

    /**
     * @see org.springframework.kafka.listener.ContainerProperties#setAckMode(AckMode)
     */
    AckMode ackMode() default AckMode.BATCH;

    /**
     * @see org.springframework.kafka.listener.ContainerProperties#setAckTime(long)
     */
    long ackTime() default 5000;

    /**
     * @see org.springframework.kafka.listener.ContainerProperties#setAckCount(int)
     */
    int ackCount() default 1;

    /**
     * @see org.springframework.kafka.listener.ContainerProperties#setLogContainerConfig(boolean)
     */
    boolean logContainerConfig() default false;

    /**
     * @see org.springframework.kafka.listener.ContainerProperties#setMissingTopicsFatal(boolean)
     */
    boolean missingTopicsFatal() default false;

    /**
     * @see org.springframework.kafka.listener.ContainerProperties#setDeliveryAttemptHeader(boolean)
     */
    boolean deliveryAttemptHeader() default false;

    /**
     * @see org.springframework.kafka.listener.ContainerProperties.AssignmentCommitOption
     */
    AssignmentCommitOption assignmentCommitOption() default AssignmentCommitOption.LATEST_ONLY_NO_TX;
}
