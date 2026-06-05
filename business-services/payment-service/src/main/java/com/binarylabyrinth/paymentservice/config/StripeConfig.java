package com.binarylabyrinth.paymentservice.config;

import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * StripeConfig - initializes the Stripe SDK at startup.
 *
 * The Stripe Java SDK reads its API key from the static field {@code Stripe.apiKey}.
 * We set it once here from {@code stripe.api.key} (env: STRIPE_API_KEY). Because
 * it's a global/static, all Charge/Refund calls in the service use this key.
 *
 * Use a {@code sk_test_...} key for development; never commit a live key.
 */
@Configuration
public class StripeConfig {

    public StripeConfig(@Value("${stripe.api.key}") String apiKey) {
        Stripe.apiKey = apiKey;
    }
}
