// DataController.java
package com.example.onLineCode;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class DataController {

    private final Judge0Client judge0Client;

    public DataController(Judge0Client judge0Client) {
        this.judge0Client = judge0Client;
    }

    @PostMapping("/data")
    public ResponseEntity<Map<String, String>> receiveData(@RequestBody Map<String, Object> payload) {
        // Expecting: language_id (Integer), source_code (String, base64), stdin (String, base64)
        Integer languageId = (Integer) payload.get("language_id");
        String sourceCode = (String) payload.get("source_code");
        String stdin = (String) payload.get("stdin");

        // Pass base64-decoded values to Judge0Client (it will re-encode, but that's fine for now)
        byte[] decodedSource = Base64.getDecoder().decode(sourceCode);
        byte[] decodedStdin = Base64.getDecoder().decode(stdin);
        String token = judge0Client.createSubmission(languageId, new String(decodedSource), new String(decodedStdin));
        System.out.println("got token");
        try {Thread.sleep(5000);} catch (InterruptedException e) {}
        String judge0Response = judge0Client.getSubmission(token);
        System.out.println(judge0Response);
        String decodedStdout = "";
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode map = objectMapper.readTree(judge0Response);
            String stdoutBase64 = map.has("stdout") && !map.get("stdout").isNull() ? map.get("stdout").asText() : "";
            if (!stdoutBase64.isEmpty()) {
                byte[] stdoutBytes = Base64.getDecoder().decode(stdoutBase64);
                decodedStdout = new String(stdoutBytes);
            }
        } catch (Exception e) {
            decodedStdout = "Error decoding stdout: " + e.getMessage();
        }
        Map<String, String> response = Map.of(
                "status", "success",
                "stdout", decodedStdout
        );
        return ResponseEntity.ok(response);
    }

}
