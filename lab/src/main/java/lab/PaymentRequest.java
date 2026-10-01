package lab;

public record PaymentRequest(
        String accountId,
        double amount,
        String currency,
        String type
) {
}
