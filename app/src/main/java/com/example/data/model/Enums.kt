package com.example.data.model

enum class UserRole {
    CUSTOMER,
    COURIER,
    ADMIN
}

enum class RequestType {
    BUY_FOR_ME,    // Courier buys it and pays upfront, customer reimburses with cash on delivery
    PICKUP_ONLY    // Customer already ordered & paid or reserved; courier picks it up
}

enum class OrderStatus {
    REQUESTED,     // Order sent to courier, pending acceptance
    ACCEPTED,      // Courier accepted the order
    AT_SHOP,       // Courier arrived at the merchant / restaurant
    PURCHASED,     // Courier purchased the items or picked up package
    ON_THE_WAY,    // Courier en route to customer pin
    DELIVERED,     // Order delivered, cash collected
    CANCELLED      // Order declined or cancelled
}

enum class AppLanguage {
    DARIJA,
    FRENCH
}
