package com.auro.auro.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response wrapper cho Shipping API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public static <T> ShippingResponse<T> success(T data, String message) {
        return new ShippingResponse<>(true, message, data);
    }

    public static <T> ShippingResponse<T> error(String message) {
        return new ShippingResponse<>(false, message, null);
    }
}
