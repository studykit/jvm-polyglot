package studykit.springboot.autoconfigure.kafka;

import org.springframework.kafka.config.ContainerCustomizer;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

public class ListenerAnnotationCustomizer implements ContainerCustomizer<Object, Object, ConcurrentMessageListenerContainer<Object, Object>> {
    private final ListenerAnnotationRegistry registry;

    public ListenerAnnotationCustomizer(ListenerAnnotationRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void configure(ConcurrentMessageListenerContainer<Object, Object> container) {
        Listener annotation = registry.get(container.getGroupId());
        if (annotation == null) {
            return;
        }

        ContainerProperties properties = container.getContainerProperties();
        properties.setAckMode(annotation.ackMode());
        properties.setAckTime(annotation.ackTime());
        properties.setAckCount(annotation.ackCount());
        properties.setLogContainerConfig(annotation.logContainerConfig());
        properties.setMissingTopicsFatal(annotation.missingTopicsFatal());
        properties.setAssignmentCommitOption(annotation.assignmentCommitOption());
        properties.setDeliveryAttemptHeader(annotation.deliveryAttemptHeader());
    }
}
