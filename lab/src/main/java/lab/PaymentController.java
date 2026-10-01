package lab;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentProducer producer;

    public PaymentController(PaymentProducer producer) {
        this.producer = producer;
    }

    @PostMapping
    public PaymentEvent create(@RequestBody PaymentRequest request) throws ExecutionException, InterruptedException {
        PaymentEvent event = new PaymentEvent(
                UUID.randomUUID().toString(),
                request.accountId(),
                request.amount(),
                request.currency(),
                request.type()
        );
        producer.publish(event);
        return event;
    }
}
