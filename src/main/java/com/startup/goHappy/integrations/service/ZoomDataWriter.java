package com.startup.goHappy.integrations.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.startup.goHappy.integrations.model.ZoomParticipantsDTO.Participant;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class ZoomDataWriter {
    private final ObjectMapper objectMapper;
    private static final String BASE_DIRECTORY = "data/zoom-participants";

    public ZoomDataWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        createDirectoryIfNotExists();
    }

    private void createDirectoryIfNotExists() {
        try {
            Files.createDirectories(Paths.get(BASE_DIRECTORY));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory for participant data", e);
        }
    }

    /**
     * Saves participant data in JSON format with event ID as header
     */
    public void saveAsJson(String eventId, List<Participant> participants) {
        try {
            String filename = generateFilename("json",eventId);
            Path filePath = Paths.get(BASE_DIRECTORY, filename);

            ObjectNode eventData = objectMapper.createObjectNode();
            eventData.put("eventId", eventId);
            eventData.put("timestamp", LocalDateTime.now().toString());
            ArrayNode participantsArray = objectMapper.valueToTree(participants);
            eventData.set("participants", participantsArray);

            ArrayNode eventsArray;
            if (Files.exists(filePath)) {
                eventsArray = objectMapper.readValue(filePath.toFile(), ArrayNode.class);
            } else {
                eventsArray = objectMapper.createArrayNode();
            }

            eventsArray.add(eventData);
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(filePath.toFile(), eventsArray);

        } catch (IOException e) {
            throw new RuntimeException("Failed to save participant data as JSON", e);
        }
    }

    /**
     * Saves participant data in CSV format with event ID column
     */
    public void saveAsCsv(String eventId, List<Participant> participants) {
        try {
            String filename = generateFilename("csv",eventId);
            Path filePath = Paths.get(BASE_DIRECTORY, filename);
            List<String> lines = new ArrayList<>();

            // Add headers if file doesn't exist
            if (!Files.exists(filePath)) {
                String headers = "timestamp,name,email,join_time,leave_time,duration,status,participant_id,user_id";
                lines.add(headers);
            }

            // Add participant data
            String timestamp = LocalDateTime.now().toString();
            for (Participant participant : participants) {
                String line = String.format("%s,%s,%s,%s,%s,%d,%s,%s,%s",
//                        escapeCsvField(eventId),
                        escapeCsvField(timestamp),
                        escapeCsvField(participant.getName()),
                        escapeCsvField(participant.getUser_email()),
                        escapeCsvField(participant.getJoin_time()),
                        escapeCsvField(participant.getLeave_time()),
                        participant.getDuration(),
                        escapeCsvField(participant.getStatus().toString()),
                        escapeCsvField(participant.getId()),
                        escapeCsvField(participant.getUser_id())
                );
                lines.add(line);
            }

            // Write to file (append mode)
            Files.write(filePath, lines,
                    Files.exists(filePath) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);

        } catch (IOException e) {
            throw new RuntimeException("Failed to save participant data as CSV", e);
        }
    }

    /**
     * Saves participant data in text format with event ID as header
     */
    public void saveAsText(String eventId, List<Participant> participants) {
        try {
            String filename = generateFilename("txt",eventId);
            Path filePath = Paths.get(BASE_DIRECTORY, filename);

            List<String> lines = new ArrayList<>();

            // Add event header with separator
            lines.add("\n=== Event ID: " + eventId + " ===");
            lines.add("Timestamp: " + LocalDateTime.now());
            lines.add("Number of Participants: " + participants.size());
            lines.add("-".repeat(50));

            // Add participant details
            for (Participant participant : participants) {
                lines.add(String.format("""
                    Name: %s
                    Email: %s
                    Join Time: %s
                    Leave Time: %s
                    Duration: %d minutes
                    Status: %s
                    """,
                        participant.getName(),
                        participant.getUser_email(),
                        participant.getJoin_time(),
                        participant.getLeave_time(),
                        participant.getDuration(),
                        participant.getStatus()
                ));
                lines.add("-".repeat(30));
            }

            // Write to file (append mode)
            Files.write(filePath, lines,
                    Files.exists(filePath) ? StandardOpenOption.APPEND : StandardOpenOption.CREATE);

        } catch (IOException e) {
            throw new RuntimeException("Failed to save participant data as text", e);
        }
    }

    private String generateFilename(String extension,String eventId) {
//        String date = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return String.format("zoom_participants_%s.%s", eventId, extension);
    }

    private String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }
}