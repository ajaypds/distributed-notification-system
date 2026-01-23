package com.example.notificationdispatcher.consumer;

//import com.example.notificationdispatcher.event.NotificationEvent;
import com.example.contract.NotificationEvent;
import com.example.notificationdispatcher.service.DispatcherService;
import lombok.AllArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class NotificationEventConsumer {

    private final DispatcherService dispatcherService;

//    public NotificationEventConsumer(DispatcherService dispatcherService) {
//        this.dispatcherService = dispatcherService;
//    }

    @KafkaListener(topics = "notification-events")
    public void consume(NotificationEvent event) {

        dispatcherService.process(event);
    }
}
