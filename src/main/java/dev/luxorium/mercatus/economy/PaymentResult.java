package dev.luxorium.mercatus.economy;

public record PaymentResult(boolean success, String message) {
    public static PaymentResult ok() {
        return new PaymentResult(true, "");
    }

    public static PaymentResult failure(String message) {
        return new PaymentResult(false, message);
    }
}
