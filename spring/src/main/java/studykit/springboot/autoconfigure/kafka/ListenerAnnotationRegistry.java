package studykit.springboot.autoconfigure.kafka;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

public class ListenerAnnotationRegistry {
    private final Map<String /*group Id */, Listener> registry = new HashMap<>();

    public boolean has(String groupId) {
        return registry.containsKey(groupId);
    }

    public void put(@Nonnull String groupId, @Nonnull Listener listener) {
        registry.put(groupId, listener);
    }

    @Nullable
    public Listener get(@Nullable String groupId) {
        return registry.get(groupId);
    }
}
