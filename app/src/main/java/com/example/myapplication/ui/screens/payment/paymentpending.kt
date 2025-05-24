package com.example.myapplication.ui.screens.payment



import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.poppins

@Composable
fun PaymentPending(
    onBackToHomeClick: () -> Unit
) {
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
            // Header with title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp, start = 15.dp, end = 15.dp, bottom = 10.dp)
            ) {
                // Title "Payment Pending" at the center

                
                // Empty spacers for alignment
                Spacer(modifier = Modifier.size(45.dp).align(Alignment.CenterStart))
                Spacer(modifier = Modifier.size(45.dp).align(Alignment.CenterEnd))
            }

            // Content with pending icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp)
                    .verticalScroll(rememberScrollState())
            ) {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.payment_pending_photos),
                            contentDescription = "Pending icon",
                            modifier = Modifier
                                .size(150.dp)
                                .padding(8.dp)
                        )
                    }


                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Payment Pending – Cash",
                    color = Color(0xFF000000),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontFamily = poppins
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Please pay in cash when picking up the car.\nA valid ID will be required at pickup.",
                    color = Color(0xFF000000),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = poppins,
                    modifier = Modifier
                        .padding(horizontal = 32.dp).alpha(0.6f)



                )

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = onBackToHomeClick,
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF149459)
                    )
                ) {
                    Text(
                        text = "Back to Main page",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = poppins
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentPendingPreview() {
    PaymentPending(
        onBackToHomeClick = {}
    )
}
