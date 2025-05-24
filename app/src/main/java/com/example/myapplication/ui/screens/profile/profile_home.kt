package com.example.myapplication.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.R
import com.example.myapplication.navigation.Screen
import com.example.myapplication.navigation.AppScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.poppins
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
    onHomeClick: () -> Unit = { navController.navigate(Screen.Home.name) },
    onBookingsClick: () -> Unit = { navController.navigate(Screen.MyBooking.name) },
    onFavoriteClick: () -> Unit = { navController.navigate(AppScreen.Favorites.name) },
    onBackClick: () -> Unit = { navController.popBackStack() }
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val view = LocalView.current

    // Configure window insets
    DisposableEffect(view) {
        val window = (view.context as android.app.Activity).window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        onDispose {}
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onProfileImageSelected(it) }
    }

    // Show a dialog when there's an error
    if (uiState is ProfileUiState.Error) {
        val errorMessage = (uiState as ProfileUiState.Error).message
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.clearError() }
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Show loading indicator when loading
    if (uiState is ProfileUiState.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    // Calculate top padding based on status bar height
    val topPadding = with(LocalDensity.current) {
        WindowInsets.statusBars.getTop(this).toDp()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F9FC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp, start = 15.dp, end = 15.dp, bottom = 10.dp)
            ) {
                // Back button
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(45.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFFFFF))
                        .clickable { onBackClick() }
                ) {
                    IconButton(
                        onClick = { onBackClick() },
                        modifier = Modifier.size(45.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                    )
                    }
                }

                // Title "Profile" at the center
                Text(
                    text = "Profile",
                    fontSize = 23.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile picture
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                // Show selected image or default
                val painter = if (viewModel.profileImageUri != null) {
                    rememberAsyncImagePainter(
                        model = viewModel.profileImageUri
                    )
                } else {
                    painterResource(id = R.drawable.account)
                }

                Image(
                    painter = painter,
                    contentDescription = "Profile Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") }
                )

                Box(
                    modifier = Modifier
                        .offset(x = (-4).dp, y = (-4).dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(4.dp)
                        .clickable { imagePickerLauncher.launch("image/*") }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.edit_icon),
                        contentDescription = "Edit",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = viewModel.name.value.ifEmpty { "User Profile" },
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 8.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = poppins
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Profile options in a Column with horizontal padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 80.dp) // Add padding for bottom nav bar
            ) {
                ProfileOption(
                    icon = R.drawable.profil,
                    label = "Your Profile"
                ) { navController.navigate(Screen.ProfileGeneral.route) }

                ProfileOption(
                    icon = R.drawable.setting_line,
                    label = "Settings"
                ) { navController.navigate(AppScreen.Settings.name) }

                ProfileOption(
                    icon = R.drawable.mdi_question_mark_circle_outline,
                    label = "Help & Support"
                ) { navController.navigate(Screen.HelpCenter.route) }

                ProfileOption(
                    icon = R.drawable.ic_outline_lock,
                    label = "Privacy & Policy"
                ) { navController.navigate(Screen.PrivacyPolicy.route) }

                Divider(modifier = Modifier.padding(vertical = 16.dp))

            ProfileOption(
                icon = R.drawable.log_out_icons,
                label = "Log Out",
                showArrow = false
            ) { navController.navigate(Screen.Logout.route) }
            }
        }

        // Bottom Navigation Bar
        BottomNavBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            onHomeClick = onHomeClick,
            onCatalogClick = onBookingsClick,
            onFavoriteClick = onFavoriteClick,
            onProfileClick = { /* Already on Profile */ },
            selectedItem = "Profile"
        )
    }
}

@Composable
fun ProfileOption(
    icon: Int,
    label: String,
    showArrow: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = label,
                fontSize = 16.sp,
                fontFamily = poppins
            )

            Spacer(modifier = Modifier.weight(1f))

            if (showArrow) {
                Icon(
                    painter = painterResource(id = R.drawable.green_arrow_for_profil_page),
                    contentDescription = "Next",
                    tint = Color(0xFF2AA046)
                )
            }
        }
    }
}

@Composable
fun BottomNavBar(
    modifier: Modifier = Modifier,
    onHomeClick: () -> Unit = {},
    onCatalogClick: () -> Unit = {},
    onFavoriteClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    selectedItem: String = "Home"
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                iconRes = R.drawable.home,
                label = "Home",
                isSelected = selectedItem == "Home",
                onClick = onHomeClick
            )

            BottomNavItem(
                iconRes = R.drawable.catalog,
                label = "Bookings",
                isSelected = selectedItem == "Bookings",
                onClick = onCatalogClick
            )

            BottomNavItem(
                iconRes = R.drawable.heart,
                label = "Favorite",
                isSelected = selectedItem == "Favorite",
                onClick = onFavoriteClick
            )

            BottomNavItem(
                iconRes = R.drawable.profilenav,
                label = "Profile",
                isSelected = selectedItem == "Profile",
                onClick = onProfileClick
            )
        }
    }
}

@Composable
fun BottomNavItem(
    iconRes: Int,
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    val itemColor = if (isSelected) Color.Black else Color.Gray
    val bgColor = if (isSelected) Color(0xFFEADDFA) else Color.Transparent

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(bgColor)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                tint = itemColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = itemColor,
                fontFamily = poppins
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    MyApplicationTheme {
        val previewNavController = rememberNavController()
        ProfileScreen(navController = previewNavController)
    }
}