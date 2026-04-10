package com.example.heritagehub.data

import com.example.heritagehub.model.Artwork
import com.example.heritagehub.model.CartItem
import com.example.heritagehub.model.CheckoutPreferences
import com.example.heritagehub.model.CartSummary
import com.example.heritagehub.model.Order
import com.example.heritagehub.util.CartPricing
import com.example.heritagehub.util.PriceUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class CartRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be logged in to access cart")
    }

    private fun cartCollection(userId: String) = firestore
        .collection("users")
        .document(userId)
        .collection("cart")

    private fun ordersCollection(userId: String) = firestore
        .collection("users")
        .document(userId)
        .collection("orders")

    private fun checkoutPrefsDoc(userId: String) = firestore
        .collection("users")
        .document(userId)
        .collection("checkout_preferences")
        .document("default")

    suspend fun getCartItems(): List<CartItem> {
        val userId = requireUserId()
        val snapshot = cartCollection(userId)
            .orderBy("updatedAt")
            .get()
            .await()

        return snapshot.documents.map { doc ->
            CartItem(
                artworkId = doc.id,
                title = doc.getString("title").orEmpty(),
                artistName = doc.getString("artistName").orEmpty(),
                imageUrl = doc.getString("imageUrl").orEmpty(),
                artistId = doc.getString("artistId").orEmpty(),
                priceDisplay = doc.getString("priceDisplay").orEmpty(),
                unitPrice = doc.getDouble("unitPrice") ?: 0.0,
                quantity = (doc.getLong("quantity") ?: 1L).toInt().coerceAtLeast(1),
                updatedAt = doc.getLong("updatedAt") ?: 0L
            )
        }.reversed()
    }

    suspend fun addToCart(artwork: Artwork, quantity: Int = 1) {
        val userId = requireUserId()
        val itemRef = cartCollection(userId).document(artwork.id)
        val unitPrice = PriceUtils.parsePriceToDouble(artwork.price)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(itemRef)
            val existingQuantity = (snapshot.getLong("quantity") ?: 0L).toInt()
            val finalQuantity = (existingQuantity + quantity).coerceAtLeast(1)

            val payload = mapOf(
                "artworkId" to artwork.id,
                "title" to artwork.title,
                "artistName" to artwork.artistName,
                "imageUrl" to artwork.imageUrl,
                "artistId" to artwork.artistId,
                "priceDisplay" to artwork.price,
                "unitPrice" to unitPrice,
                "quantity" to finalQuantity,
                "updatedAt" to System.currentTimeMillis(),
                "createdAt" to (snapshot.getLong("createdAt") ?: System.currentTimeMillis())
            )

            transaction.set(itemRef, payload, SetOptions.merge())
        }.await()
    }

    suspend fun updateQuantity(artworkId: String, quantity: Int) {
        val userId = requireUserId()
        val itemRef = cartCollection(userId).document(artworkId)

        if (quantity <= 0) {
            itemRef.delete().await()
            return
        }

        itemRef.update(
            mapOf(
                "quantity" to quantity,
                "updatedAt" to System.currentTimeMillis()
            )
        ).await()
    }

    suspend fun removeFromCart(artworkId: String) {
        val userId = requireUserId()
        cartCollection(userId).document(artworkId).delete().await()
    }

    suspend fun clearCart() {
        val userId = requireUserId()
        val snapshot = cartCollection(userId).get().await()
        if (snapshot.isEmpty) return

        val batch = firestore.batch()
        snapshot.documents.forEach { doc ->
            batch.delete(doc.reference)
        }
        batch.commit().await()
    }

    fun calculateSummary(items: List<CartItem>): CartSummary {
        return CartPricing.calculateSummary(items)
    }

    suspend fun saveCheckoutPreferences(preferences: CheckoutPreferences) {
        val userId = requireUserId()
        checkoutPrefsDoc(userId).set(
            mapOf(
                "fullName" to preferences.fullName,
                "phoneNumber" to preferences.phoneNumber,
                "addressLine1" to preferences.addressLine1,
                "addressLine2" to preferences.addressLine2,
                "city" to preferences.city,
                "state" to preferences.state,
                "pincode" to preferences.pincode,
                "paymentMethod" to preferences.paymentMethod,
                "updatedAt" to System.currentTimeMillis()
            ),
            SetOptions.merge()
        ).await()
    }

    suspend fun getCheckoutPreferences(): CheckoutPreferences {
        val userId = requireUserId()
        val doc = checkoutPrefsDoc(userId).get().await()
        if (!doc.exists()) return CheckoutPreferences()

        return CheckoutPreferences(
            fullName = doc.getString("fullName").orEmpty(),
            phoneNumber = doc.getString("phoneNumber").orEmpty(),
            addressLine1 = doc.getString("addressLine1").orEmpty(),
            addressLine2 = doc.getString("addressLine2").orEmpty(),
            city = doc.getString("city").orEmpty(),
            state = doc.getString("state").orEmpty(),
            pincode = doc.getString("pincode").orEmpty(),
            paymentMethod = doc.getString("paymentMethod").orEmpty().ifBlank { "Cash on Delivery" }
        )
    }

    suspend fun createOrderFromCart(
        items: List<CartItem>,
        summary: CartSummary,
        preferences: CheckoutPreferences
    ): String {
        val userId = requireUserId()
        if (items.isEmpty()) {
            throw IllegalStateException("Cart is empty")
        }
        if (!preferences.isValid()) {
            throw IllegalStateException("Please add delivery address and payment method")
        }

        val orderPayload = mapOf(
            "status" to "placed",
            "itemCount" to summary.itemCount,
            "subtotal" to summary.subtotal,
            "deliveryFee" to summary.deliveryFee,
            "tax" to summary.tax,
            "total" to summary.total,
            "fullName" to preferences.fullName,
            "phoneNumber" to preferences.phoneNumber,
            "deliveryAddress" to preferences.formattedAddress(),
            "paymentMethod" to preferences.paymentMethod,
            "items" to items.map {
                mapOf(
                    "artworkId" to it.artworkId,
                    "title" to it.title,
                    "artistName" to it.artistName,
                    "artistId" to it.artistId,
                    "priceDisplay" to it.priceDisplay,
                    "unitPrice" to it.unitPrice,
                    "quantity" to it.quantity,
                    "lineTotal" to it.lineTotal
                )
            },
            "createdAt" to FieldValue.serverTimestamp()
        )

        val orderDoc = ordersCollection(userId)
            .add(orderPayload)
            .await()

        clearCart()
        return orderDoc.id
    }

    suspend fun getOrders(): List<Order> {
        val userId = requireUserId()
        val snapshot = ordersCollection(userId)
            .orderBy("createdAt")
            .get()
            .await()

        return snapshot.documents.map { doc ->
            val items = doc.get("items") as? List<Map<String, Any?>> ?: emptyList()
            val firstItemTitle = items.firstOrNull()?.get("title")?.toString().orEmpty()
            val createdAtRaw = doc.getTimestamp("createdAt")?.toDate()?.time
                ?: doc.getLong("createdAt")
                ?: 0L

            Order(
                id = doc.id,
                status = doc.getString("status").orEmpty().ifBlank { "placed" },
                itemCount = (doc.getLong("itemCount") ?: 0L).toInt(),
                total = doc.getDouble("total") ?: 0.0,
                createdAt = createdAtRaw,
                deliveringTo = doc.getString("deliveryAddress").orEmpty(),
                paymentMethod = doc.getString("paymentMethod").orEmpty(),
                firstItemTitle = firstItemTitle
            )
        }.reversed()
    }
}



