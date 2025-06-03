package com.example.myapplication.ui.screens.profile

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.poppins

data class ContactOption(
    val name: String,
    val icon: ImageVector,
    val iconTint: Color
)

@Composable
fun HelpCenterScreen(onBackClick: () -> Unit = {}) {
    val faqQuestions = listOf(
        "Can I track my booked deliver status?",
        "Is there a return policy?",
        "Can I save my favorite item for later?",
        "Can I share the products with my friends",
        "How do I contact customer Support?",
        "What payment methods are accepted?",
        "How to add review?"
    )

    var selectedTab by remember { mutableStateOf(0) }
    
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
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Title "Help Center" at the center
                Text(
                    text = "Help Center",
                    fontSize = 23.sp,
                    fontFamily = poppins,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.align(Alignment.Center)
                )
                
                // Empty spacer for alignment
                Spacer(modifier = Modifier.size(45.dp).align(Alignment.CenterEnd))
            }

            // Tab selector
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = "FAQ",
                        fontSize = 16.sp,
                        fontFamily = poppins,
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 0) Color.Black else Color.Gray,
                        modifier = Modifier
                            .clickable { selectedTab = 0 }
                            .padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Contact",
                        fontSize = 16.sp,
                        fontFamily = poppins,
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTab == 1) Color.Black else Color.Gray,
                        modifier = Modifier
                            .clickable { selectedTab = 1 }
                            .padding(bottom = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(if (selectedTab == 0) Color(0xFF149459) else Color.LightGray)
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(if (selectedTab == 1) Color(0xFF149459) else Color.LightGray)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Content based on selected tab
                when (selectedTab) {
                    0 -> {
                        // FAQ Tab
                        FAQList(questions = faqQuestions)
                    }
                    1 -> {
                        // Contact Tab
                        ContactList()
                    }
                }
            }
        }
    }
}

@Composable
fun FAQList(questions: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        questions.forEach { question ->
            FAQItem(question = question)
        }
    }
}

@Composable
fun FAQItem(question: String) {
    var expanded by remember { mutableStateOf(false) }
    
    // Map questions to answers
    val answers = mapOf(
        "Can I track my booked deliver status?" to "Yes, you can track your booking status in the 'My Bookings' section. The app provides real-time updates on the status of your reservation.",
        "Is there a return policy?" to "Yes, you can cancel your booking up to 24 hours before the pickup time for a full refund. Cancellations made less than 24 hours before will incur a 30% fee.",
        "Can I save my favorite item for later?" to "Yes, you can add cars to your favorites by clicking the heart icon. Access your saved cars anytime from the 'Favorites' tab.",
        "Can I share the products with my friends" to "Yes, you can share car details with friends via the share button on the car details page, which allows sharing via messaging apps, email, or social media.",
        "How do I contact customer Support?" to "You can contact our customer support team via phone at +213 657123478, email at support@zmauto.com, or through the 'Contact' section in the Help Center.",
        "What payment methods are accepted?" to "We accept credit/debit cards, PayPal, and cash payments at the time of pickup. All online payments are secured with industry-standard encryption.",
        "How to add review?" to "After completing your rental, you'll receive a notification to review your experience. You can also add reviews by visiting the car details page and clicking 'Add Review'."
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .clickable { expanded = !expanded }
            .animateContentSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = question,
                modifier = Modifier.weight(1f),
                fontFamily = poppins,
                fontSize = 15.sp,
                color = Color.Black
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color(0xFF149459)
            )
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = answers[question] ?: "No answer available",
                fontFamily = poppins,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ContactList() {
    val contacts = listOf(
        ContactOption("Customer Service", Icons.Default.Person, Color.Gray),
        ContactOption("WhatsApp", Icons.Default.Chat, Color(0xFF25D366)),
        ContactOption("Website", Icons.Default.Public, Color.Gray),
        ContactOption("Facebook", Icons.Default.Facebook, Color(0xFF1877F2)),
        ContactOption("Twitter", Icons.Default.Share, Color(0xFF1DA1F2)),
        ContactOption("Instagram", Icons.Default.CameraAlt, Color(0xFFC13584))
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        contacts.forEach {
            ContactItem(contact = it)
        }
    }
}

@Composable
fun ContactItem(contact: ContactOption) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Map contact options to their details
    val contactDetails = mapOf(
        "Customer Service" to "Call us at: +213 657123478",
        "WhatsApp" to "Message us on: +213 657123478",
        "Website" to "Visit our website: ZMauto.com",
        "Facebook" to "Follow us: ZM Auto Official",
        "Twitter" to "Follow us: @ZMAutoOfficial",
        "Instagram" to "Follow us: @zmauto_official"
    )
    
    // Handle website click
    val handleWebsiteClick = {
        if (contact.name == "Website") {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://zmauto.com"))
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open website", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .clickable { expanded = !expanded }
            .animateContentSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = contact.icon,
                contentDescription = contact.name,
                tint = contact.iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = contact.name,
                modifier = Modifier.weight(1f),
                fontFamily = poppins,
                fontSize = 15.sp,
                color = Color.Black
            )
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color(0xFF149459)
            )
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // If it's the website option, make it clickable
            if (contact.name == "Website") {
                Text(
                    text = contactDetails[contact.name] ?: "No details available",
                    fontFamily = poppins,
                    fontSize = 14.sp,
                    color = Color(0xFF149459),
                    modifier = Modifier.clickable(onClick = handleWebsiteClick)
                )
            } else {
                Text(
                    text = contactDetails[contact.name] ?: "No details available",
                    fontFamily = poppins,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HelpCenterScreenPreview() {
    MaterialTheme {
        HelpCenterScreen()
    }
}
