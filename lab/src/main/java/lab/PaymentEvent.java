package lab;

public record PaymentEvent(
        String paymentId,
        String accountId,
        double amount,
        String currency,
        String type
) {
}
