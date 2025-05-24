package com.example.myapplication.ui.screens.bookings


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.poppins

class CancelationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                CancelationScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CancelationScreen(
    onBackClick: () -> Unit = {}
) {
    val reasons = listOf(
        "Schedule Change",
        "Book Another Car",
        "Found Better alternative",
        "My reason is not listed",
        "Want to Book Another Car",
        "Others"
    )
    var selectedReason by remember { mutableStateOf(reasons[0]) }
    var otherText by remember { mutableStateOf("") }

    // Calculate top padding based on status bar height
    val topPadding = with(LocalDensity.current) {
        WindowInsets.statusBars.getTop(this).toDp()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F5FA))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topPadding)
        ) {
            // Header with back button
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
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Title "Cancelation" at the center
            Text(
                text = "Cancelation",
                    fontSize = 23.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Center)
            )

                // Empty spacer for alignment
                Spacer(modifier = Modifier.size(45.dp).align(Alignment.CenterEnd))
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Please select the reason for cancellation",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Radio Buttons
        reasons.forEach { reason ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { selectedReason = reason }
            ) {
                RadioButton(
                    selected = selectedReason == reason,
                    onClick = { selectedReason = reason }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = reason,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )
            }
        }

        // Others TextField if selected
        if (selectedReason == "Others") {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Others",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = otherText,
                onValueChange = { otherText = it },
                placeholder = { Text("Enter Text") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Submit Button
        Button(
            onClick = { /* Handle submit */ },
            modifier = Modifier
                .fillMaxWidth()
                        .height(50.dp)
                        .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF017A51)),
            elevation = ButtonDefaults.buttonElevation(6.dp)
        ) {
            Text("Submit", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun CancelationScreenPreview() {
    MyApplicationTheme {
        CancelationScreen()
    }
}
