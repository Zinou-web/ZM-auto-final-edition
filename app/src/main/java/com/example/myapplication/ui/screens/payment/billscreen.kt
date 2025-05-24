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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.example.myapplication.ui.screens.home.BookingViewModel
import com.example.myapplication.ui.theme.poppins
import java.text.NumberFormat
import java.util.*

@Composable
fun BillScreen(
    onBackClick: () -> Unit,
    onContinueClick: () -> Unit,
    viewModel: BookingViewModel = viewModel()
) {
    // Calculate top padding based on status bar height
    val topPadding = with(LocalDensity.current) {
        WindowInsets.statusBars.getTop(this).toDp()
    }
    
    // Currency formatter
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US).apply {
        currency = Currency.getInstance("DZD")
    }
    
    // Format prices as currency
    val carPricePerDay = currencyFormatter.format(viewModel.carPrice).replace("DZD", "DA")
    val driverFees = currencyFormatter.format(viewModel.driverFees).replace("DZD", "DA")
    val totalPrice = currencyFormatter.format(viewModel.totalPrice).replace("DZD", "DA")
    
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
                        Image(
                            painter = painterResource(id = R.drawable.fleche_icon_lonly),
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Title "Bill" at the center
                Text(
                    text = "Bill",
                    fontSize = 23.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Center)
                )

                // Empty spacer for alignment
                Spacer(modifier = Modifier.size(45.dp).align(Alignment.CenterEnd))
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Rest of the content in a scrollable column
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_background),
                        contentDescription = "Car image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.36f))
                                .padding(horizontal = 5.dp)
                        ) {
                            Text(
                                text = viewModel.carTransmission,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.height(11.dp))

                        Text(
                            text = "${viewModel.carName} ${viewModel.carYear}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = "$carPricePerDay/day",
                            fontSize = 11.sp,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 3.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.36f))
                            .padding(vertical = 2.dp, horizontal = 5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.star_for_the_review_lonly),
                                contentDescription = "Rating icon",
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = viewModel.carRating.toString(),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(27.dp))

                BillDetailItem("Pick-Up Date & Time", "${viewModel.pickUpDate} | ${viewModel.pickUpTime}")
                BillDetailItem("Drop-Off Date & Time", "${viewModel.dropOffDate} | ${viewModel.dropOffTime}")
                BillDetailItem("Driver option", viewModel.driverOption)
                BillDetailItem("Amount", "$carPricePerDay/day")

                Divider(
                    color = Color.Black.copy(alpha = 0.4f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 35.dp)
                )

                BillDetailItem("Total Days", viewModel.totalDays.toString(), textColor = Color(0xFF323232))
                if (viewModel.driverOption == "With Driver") {
                    BillDetailItem("Driver Fees", driverFees, textColor = Color(0xFF323232))
                }

                Divider(
                    color = Color.Black.copy(alpha = 0.4f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 22.dp)
                )

                BillDetailItem("Total", totalPrice, textColor = Color(0xFF323232))

                Divider(
                    color = Color.Black.copy(alpha = 0.4f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 13.dp)
                )

                // Personal Information
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Personal Information",
                            fontSize = 18.sp,
                            fontFamily = poppins,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Name:",
                                fontSize = 14.sp,
                                fontFamily = poppins,
                                color = Color.Gray
                            )
                            
                            Text(
                                text = "${viewModel.firstName} ${viewModel.lastName}",
                                fontSize = 14.sp,
                                fontFamily = poppins,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Phone:",
                                fontSize = 14.sp,
                                fontFamily = poppins,
                                color = Color.Gray
                            )
                            
                            Text(
                                text = "+213 ${viewModel.phoneNumber}",
                                fontSize = 14.sp,
                                fontFamily = poppins,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Email:",
                                fontSize = 14.sp,
                                fontFamily = poppins,
                                color = Color.Gray
                            )
                            
                            Text(
                                text = viewModel.email,
                                fontSize = 14.sp,
                                fontFamily = poppins,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Wilaya:",
                                fontSize = 14.sp,
                                fontFamily = poppins,
                                color = Color.Gray
                            )
                            
                            Text(
                                text = viewModel.wilaya,
                                fontSize = 14.sp,
                                fontFamily = poppins,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "License:",
                                fontSize = 14.sp,
                                fontFamily = poppins,
                                color = Color.Gray
                            )
                            
                            Text(
                                text = viewModel.driverLicenseFileName,
                                fontSize = 14.sp,
                                fontFamily = poppins,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Confirm Payment button
            Button(
                onClick = onContinueClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF149459)
                )
            ) {
                Text(
                    text = "Confirm Payment",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun BillDetailItem(
    label: String,
    value: String,
    withDivider: Boolean = false,
    textColor: Color = Color.Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            color = textColor
        )

        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BillScreenPreview() {
    // Create a sample BookingViewModel for preview
    val viewModel = BookingViewModel().apply {
        carName = "Toyota Corolla"
        carYear = "2023"
        carPrice = 5000.0
        carTransmission = "Auto"
        carRating = 4.5f
        
        pickUpDate = "May 20, 2023"
        pickUpTime = "10:00 AM"
        dropOffDate = "May 23, 2023"
        dropOffTime = "12:00 PM"
        
        driverOption = "Self-Driver"
        totalDays = 3
        driverFees = 0.0
        totalPrice = 15000.0
        
        firstName = "Ahmed"
        lastName = "Mohammad"
        phoneNumber = "1234567890"
        email = "ahmed@example.com"
        wilaya = "Algiers"
        driverLicenseFileName = "license_scan.pdf"
    }
    
    BillScreen(
        onBackClick = {},
        onContinueClick = {},
        viewModel = viewModel
    )
}