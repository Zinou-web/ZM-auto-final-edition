package com.example.myapplication.ui.screens.payment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.R
import com.example.myapplication.ui.screens.home.BookingViewModel
import com.example.myapplication.ui.theme.poppins

private const val CASH = "Cash"
private const val CARD = "Card"

data class PaymentMethodOption(
    val title: String,
    val subtitle: String,
    val key: String,
    val iconRes: Int? = null
)

@Composable
fun PaymentMethodScreen(
    onBackClick: () -> Unit,
    onEdahabiaClick: () -> Unit,
    onCashClick: () -> Unit,
    viewModel: BookingViewModel = viewModel()
) {
    var selectedOption by remember { mutableStateOf("") }
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
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Header with back button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = topPadding)
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

                    // Title "Payment Method" at the center
                    Text(
                        text = "Payment Method",
                        fontSize = 21.sp,
                        fontFamily = poppins,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    
                    // Empty spacer for alignment
                    Spacer(modifier = Modifier.size(45.dp).align(Alignment.CenterEnd))
                }
                
                // Payment options
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    PaymentOptions(selectedOption) { selectedOption = it }
                }
            }
            
            // Continue button at the bottom
            Button(
                onClick = {
                    if (selectedOption == CARD) {
                        onEdahabiaClick()
                    } else if (selectedOption == CASH) {
                        onCashClick()
                    }
                },
                enabled = selectedOption.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF149459),
                    disabledContainerColor = Color.Gray
                )
            ) {
                Text(
                    text = "Continue",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun PaymentOptions(
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    val options = listOf(
        PaymentMethodOption(
            title = "Cash",
            subtitle = "DA cash",
            key = CASH
        ),
        PaymentMethodOption(
            title = "Card Edahabia",
            subtitle = "Add Card",
            key = CARD,
            iconRes = R.drawable.credit_card_icon
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        options.forEachIndexed { index, option ->
            PaymentOption(
                title = option.title,
                subtitle = option.subtitle,
                isSelected = selectedOption == option.key,
                onSelect = { onOptionSelected(option.key) },
                iconRes = option.iconRes
            )
            if (index < options.lastIndex) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PaymentOption(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    iconRes: Int?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                color = Color.Black
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.5.dp,
                    color = if (isSelected) Color(0xFF149459) else Color.Black,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(Color.White, RoundedCornerShape(12.dp))
                .clickable(onClick = onSelect)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PaymentLabelRow(icon = iconRes, label = subtitle)

                RadioButton(
                    selected = isSelected,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color(0xFF149459),
                        unselectedColor = Color.Black
                    )
                )
            }
        }
    }
}

@Composable
private fun PaymentLabelRow(icon: Int?, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (icon != null) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Black
                )
            )
        } else {
            Text(
                text = "DA",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Black
                )
            )
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(16.dp)
                    .background(Color.Black)
            )
            Text(
                text = "cash",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Black
                )
            )
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun PreviewPaymentMethod() {
    MaterialTheme {
        PaymentMethodScreen(
            onBackClick = {},
            onEdahabiaClick = {},
            onCashClick = {}
        )
    }
}