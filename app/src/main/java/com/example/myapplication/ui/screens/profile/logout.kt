package com.example.myapplication.ui.screens.profile

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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.R
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.poppins
import com.example.myapplication.navigation.AppScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.ui.screens.auth.AuthViewModel
import androidx.compose.runtime.collectAsState
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.example.myapplication.ui.screens.profile.ProfileViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.alpha

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun logoutScreen(
    navController: NavController,
    onHomeClick: () -> Unit = { navController.navigate("Home") },
    onBookingsClick: () -> Unit = { navController.navigate("MyBooking") },
    onFavoriteClick: () -> Unit = { navController.navigate(AppScreen.Favorites.name) },
    onBackClick: () -> Unit = { navController.popBackStack() }
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val user by profileViewModel.user.collectAsState()
    LaunchedEffect(Unit) {
        profileViewModel.loadUserProfile()
    }

    var showLogoutSheet by remember { mutableStateOf(false) }

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
                if (user?.profileImage != null && user!!.profileImage.isNotEmpty()) {
                    AsyncImage(
                        model = user!!.profileImage,
                        contentDescription = "Profile Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.account),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .alpha(0.5f)
                    )
                }
                Box(
                    modifier = Modifier
                        .offset(x = (-4).dp, y = (-4).dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(4.dp)
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
                text = user?.let {
                    if (!it.firstName.isNullOrEmpty()) {
                        listOfNotNull(it.firstName, it.lastName).joinToString(" ")
                    } else {
                        it.name.orEmpty()
                    }
                } ?: "Name",
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
            ProfileOption(R.drawable.profil, "Your Profile") { }
            ProfileOption(R.drawable.setting_line, "Settings") { }
            ProfileOption(R.drawable.mdi_question_mark_circle_outline, "Help & Support") { }
            ProfileOption(R.drawable.ic_outline_lock, "Privacy & Policy") { }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            ProfileOption(
                icon = R.drawable.log_out_icons,
                label = "Log Out",
                showArrow = false
            ) {
                showLogoutSheet = true
            }
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

        // Bottom sheet for logout
        if (showLogoutSheet) {
            ModalBottomSheet(
                onDismissRequest = { showLogoutSheet = false },
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Log Out",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Are you sure you want to Log Out?",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { showLogoutSheet = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2AA046)),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Text("Cancel", color = Color.White)
                        }

                        Button(
                            onClick = {
                                showLogoutSheet = false
                                // Perform logout and navigate to SignIn
                                authViewModel.logout()
                                navController.navigate(AppScreen.SignIn.name) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFEFEF)),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        ) {
                            Text("Yes", color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LogoutScreenPreview() {
    MyApplicationTheme {
        logoutScreen(navController = rememberNavController())
    }
}
