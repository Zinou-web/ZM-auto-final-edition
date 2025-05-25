# Retrofit Integration Guide for Car Rental App

This guide demonstrates how to integrate the Jetpack Compose frontend with our Spring Boot backend via Retrofit.

## Base API URL

The backend provides REST APIs for users, cars, and reservations.
```
https://046e-105-105-223-167.ngrok-free.app/api
```

## Data Models (DTOs)

The app already has the necessary data models defined:

### User Model
```kotlin
data class User(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String? = null,
    val profileImage: String? = null,
    val address: Address? = null,
    val drivingLicense: DrivingLicense? = null,
    val favorites: List<Long> = emptyList(),
    val isEmailVerified: Boolean = false,
    val profileImageUrl: String? = null
)
```

### Car Model
```kotlin
data class Car(
    val id: Long = 0,
    val licensePlate: String = "",
    val description: String = "",
    val picture: String = "",
    val brand: String = "",
    val condition: String = "Mint",
    val model: String = "",
    val mileage: Long = 0,
    val type: String = "Hatchback",
    val year: Long = 0,
    val colour: String = "",
    val transmission: String = "Manual",
    val fuel: String = "Petrol",
    val seatingCapacity: Long = 4,
    val rentalPricePerDay: BigDecimal = BigDecimal.ZERO,
    val rentalPricePerHour: BigDecimal? = null,
    val rentalStatus: String = "Available",
    val currentLocation: String = "",
    val lastServiceDate: String? = null,
    val nextServiceDate: String = "",
    val insuranceExpiryDate: String = "",
    val gpsEnabled: Boolean = true,
    val rating: Long = 0,
    val createdAt: String = "",
    val updatedAt: String = ""
)
```

### Reservation Model
```kotlin
data class Reservation(
    val id: Long = 0,
    val userId: Long = 0,
    val carId: Long = 0,
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now().plusDays(1),
    val status: String = "PENDING", // PENDING, CONFIRMED, CANCELLED, COMPLETED
    val totalPrice: Double = 0.0,
    val paymentStatus: String = "UNPAID", // UNPAID, PAID, REFUNDED
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val car: Car? = null,
    val user: User? = null
)
```

## Retrofit API Interfaces

The app already has a comprehensive ApiService interface that includes all the necessary endpoints:

```kotlin
interface ApiService {
    // User Authentication
    @POST("users/login")
    @FormUrlEncoded
    fun login(
        @Query("email") email: String,
        @Query("password") password: String
    ): AuthResponse

    @POST("users/register")
    @FormUrlEncoded
    fun register(
        @Query("name") name: String,
        @Query("email") email: String,
        @Query("password") password: String,
        @Query("phone") phone: String
    ): AuthResponse

    // Car Endpoints
    @GET("cars")
    fun getAllCars(): List<Car>

    @GET("cars")
    fun getAllCarsPaged(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10,
        @Query("sort") sort: String = "id",
        @Query("direction") direction: String = "asc",
        @Query("brand") brand: String? = null,
        @Query("model") model: String? = null,
        @Query("minRating") minRating: Long? = null,
        @Query("maxRating") maxRating: Long? = null,
        @Query("rentalStatus") rentalStatus: String? = null
    ): PagedResponse<Car>

    // Reservation Endpoints
    @POST("reservations")
    fun createReservation(
        @Body reservation: Reservation,
        @Header("Authorization") token: String
    ): Reservation
}
```

## Repository Classes

The app already has repository interfaces and implementations for authentication, cars, and reservations:

### AuthRepository
```kotlin
interface AuthRepository {
    fun isLoggedIn(): Boolean
    fun login(email: String, password: String): Flow<ApiResource<Any>>
    fun register(name: String, email: String, password: String, phone: String): Flow<ApiResource<Any>>
    // Other methods...
}
```

### CarRepository
```kotlin
interface CarRepository {
    fun getAllCars(): Flow<ApiResource<List<Car>>>
    fun getCarById(id: Long): Flow<ApiResource<Car>>
    fun getAvailableCarsPaged(page: Int, size: Int, sort: String): Flow<ApiResource<PagedResponse<Car>>>
    // Other methods...
}
```

### ReservationRepository
```kotlin
interface ReservationRepository {
    fun createReservation(carId: Long, startDate: LocalDate, endDate: LocalDate, totalPrice: Double): Flow<ApiResource<Reservation>>
    fun getUserReservations(): Flow<ApiResource<List<Reservation>>>
    // Other methods...
}
```

## ViewModels

The app already has ViewModels that use the repositories to interact with the backend:

### AuthViewModel
```kotlin
@HiltViewModel
class AuthViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val preferenceManager: PreferenceManager
) : ViewModel() {
    // UI state
    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Loading)
    val uiState: StateFlow<AuthUiState> = _uiState

    fun login(email: String, password: String) {
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.login(email, password).collectLatest { result ->
                when (result.status) {
                    ApiStatus.SUCCESS -> {
                        // Handle successful login
                        _uiState.value = AuthUiState.Success(result.data.toString())
                    }
                    ApiStatus.ERROR -> {
                        // Handle login error
                        _uiState.value = AuthUiState.Error(result.message ?: "Unknown error")
                    }
                    else -> {
                        // Handle loading state
                        _uiState.value = AuthUiState.Loading
                    }
                }
            }
        }
    }
    
    // Other methods...
}
```

### CarViewModel
```kotlin
@HiltViewModel
class CarViewModel @Inject constructor(
    private val carRepository: CarRepository
) : ViewModel() {
    // UI state
    private val _uiState = MutableStateFlow<CarUiState>(CarUiState.Loading)
    val uiState: StateFlow<CarUiState> = _uiState

    fun getAvailableCars(page: Int = 0, size: Int = 10) {
        _uiState.value = CarUiState.Loading
        viewModelScope.launch {
            carRepository.getAvailableCarsPaged(page, size, "id").collectLatest { result ->
                when (result.status) {
                    ApiStatus.SUCCESS -> {
                        // Handle successful car fetch
                        _uiState.value = CarUiState.PaginatedSuccess(result.data!!)
                    }
                    ApiStatus.ERROR -> {
                        // Handle error
                        _uiState.value = CarUiState.Error(result.message ?: "Unknown error")
                    }
                    else -> {
                        // Handle loading state
                        _uiState.value = CarUiState.Loading
                    }
                }
            }
        }
    }
    
    // Other methods...
}
```

### ReservationViewModel
```kotlin
@HiltViewModel
class ReservationViewModel @Inject constructor(
    private val reservationRepository: ReservationRepository
) : ViewModel(), ReservationScreenActions {
    // UI state
    private val _uiState = MutableStateFlow<ReservationUiState>(ReservationUiState.Loading)
    val uiState: StateFlow<ReservationUiState> = _uiState

    override fun createReservation(carId: Long, startDate: LocalDate, endDate: LocalDate, totalPrice: Double) {
        _uiState.value = ReservationUiState.Loading
        viewModelScope.launch {
            reservationRepository.createReservation(carId, startDate, endDate, totalPrice).collectLatest { result ->
                when (result.status) {
                    ApiStatus.SUCCESS -> {
                        // Handle successful reservation creation
                        _uiState.value = ReservationUiState.SingleReservationSuccess(result.data!!)
                    }
                    ApiStatus.ERROR -> {
                        // Handle error
                        _uiState.value = ReservationUiState.Error(result.message ?: "Unknown error")
                    }
                    else -> {
                        // Handle loading state
                        _uiState.value = ReservationUiState.Loading
                    }
                }
            }
        }
    }
    
    // Other methods...
}
```

## Compose UI Examples

Here are examples of how the Compose UI could call these functions:

### Login Screen
```kotlin
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { viewModel.login(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
        
        when (uiState) {
            is AuthUiState.Loading -> {
                CircularProgressIndicator()
            }
            is AuthUiState.Success -> {
                LaunchedEffect(Unit) {
                    onNavigateToHome()
                }
            }
            is AuthUiState.Error -> {
                Text(
                    text = (uiState as AuthUiState.Error).message,
                    color = MaterialTheme.colors.error
                )
            }
            else -> {}
        }
    }
}
```

### Car Listing Screen
```kotlin
@Composable
fun CarListingScreen(
    viewModel: CarViewModel = hiltViewModel(),
    onCarSelected: (Car) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.getAvailableCars()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Available Cars",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        when (uiState) {
            is CarUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is CarUiState.PaginatedSuccess -> {
                val cars = (uiState as CarUiState.PaginatedSuccess).pagedResponse.content
                LazyColumn {
                    items(cars) { car ->
                        CarItem(car = car, onClick = { onCarSelected(car) })
                    }
                }
            }
            is CarUiState.Error -> {
                Text(
                    text = (uiState as CarUiState.Error).message,
                    color = MaterialTheme.colors.error
                )
            }
            else -> {}
        }
    }
}

@Composable
fun CarItem(car: Car, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Car image
            AsyncImage(
                model = car.picture,
                contentDescription = "${car.brand} ${car.model}",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Car details
            Column {
                Text(
                    text = "${car.brand} ${car.model}",
                    style = MaterialTheme.typography.h6
                )
                Text(
                    text = "Year: ${car.year}",
                    style = MaterialTheme.typography.body2
                )
                Text(
                    text = "Price: $${car.rentalPricePerDay}/day",
                    style = MaterialTheme.typography.body2
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color.Yellow
                    )
                    Text(
                        text = "${car.rating}",
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }
    }
}
```

### Make Reservation Screen
```kotlin
@Composable
fun MakeReservationScreen(
    carId: Long,
    viewModel: CarViewModel = hiltViewModel(),
    reservationViewModel: ReservationViewModel = hiltViewModel(),
    onReservationComplete: () -> Unit
) {
    val carUiState by viewModel.uiState.collectAsState()
    val reservationUiState by reservationViewModel.uiState.collectAsState()
    
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusDays(1)) }
    var totalPrice by remember { mutableStateOf(0.0) }
    
    LaunchedEffect(carId) {
        viewModel.loadCarById(carId)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Make a Reservation",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        when (carUiState) {
            is CarUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is CarUiState.SingleCarSuccess -> {
                val car = (carUiState as CarUiState.SingleCarSuccess).car
                
                // Car details
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = 4.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "${car.brand} ${car.model}",
                            style = MaterialTheme.typography.h6
                        )
                        Text(
                            text = "Year: ${car.year}",
                            style = MaterialTheme.typography.body2
                        )
                        Text(
                            text = "Price: $${car.rentalPricePerDay}/day",
                            style = MaterialTheme.typography.body2
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Date selection
                Text(
                    text = "Select Dates",
                    style = MaterialTheme.typography.h6
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Start Date")
                        Button(onClick = {
                            // Show date picker for start date
                        }) {
                            Text(startDate.toString())
                        }
                    }
                    
                    Column {
                        Text("End Date")
                        Button(onClick = {
                            // Show date picker for end date
                        }) {
                            Text(endDate.toString())
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Calculate price
                LaunchedEffect(startDate, endDate) {
                    val days = ChronoUnit.DAYS.between(startDate, endDate).toInt()
                    totalPrice = car.rentalPricePerDay.toDouble() * days
                }
                
                Text(
                    text = "Total Price: $${totalPrice}",
                    style = MaterialTheme.typography.h6
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Make reservation button
                Button(
                    onClick = {
                        reservationViewModel.createReservation(
                            carId = car.id,
                            startDate = startDate,
                            endDate = endDate,
                            totalPrice = totalPrice
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Make Reservation")
                }
                
                // Handle reservation state
                when (reservationUiState) {
                    is ReservationUiState.Loading -> {
                        CircularProgressIndicator()
                    }
                    is ReservationUiState.SingleReservationSuccess -> {
                        LaunchedEffect(Unit) {
                            onReservationComplete()
                        }
                    }
                    is ReservationUiState.Error -> {
                        Text(
                            text = (reservationUiState as ReservationUiState.Error).message,
                            color = MaterialTheme.colors.error
                        )
                    }
                    else -> {}
                }
            }
            is CarUiState.Error -> {
                Text(
                    text = (carUiState as CarUiState.Error).message,
                    color = MaterialTheme.colors.error
                )
            }
            else -> {}
        }
    }
}
```

## Conclusion

The Car Rental App already has a well-structured architecture with Retrofit integration:

1. **Data Models (DTOs)** are defined for User, Car, and Reservation.
2. **Retrofit API Interface** (ApiService) is set up with endpoints for user authentication, car listing, and reservations.
3. **Repository Classes** abstract the API calls and provide a clean interface for the ViewModels.
4. **ViewModels** handle the business logic and expose UI states that can be observed by the Compose UI.
5. **Compose UI** observes the UI states and calls the appropriate ViewModel methods.

This architecture follows the recommended approach for Android apps, with a clear separation of concerns and a unidirectional data flow.