package com.example.myapplication.ui.screens.welcome
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.*

private const val TAG = "ThirdScreen"

@Composable
fun ThirdScreen(
    onBackClick: () -> Unit = {},
    onNextClick: () -> Unit = {}
) {
    // Add logging when ThirdScreen is composed
    Log.d(TAG, "ThirdScreen is being composed")
    
    // Get the context for showing toasts
    val context = LocalContext.current
    
    // Add a LaunchedEffect for logging when the screen is first displayed
    LaunchedEffect(Unit) {
        Log.d(TAG, "ThirdScreen LaunchedEffect triggered")
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.audi2),
                contentDescription = "Audi N2",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 70.dp)
                .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            WelcomeTexts()
            Spacer(modifier = Modifier.height(40.dp))
            BottomSection(
                onBackClick = {
                    Log.d(TAG, "Back button clicked")
                    onBackClick()
                },
                onNextClick = {
                    Log.d(TAG, "Next button clicked - proceeding to SignInScreen")
                    onNextClick()
                    Log.d(TAG, "Navigation request sent successfully")
                }
            )
        }
    }
}

@Composable
fun WelcomeTexts() {
    Text(
        text = "Save Your Top Picks for Later",
        fontSize = 26.sp,
        textAlign = TextAlign.Center,
        color = Color.Black,
        fontFamily = poppins,
        fontWeight = FontWeight.Normal,

        )

    Text(
        text = "Add it to Favourites",
        fontSize = 26.sp,
        color = Page2Green,
        textAlign = TextAlign.Center,
        fontFamily = poppins,
        fontWeight = FontWeight.Normal,
    )

    Spacer(modifier = Modifier.height(30.dp))

    Text(
        text = "Found a car you love? Add it to your\n favorites to keep it handy for later.\n Compare your top picks, revisit them\n anytime, and book with confidence when\n you're ready to hit the road!",
        fontSize = 18.sp,
        lineHeight = 22.sp,
        color = TextGray,
        textAlign = TextAlign.Center,
        fontFamily = poppins,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.075.sp
    )
}

@Composable
fun BottomSection(
    onBackClick: () -> Unit,
    onNextClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp)
    ) {
        PageIndicator(
            modifier = Modifier.align(Alignment.Center)
        )

        NextButton(
            onClick = onNextClick,
            modifier = Modifier.align(Alignment.CenterEnd)
        )

        BackButton(
            onClick = onBackClick,
            modifier = Modifier.align(Alignment.CenterStart)
        )
    }
}

@Composable
fun PageIndicator(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(15.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
        Box(
            modifier = Modifier
                .size(15.dp)
                .clip(CircleShape)
                .background(Page2Green)

        )
    }
}

@Composable
fun NextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(58.dp)
            .background(color = Page2Green, shape = RoundedCornerShape(25.dp))
    ) {
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "Next",
            tint = Color.White,
            modifier = Modifier.size(35.dp)
        )
    }
}

@Composable
fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(58.dp)
            .background(color = Page2Green, shape = RoundedCornerShape(25.dp))
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            tint = Color.White,
            modifier = Modifier.size(35.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ThirdScreenPreview() {
    MyApplicationTheme {
        ThirdScreen()
    }
}