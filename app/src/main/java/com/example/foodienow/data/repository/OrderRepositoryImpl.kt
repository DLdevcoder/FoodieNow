package com.example.foodienow.data.repository

import com.example.foodienow.domain.model.Address
import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.model.OrderItemResponse
import com.example.foodienow.domain.model.OrderItemUiModel
import com.example.foodienow.domain.model.OrderStatus
import com.example.foodienow.domain.model.ShipperAcceptOrderParams
import com.example.foodienow.domain.model.Store
import com.example.foodienow.domain.repository.OrderRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

class OrderRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : OrderRepository {

    override suspend fun createOrder(order: Order): Result<Order> {
        return try {
            var updatedOrder = order

            // 1. TÌM TỌA ĐỘ QUÁN ĂN (LẤY TỪ CỘT lat VÀ lng CỦA BẢNG STORES)
            if (order.merchantLat == null || order.merchantLng == null) {
                if (order.merchantId != null) {
                    android.util.Log.d("OrderRepository", "Bắt đầu tìm tọa độ cho quán: ${order.merchantId}")

                    val store = supabaseClient.postgrest["stores"]
                        .select { filter { eq("id", order.merchantId) } }
                        .decodeSingleOrNull<Store>()

                    if (store == null) {
                        android.util.Log.e("OrderRepository", "Lỗi: Query bảng stores trả về null")
                    } else if (store.lat == null || store.lng == null) {
                        android.util.Log.e("OrderRepository", "Lỗi: Tìm thấy quán '${store.name}' nhưng cột lat=${store.lat}, lng=${store.lng}")
                    } else {
                        android.util.Log.d("OrderRepository", "Thành công: Lấy được tọa độ quán (${store.lat}, ${store.lng})")
                        updatedOrder = updatedOrder.copy(
                            merchantLat = store.lat,
                            merchantLng = store.lng
                        )
                    }
                }
            }

            // 2. TÌM TỌA ĐỘ KHÁCH HÀNG
            if (updatedOrder.deliveryLat == null || updatedOrder.deliveryLng == null) {
                try {
                    val addressList = supabaseClient.postgrest["addresses"]
                        .select {
                            filter {
                                eq("user_id", order.customerId)
                                eq("detail", order.deliveryAddress)
                            }
                        }
                        .decodeList<Address>()

                    val customerAddress = addressList.firstOrNull()
                    if (customerAddress != null) {
                        updatedOrder = updatedOrder.copy(
                            deliveryLat = customerAddress.latitude, // Lưu ý: Nếu bảng addresses dùng 'lat', hãy sửa chỗ này thành customerAddress.lat
                            deliveryLng = customerAddress.longitude
                        )
                    }
                } catch (e: Exception) {
                    android.util.Log.e("OrderRepository", "Lỗi tìm tọa độ khách: ${e.message}")
                }
            }

            // 3. LƯU ĐƠN HÀNG
            val insertedOrder = supabaseClient.postgrest["orders"]
                .insert(updatedOrder) {
                    select()
                }
                .decodeSingle<Order>()

            Result.success(insertedOrder)

        } catch (e: Exception) {
            android.util.Log.e("OrderRepository", "Lỗi tạo đơn hàng: ", e)
            Result.failure(e)
        }
    }

    override fun getMerchantOrders(merchantId: String): Flow<List<Order>> = channelFlow {
        try {
            val initialOrders = supabaseClient.postgrest["orders"]
                .select {
                    filter {
                        eq("merchant_id", merchantId)
                    }
                }
                .decodeList<Order>()
            send(initialOrders)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val channelName = "merchant_orders_channel_$merchantId"
        val channel = supabaseClient.channel(channelName)
        val insertFlow = channel.postgresChangeFlow<io.github.jan.supabase.realtime.PostgresAction.Insert>(schema = "public") {
            table = "orders"
            filter = "merchant_id=eq.$merchantId"
        }
        val updateFlow = channel.postgresChangeFlow<io.github.jan.supabase.realtime.PostgresAction.Update>(schema = "public") {
            table = "orders"
            filter = "merchant_id=eq.$merchantId"
        }

        launch {
            insertFlow.collect {
                try {
                    val updatedOrders = supabaseClient.postgrest["orders"]
                        .select {
                            filter { eq("merchant_id", merchantId) }
                        }
                        .decodeList<Order>()
                    send(updatedOrders)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        launch {
            updateFlow.collect {
                try {
                    val updatedOrders = supabaseClient.postgrest["orders"]
                        .select {
                            filter { eq("merchant_id", merchantId) }
                        }
                        .decodeList<Order>()
                    send(updatedOrders)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        channel.subscribe()

        awaitClose {
            launch {
                supabaseClient.realtime.removeChannel(channel)
            }
        }
    }

    override fun getOrdersByCustomer(customerId: String): Flow<List<Order>> = channelFlow {
        val fetchOrders = suspend {
            val orders = supabaseClient.postgrest["orders"]
                .select { filter { eq("customer_id", customerId) } }
                .decodeList<Order>()
            if (orders.isNotEmpty()) {
                val orderIds = orders.mapNotNull { it.id }
                val itemsResponse = supabaseClient.postgrest["order_items"]
                    .select(columns = Columns.raw("*, foods(name, image_url)")) {
                        filter {
                            isIn("order_id", orderIds)
                        }
                    }
                    .decodeList<OrderItemResponse>()
                val itemsByOrder = itemsResponse.groupBy { it.orderId }
                orders.map { order ->
                    val orderItems = itemsByOrder[order.id] ?: emptyList()
                    if (orderItems.isNotEmpty()) {
                        val maxItem = orderItems.maxByOrNull { it.priceAtTime }
                        val otherCount = orderItems.size - 1
                        order.copy(
                            previewFoodName = maxItem?.foods?.name,
                            previewImageUrl = maxItem?.foods?.imageUrl,
                            otherItemsCount = otherCount
                        )
                    } else {
                        order
                    }
                }
            } else {
                orders
            }
        }

        send(fetchOrders())

        val channelName = "orders_customer_$customerId"
        val channel = supabaseClient.channel(channelName)
        val changes = channel.postgresChangeFlow<PostgresAction>("public") {
            table = "orders"
            filter = "customer_id=eq.$customerId"
        }

        launch {
            changes.collect {
                send(fetchOrders())
            }
        }

        channel.subscribe()

        awaitClose {
            launch {
                supabaseClient.realtime.removeChannel(channel)
            }
        }
    }

    override fun getAvailableDeliveries(): Flow<List<Order>> = channelFlow {
        val fetchOrders = suspend {
            supabaseClient.postgrest["orders"]
                .select {
                    filter {
                        eq("status", OrderStatus.WAITING_SHIPPER.name)
                    }
                }
                .decodeList<Order>()
                .filter { it.shipperId == null }
        }

        send(fetchOrders())

        val channelName = "orders_shipper_available"
        val channel = supabaseClient.channel(channelName)
        val changes = channel.postgresChangeFlow<PostgresAction>("public") {
            table = "orders"
            filter = "status=eq.${OrderStatus.WAITING_SHIPPER.name}"
        }

        launch {
            changes.collect {
                send(fetchOrders())
            }
        }

        channel.subscribe()

        awaitClose {
            launch {
                supabaseClient.realtime.removeChannel(channel)
            }
        }
    }

    override fun getShipperActiveOrder(shipperId: String): Flow<List<Order>> = channelFlow {
        val fetchActive = suspend {
            try {
                supabaseClient.postgrest["orders"]
                    .select(columns = Columns.raw("*")) {
                        filter {
                            eq("shipper_id", shipperId)
                            isIn("status", listOf(OrderStatus.PICKING_UP.name, OrderStatus.DELIVERING.name))
                        }
                    }
                    .decodeList<Order>()
            } catch (e: Exception) {
                android.util.Log.e("OrderRepository", "Lỗi tải đơn active: ${e.message}")
                emptyList<Order>()
            }
        }

        send(fetchActive())

        val channelName = "orders_shipper_active_$shipperId"
        val channel = supabaseClient.channel(channelName)
        val changes = channel.postgresChangeFlow<PostgresAction>("public") {
            table = "orders"
            // Bổ sung filter này để tránh làm ngập băng thông
            filter = "shipper_id=eq.$shipperId"
        }

        launch {
            changes.collect {
                send(fetchActive())
            }
        }

        channel.subscribe()
        awaitClose {
            launch { supabaseClient.realtime.removeChannel(channel) }
        }
    }

    override suspend fun acceptOrder(orderId: String, shipperId: String, lat: Double, lng: Double): Result<Unit> {
        return shipperAcceptOrder(orderId, shipperId, lat, lng)
    }

    override suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Result<Unit> {
        return try {
            supabaseClient.postgrest["orders"].update(
                {
                    set("status", newStatus.name)
                }
            ) {
                filter {
                    eq("id", orderId)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getShipperCompletedOrders(shipperId: String): Flow<List<Order>> = channelFlow {
        val fetchCompleted = suspend {
            supabaseClient.postgrest["orders"]
                .select {
                    filter {
                        eq("shipper_id", shipperId)
                        isIn(
                            "status",
                            listOf(
                                OrderStatus.COMPLETED.name,
                                OrderStatus.CANCELLED_BY_CUSTOMER.name,
                                OrderStatus.CANCELLED_BY_STORE.name,
                                OrderStatus.NO_SHIPPER_FOUND.name,
                                OrderStatus.PAYMENT_FAILED.name,
                                OrderStatus.DELIVERY_TIMEOUT.name
                            )
                        )
                    }
                }
                .decodeList<Order>()
        }

        send(fetchCompleted())

        val channelName = "orders_shipper_completed_$shipperId"
        val channel = supabaseClient.channel(channelName)
        val changes = channel.postgresChangeFlow<PostgresAction>("public") {
            table = "orders"
            filter = "shipper_id=eq.$shipperId"
        }

        launch {
            changes.collect { send(fetchCompleted()) }
        }

        channel.subscribe()
        awaitClose { launch { supabaseClient.realtime.removeChannel(channel) } }
    }

    override suspend fun getOrderItemsByOrderId(orderId: String): List<OrderItemUiModel> {
        return try {
            val response = supabaseClient.postgrest["order_items"]
                .select(columns = Columns.raw("*, foods(name, image_url)")) {
                    filter { eq("order_id", orderId) }
                }
                .decodeList<OrderItemResponse>()

            response.map { item ->
                OrderItemUiModel(
                    id = item.id,
                    orderId = item.orderId,
                    foodId = item.foodId,
                    quantity = item.quantity,
                    priceAtTime = item.priceAtTime,
                    foodName = item.foods?.name ?: "Món ăn không xác định",
                    foodImageUrl = item.foods?.imageUrl
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    override suspend fun updateShipperLocation(orderId: String, lat: Double, lng: Double): Result<Unit> {
        return try {
            supabaseClient.postgrest["orders"].update(
                {
                    set("shipper_lat", lat)
                    set("shipper_lng", lng)
                }
            ) {
                filter { eq("id", orderId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOrderById(orderId: String): Order? {
        return try {
            supabaseClient.postgrest["orders"]
                .select {
                    filter {
                        eq("id", orderId)
                    }
                }
                .decodeSingleOrNull<Order>()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun confirmShipperDelivery(orderId: String): Result<Unit> {
        return try {
            supabaseClient.postgrest["orders"].update(
                { set("shipper_confirmed", true) }
            ) { filter { eq("id", orderId) } }

            checkAndCompleteOrder(orderId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun confirmCustomerReceipt(orderId: String): Result<Unit> {
        return try {
            supabaseClient.postgrest["orders"].update(
                { set("customer_confirmed", true) }
            ) { filter { eq("id", orderId) } }

            checkAndCompleteOrder(orderId)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun checkAndCompleteOrder(orderId: String): Result<Unit> {
        return try {
            val order = getOrderById(orderId)

            if (order != null && order.shipperConfirmed && order.customerConfirmed) {
                shipperCompleteDelivery(orderId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelOrderShipper(orderId: String): Result<Unit> {
        return shipperCancelOrder(orderId)
    }

    override suspend fun merchantAcceptOrderWithLocation(orderId: String, merchantId: String): Result<Unit> {
        return try {
            val store = supabaseClient.postgrest["stores"]
                .select { filter { eq("id", merchantId) } }
                .decodeSingleOrNull<Store>()

            var lat: Double? = null
            var lng: Double? = null

            if (store?.ownerId != null) {
                val ownerAddresses = supabaseClient.postgrest["addresses"]
                    .select { filter { eq("user_id", store.ownerId) } }
                    .decodeList<Address>()
                val storeAddress = ownerAddresses.firstOrNull { it.isDefault } ?: ownerAddresses.firstOrNull()

                if (storeAddress != null) {
                    lat = storeAddress.latitude
                    lng = storeAddress.longitude
                }
            }

            supabaseClient.postgrest["orders"].update(
                {
                    if (lat != null && lng != null) {
                        set("merchant_lat", lat)
                        set("merchant_lng", lng)
                    }
                }
            ) {
                filter { eq("id", orderId) }
            }
            storeConfirmOrder(orderId)
        } catch (e: Exception) {
            android.util.Log.e("OrderRepository", "Lỗi khi Merchant nhận đơn: ", e)
            Result.failure(e)
        }
    }

    override suspend fun cancelOrderWithReason(orderId: String, reason: String, cancelledBy: String): Result<Unit> {
        return try {
            supabaseClient.postgrest["orders"].update(
                {
                    set("status", OrderStatus.CANCELLED_BY_CUSTOMER.name)
                    set("cancellation_reason", reason)
                    set("cancelled_by", cancelledBy)
                }
            ) {
                filter { eq("id", orderId) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("OrderRepository", "Lỗi khi hủy đơn: ", e)
            Result.failure(e)
        }
    }

    override suspend fun storeConfirmOrder(orderId: String): Result<Unit> {
        return try {
            supabaseClient.postgrest.rpc(
                function = "store_confirm_order",
                parameters = mapOf("p_order_id" to orderId)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun storeRejectOrder(orderId: String, reason: String): Result<Unit> {
        return try {
            supabaseClient.postgrest.rpc(
                function = "store_reject_order",
                parameters = mapOf(
                    "p_order_id" to orderId,
                    "p_reason" to reason
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun storeMarkReady(orderId: String): Result<Unit> {
        return try {
            supabaseClient.postgrest.rpc(
                function = "store_mark_ready",
                parameters = mapOf("p_order_id" to orderId)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun shipperAcceptOrder(orderId: String, shipperId: String, lat: Double, lng: Double): Result<Unit> {
        return try {
            // Thay thế mapOf bằng data class vừa tạo
            val params = ShipperAcceptOrderParams(
                orderId = orderId,
                shipperId = shipperId,
                shipperLat = lat,
                shipperLng = lng
            )

            supabaseClient.postgrest.rpc(
                function = "shipper_accept_order",
                parameters = params
            )
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("OrderRepository", "Lỗi RPC nhận đơn: ", e)
            Result.failure(e)
        }
    }

    override suspend fun shipperCancelOrder(orderId: String): Result<Unit> {
        return try {
            val params = OrderIdParam(orderId = orderId)
            supabaseClient.postgrest.rpc(
                function = "shipper_cancel_order",
                parameters = params
            )
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("OrderRepository", "Lỗi hủy đơn: ", e)
            Result.failure(e)
        }
    }

    override suspend fun shipperCompleteDelivery(orderId: String): Result<Unit> {
        return try {
            updateOrderStatus(orderId, OrderStatus.COMPLETED)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun confirmOnlinePayment(orderId: String, transactionId: String): Result<Unit> {
        return try {
            supabaseClient.postgrest.rpc(
                function = "handle_bank_transfer_payment_v2",
                parameters = mapOf(
                    "p_order_id" to orderId,
                    "p_transaction_id" to transactionId
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun handlePaymentFailure(orderId: String): Result<Unit> {
        return try {
            supabaseClient.postgrest.rpc(
                function = "handle_payment_failure",
                parameters = mapOf("p_order_id" to orderId)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Serializable
data class OrderIdParam(
    @SerialName("p_order_id") val orderId: String
)