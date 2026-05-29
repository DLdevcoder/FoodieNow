package com.example.foodienow.feature.activity

import com.example.foodienow.R
import com.example.foodienow.domain.model.Order
import com.example.foodienow.domain.model.OrderItemUiModel
import com.example.foodienow.domain.model.OrderStatus
import com.example.foodienow.domain.model.Payment
import com.example.foodienow.domain.model.PaymentMethod
import com.example.foodienow.domain.model.PaymentStatus
import com.example.foodienow.domain.model.Review
import com.example.foodienow.domain.model.ReviewUiModel
import com.example.foodienow.domain.model.User
import com.example.foodienow.domain.model.UserRole
import com.example.foodienow.domain.model.WalletTransaction
import com.example.foodienow.domain.model.WalletTransactionType
import com.example.foodienow.domain.model.SystemSetting
import com.example.foodienow.domain.repository.AuthRepository
import com.example.foodienow.domain.repository.OrderRepository
import com.example.foodienow.domain.repository.PaymentRepository
import com.example.foodienow.domain.repository.ReviewRepository
import com.example.foodienow.domain.repository.AtomicPaymentRequest
import com.example.foodienow.domain.repository.AtomicPaymentResult
import com.example.foodienow.data.repository.MockWalletTransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityHistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var authRepository: FakeAuthRepository
    private lateinit var orderRepository: FakeOrderRepository
    private lateinit var paymentRepository: FakePaymentRepository
    private lateinit var reviewRepository: FakeReviewRepository
    private lateinit var walletTransactionRepository: MockWalletTransactionRepository
    private lateinit var viewModel: ActivityHistoryViewModel

    private val testUser = User(
        id = "user123",
        name = "Test User",
        email = "test@example.com",
        role = UserRole.CUSTOMER
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        orderRepository = FakeOrderRepository()
        paymentRepository = FakePaymentRepository()
        reviewRepository = FakeReviewRepository()
        walletTransactionRepository = MockWalletTransactionRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadHistory_whenUserLoggedInAndHasData_mergesAndSortsCorrectly() = runTest {
        authRepository.authStateFlow = flowOf(testUser)

        orderRepository.ordersFlow = flowOf(
            listOf(
                Order(
                    id = "order1",
                    customerId = "user123",
                    totalPrice = 15000L,
                    status = OrderStatus.COMPLETED,
                    deliveryAddress = "123 Street",
                    createdAt = "2026-05-24T10:00:00Z"
                )
            )
        )

        paymentRepository.paymentsFlow = flowOf(
            listOf(
                Payment(
                    id = "payment1",
                    customerId = "user123",
                    orderId = "order1",
                    amount = 15000L,
                    method = PaymentMethod.FOODIE_PAY,
                    status = PaymentStatus.SUCCESS,
                    deliveryAddress = "123 Street",
                    createdAt = "2026-05-24T10:05:00Z"
                )
            )
        )

        reviewRepository.reviewsFlow = flowOf(
            listOf(
                Review(
                    id = "review1",
                    orderId = "order1",
                    customerId = "user123",
                    foodId = "food1",
                    rating = 5,
                    comment = "Delicious",
                    createdAt = "2026-05-24T10:10:00Z",
                    foodName = "Pho"
                )
            )
        )

        walletTransactionRepository.addTransaction(
            WalletTransaction(
                id = "txn1",
                type = WalletTransactionType.TOP_UP,
                amount = 50000L,
                description = "Top up ZaloPay",
                createdAt = "2026-05-24T09:00:00Z"
            )
        )

        viewModel = ActivityHistoryViewModel(
            authRepository,
            orderRepository,
            paymentRepository,
            reviewRepository,
            walletTransactionRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals(false, uiState.isLoading)
        assertNull(uiState.errorResId)
        assertEquals(4, uiState.items.size)

        assertEquals("review-review1", uiState.items[0].id)
        assertEquals(ActivityType.REVIEW, uiState.items[0].type)

        assertEquals("payment-payment1", uiState.items[1].id)
        assertEquals(ActivityType.PAYMENT, uiState.items[1].type)

        assertEquals("order-order1", uiState.items[2].id)
        assertEquals(ActivityType.ORDER, uiState.items[2].type)

        assertEquals("wallet-tx-txn1", uiState.items[3].id)
        assertEquals(ActivityType.WALLET_TRANSACTION, uiState.items[3].type)
    }

    @Test
    fun loadHistory_whenNotLoggedIn_emitsNoSessionError() = runTest {
        authRepository.authStateFlow = flowOf(null)

        viewModel = ActivityHistoryViewModel(
            authRepository,
            orderRepository,
            paymentRepository,
            reviewRepository,
            walletTransactionRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals(false, uiState.isLoading)
        assertEquals(R.string.error_no_session, uiState.errorResId)
        assertTrue(uiState.items.isEmpty())
    }

    @Test
    fun loadHistory_whenErrorOccurs_emitsLoadError() = runTest {
        authRepository.authStateFlow = flowOf(testUser)
        orderRepository.ordersFlow = flow { throw RuntimeException("Database error") }

        viewModel = ActivityHistoryViewModel(
            authRepository,
            orderRepository,
            paymentRepository,
            reviewRepository,
            walletTransactionRepository
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals(false, uiState.isLoading)
        assertEquals(R.string.error_load_activity_history, uiState.errorResId)
    }

    private class FakeAuthRepository : AuthRepository {
        var authStateFlow: Flow<User?> = flowOf(null)

        override suspend fun login(email: String, pass: String): Result<User> = TODO()
        override suspend fun register(email: String, pass: String, role: UserRole): Result<User> = TODO()
        override suspend fun resendVerificationEmail(email: String): Result<Unit> = TODO()
        override suspend fun verifyRegistrationCode(email: String, code: String): Result<Unit> = TODO()
        override suspend fun forgotPassword(email: String): Result<Unit> = TODO()
        override suspend fun sendPasswordChangeCode(email: String): Result<Unit> = TODO()
        override suspend fun verifyPasswordChangeCode(email: String, code: String): Result<User> = TODO()
        override suspend fun logout(): Result<Unit> = TODO()
        override fun getAuthState(): Flow<User?> = authStateFlow
        override suspend fun updateBalance(amount: Long): Result<User> = TODO()
        override suspend fun updateRewardPoints(points: Int): Result<User> = TODO()
        override suspend fun updateSessionFinancials(balance: Long, rewardPoints: Int): Result<User> = TODO()
        override suspend fun changePassword(newPass: String): Result<Unit> = TODO()
        override suspend fun resolveStoredSession(): User? = null
        override suspend fun refreshSession(): Result<User> = TODO()
    }

    private class FakeOrderRepository : OrderRepository {
        var ordersFlow: Flow<List<Order>> = flowOf(emptyList())

        override suspend fun createOrder(order: Order): Result<Order> = TODO()
        override fun getMerchantOrders(merchantId: String): Flow<List<Order>> = TODO()
        override fun getOrdersByCustomer(customerId: String): Flow<List<Order>> = ordersFlow
        override fun getAvailableDeliveries(): Flow<List<Order>> = TODO()
        override fun getShipperActiveOrder(shipperId: String): Flow<List<Order>> = TODO()
        override suspend fun acceptOrder(orderId: String, shipperId: String): Result<Unit> = TODO()
        override suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Result<Unit> = TODO()
        override fun getShipperCompletedOrders(shipperId: String): Flow<List<Order>> = TODO()
        override suspend fun getOrderItemsByOrderId(orderId: String): List<OrderItemUiModel> = TODO()
        override suspend fun updateShipperLocation(orderId: String, lat: Double, lng: Double): Result<Unit> = TODO()
        override suspend fun getOrderById(orderId: String): Order? = TODO()
        override suspend fun confirmShipperDelivery(orderId: String): Result<Unit> = TODO()
        override suspend fun confirmCustomerReceipt(orderId: String): Result<Unit> = TODO()
        override suspend fun checkAndCompleteOrder(orderId: String): Result<Unit> = TODO()
        override suspend fun cancelOrderShipper(orderId: String): Result<Unit> = TODO()
        override suspend fun merchantAcceptOrderWithLocation(orderId: String, merchantId: String): Result<Unit> = TODO()
        override suspend fun cancelOrderWithReason(orderId: String, reason: String, cancelledBy: String): Result<Unit> = TODO()
        override suspend fun storeConfirmOrder(orderId: String): Result<Unit> = TODO()
        override suspend fun storeRejectOrder(orderId: String, reason: String): Result<Unit> = TODO()
        override suspend fun storeMarkReady(orderId: String): Result<Unit> = TODO()
        override suspend fun shipperAcceptOrder(orderId: String, shipperId: String): Result<Unit> = TODO()
        override suspend fun shipperCancelOrder(orderId: String): Result<Unit> = TODO()
        override suspend fun shipperCompleteDelivery(orderId: String): Result<Unit> = TODO()
        override suspend fun confirmOnlinePayment(orderId: String, transactionId: String): Result<Unit> = TODO()
        override suspend fun handlePaymentFailure(orderId: String): Result<Unit> = TODO()
    }

    private class FakePaymentRepository : PaymentRepository {
        var paymentsFlow: Flow<List<Payment>> = flowOf(emptyList())

        override suspend fun createPayment(payment: Payment): Result<Payment> = TODO()
        override suspend fun processPaymentAtomic(request: AtomicPaymentRequest): Result<AtomicPaymentResult> = TODO()
        override fun getPaymentsByCustomer(customerId: String): Flow<List<Payment>> = paymentsFlow
        override suspend fun getSystemSettings(): Result<List<SystemSetting>> = Result.success(emptyList())
    }

    private class FakeReviewRepository : ReviewRepository {
        var reviewsFlow: Flow<List<Review>> = flowOf(emptyList())

        override suspend fun getReviewsByFoodId(foodId: String): List<ReviewUiModel> = TODO()
        override suspend fun getReviewByOrderAndFood(orderId: String, foodId: String): Review? = TODO()
        override suspend fun submitReview(
            orderId: String,
            customerId: String,
            foodId: String,
            rating: Int,
            comment: String
        ): Boolean = TODO()

        override suspend fun updateReview(reviewId: String, rating: Int, comment: String): Boolean = TODO()
        override fun getReviewsByCustomer(customerId: String): Flow<List<Review>> = reviewsFlow
    }
}
