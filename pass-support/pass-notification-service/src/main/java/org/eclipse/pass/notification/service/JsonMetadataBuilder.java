package org.eclipse.pass.notification.service;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.eclipse.pass.support.client.model.Submission;
import org.eclipse.pass.support.client.model.SubmissionEvent;
import org.springframework.util.ReflectionUtils;

public class JsonMetadataBuilder {

    private JsonMetadataBuilder() {}

    public static String resourceMetadata(Submission submission, ObjectMapper mapper) {
        String metadata = submission.getMetadata();
        if (metadata == null || metadata.trim().length() == 0) {
            return "{}";
        }
        JsonNode metadataNode;
        try {
            metadataNode = mapper.readTree(metadata);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ObjectNode resourceMetadata = mapper.createObjectNode();
        resourceMetadata.put("title", field("title", metadataNode).orElse(""));
        resourceMetadata.put("journal-title", field("journal-title", metadataNode).orElse(""));
        resourceMetadata.put("volume", field("volume", metadataNode).orElse(""));
        resourceMetadata.put("issue", field("issue", metadataNode).orElse(""));
        resourceMetadata.put("abstract", field("abstract", metadataNode).orElse(""));
        resourceMetadata.put("doi", field("doi", metadataNode).orElse(""));
        resourceMetadata.put("publisher", field("publisher", metadataNode).orElse(""));
        resourceMetadata.put("authors", field("authors", metadataNode).orElse(""));
        return resourceMetadata.toString();
    }

    public static String eventMetadata(SubmissionEvent event, ObjectMapper mapper) {
        ObjectNode eventMetadata = mapper.createObjectNode();
        eventMetadata.put("id", field("id", event).orElse(""));
        eventMetadata.put("comment", field("comment", event).orElse(""));
        eventMetadata.put("performedDate", field("performedDate", event).orElse(""));
        eventMetadata.put("performedBy", field("performedBy", event).orElse(""));
        eventMetadata.put("performerRole", field("performerRole", event).orElse(""));
        eventMetadata.put("performedDate", field("performedDate", event).orElse(""));
        eventMetadata.put("eventType", field("eventType", event).orElse(""));
        return eventMetadata.toString();
    }

    private static Optional<String> field(String fieldname, JsonNode metadata) {
        Optional<JsonNode> node = Optional.ofNullable(metadata.findValue(fieldname));
        if (node.isPresent() && node.get().isArray()) {
            return node.map(Objects::toString);
        }
        return node.map(JsonNode::asText);
    }

    private static Optional<String> field(String fieldname, SubmissionEvent event) {
        Optional<Field> field = Optional.ofNullable(ReflectionUtils.findField(SubmissionEvent.class, fieldname));
        return field
            .map(f -> {
                ReflectionUtils.makeAccessible(f);
                return f;
            })
            .map(f -> ReflectionUtils.getField(f, event))
            .map(Object::toString);
    }

}
