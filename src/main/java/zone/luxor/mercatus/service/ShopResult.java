package zone.luxor.mercatus.service;

public record ShopResult<T>(boolean success, ShopFailure failure, T value, String message) {
    public static <T> ShopResult<T> success(T value, String message) {
        return new ShopResult<>(true, ShopFailure.NONE, value, message);
    }

    public static <T> ShopResult<T> failure(ShopFailure failure, String message) {
        return new ShopResult<>(false, failure, null, message);
    }
}
