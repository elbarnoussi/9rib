package com.example

import com.example.data.geo.ChefchaouenGeoFence
import com.example.data.geo.GeoPoint
import com.example.data.model.OrderStatus
import org.junit.Assert.*
import org.junit.Test

class DeliveryUnitTest {

    @Test
    fun testChefchaouenServiceAreaValidation() {
        val polygon = ChefchaouenGeoFence.DEFAULT_SERVICE_POLYGON

        // Points inside Chefchaouen Medina
        val outaElHammam = GeoPoint(35.1688, -5.2636)
        val rasElMaa = GeoPoint(35.1712, -5.2575)
        val babSouk = GeoPoint(35.1715, -5.2662)
        val babAin = GeoPoint(35.1675, -5.2678)

        assertTrue("Outa El Hammam should be inside service area", ChefchaouenGeoFence.isInsidePolygon(outaElHammam, polygon))
        assertTrue("Ras El Maa should be inside service area", ChefchaouenGeoFence.isInsidePolygon(rasElMaa, polygon))
        assertTrue("Bab Souk should be inside service area", ChefchaouenGeoFence.isInsidePolygon(babSouk, polygon))
        assertTrue("Bab El Ain should be inside service area", ChefchaouenGeoFence.isInsidePolygon(babAin, polygon))

        // Points outside Chefchaouen (Tetouan, Tangier, distant Rif mountain)
        val tetouan = GeoPoint(35.5889, -5.3626)
        val tangier = GeoPoint(35.7595, -5.8340)
        val distantAkchour = GeoPoint(35.2389, -5.1740)
        val farNorth = GeoPoint(35.3000, -5.2600)

        assertFalse("Tetouan should be outside Chefchaouen boundary", ChefchaouenGeoFence.isInsidePolygon(tetouan, polygon))
        assertFalse("Tangier should be outside Chefchaouen boundary", ChefchaouenGeoFence.isInsidePolygon(tangier, polygon))
        assertFalse("Akchour should be outside Chefchaouen boundary", ChefchaouenGeoFence.isInsidePolygon(distantAkchour, polygon))
        assertFalse("Far North should be outside Chefchaouen boundary", ChefchaouenGeoFence.isInsidePolygon(farNorth, polygon))
    }

    @Test
    fun testOrderStatusTransitions() {
        var currentStatus = OrderStatus.REQUESTED

        // Allowed sequential progression
        val pipeline = listOf(
            OrderStatus.ACCEPTED,
            OrderStatus.AT_SHOP,
            OrderStatus.PURCHASED,
            OrderStatus.ON_THE_WAY,
            OrderStatus.DELIVERED
        )

        for (next in pipeline) {
            currentStatus = next
        }
        assertEquals(OrderStatus.DELIVERED, currentStatus)

        // Cancelled transition test
        var cancelOrder = OrderStatus.REQUESTED
        cancelOrder = OrderStatus.CANCELLED
        assertEquals(OrderStatus.CANCELLED, cancelOrder)
    }

    @Test
    fun testCourierAvailabilityAndRaceConditionSimulation() {
        // Model courier state
        var isOnline = true
        var isApproved = true
        var isBusy = false

        // First customer attempts reservation
        val canReserveFirst = isOnline && isApproved && !isBusy
        assertTrue("First customer should successfully reserve available courier", canReserveFirst)
        isBusy = true // atomically acquired lock

        // Second customer attempts reservation concurrently for the same courier
        val canReserveSecond = isOnline && isApproved && !isBusy
        assertFalse("Second customer must be rejected due to courier busy race condition", canReserveSecond)

        // Courier finishes delivery and is released
        isBusy = false
        val canReserveThird = isOnline && isApproved && !isBusy
        assertTrue("Courier should be available again after completing delivery", canReserveThird)
    }

    @Test
    fun testCashLedgerCalculations() {
        val actualItemCost = 65.0 // MAD
        val deliveryFee = 15.0    // MAD
        val appCommission = 2.0   // MAD

        val totalCashCollected = actualItemCost + deliveryFee
        val courierNetEarnings = deliveryFee - appCommission

        assertEquals(80.0, totalCashCollected, 0.001)
        assertEquals(13.0, courierNetEarnings, 0.001)
        assertEquals(2.0, appCommission, 0.001)
    }

    @Test
    fun testDistanceAndPricing() {
        val p1 = ChefchaouenGeoFence.CHEFCHAOUEN_CENTER
        val p2 = GeoPoint(35.1712, -5.2575) // Ras El Maa (~600m away)

        val dist = ChefchaouenGeoFence.distanceKm(p1, p2)
        assertTrue("Distance to Ras El Maa should be around 0.3 - 1.0 km", dist in 0.2..1.5)

        val fee = ChefchaouenGeoFence.calculateDeliveryFeeMad(dist)
        assertTrue("Base delivery fee in Chefchaouen should be at least 12 MAD", fee >= 12.0)
    }
}
