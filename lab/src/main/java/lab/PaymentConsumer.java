package lab;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentConsumer {

    private static final Logger log = LoggerFactory.getLogger(PaymentConsumer.class);

    @KafkaListener(topics = "${app.topic.payments}", groupId = "payment-core")
    public void onPayment(ConsumerRecord<String, PaymentEvent> record) {
        log.info(
                "consumed partition={} offset={} key={} value={}",
                record.partition(),
                record.offset(),
                record.key(),
                record.value()
        );
        // Production: check paymentId unique key before posting. Redelivery must be a no-op.
    }
}
