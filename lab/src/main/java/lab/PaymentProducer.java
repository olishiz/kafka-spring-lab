package lab;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

@Service
public class PaymentProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private final String topic;

    public PaymentProducer(
            KafkaTemplate<String, PaymentEvent> kafkaTemplate,
            @Value("${app.topic.payments}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publish(PaymentEvent event) throws ExecutionException, InterruptedException {
        // Key is accountId so all events for one account stay on one partition, in order.
        kafkaTemplate.send(topic, event.accountId(), event).get();
    }
}
