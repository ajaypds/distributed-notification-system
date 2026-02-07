package com.example.notificationdispatcher.dlq;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dlq")
public class DlqReplayController {

    private final DlqReplayService replayService;

    public DlqReplayController(DlqReplayService replayService) {
        this.replayService = replayService;
    }

    @PostMapping("/replay")
    public ResponseEntity<String> replayAll() {

        int count = replayService.replayAll();

        return ResponseEntity.ok(
                "Replayed " + count + " DLQ events"
        );
    }
}
