package dev.looklatch.xr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LookLatchTheme {
                LookLatchXrApp()
            }
        }
    }
}

@Composable
fun LookLatchXrApp() {
    var state by remember { mutableStateOf(AccessState.Idle) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF7F4EE),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "LookLatch XR",
                    color = Color(0xFF172026),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "look to arm / phone to prove / touch to confirm",
                    color = Color(0xFF5B6770),
                    fontSize = 14.sp,
                )
                Spacer(modifier = Modifier.height(28.dp))
                StatePanel(state = state)
                Spacer(modifier = Modifier.height(24.dp))
                DemoControls(
                    state = state,
                    onNext = { state = state.nextDemoState() },
                    onExpire = { state = AccessState.Expired },
                    onError = { state = AccessState.Error },
                    onReset = { state = AccessState.Idle },
                )
            }
        }
    }
}

@Composable
private fun StatePanel(state: AccessState) {
    val accent = when (state) {
        AccessState.Unlocked -> Color(0xFF1F7A4D)
        AccessState.Error -> Color(0xFFB3261E)
        AccessState.Expired -> Color(0xFF806000)
        else -> Color(0xFF285B73)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(22.dp),
    ) {
        Text(
            text = state.title,
            color = accent,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = state.summary,
            color = Color(0xFF172026),
            fontSize = 18.sp,
            lineHeight = 24.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = state.safetyNote,
            color = Color(0xFF5B6770),
            fontSize = 15.sp,
            lineHeight = 21.sp,
        )
    }
}

@Composable
private fun DemoControls(
    state: AccessState,
    onNext: () -> Unit,
    onExpire: () -> Unit,
    onError: () -> Unit,
    onReset: () -> Unit,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF285B73)),
        ) {
            Text(if (state == AccessState.Unlocked) "Reset flow" else "Next state")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.Center) {
            OutlinedButton(onClick = onExpire) {
                Text("Expire")
            }
            Spacer(modifier = Modifier.width(10.dp))
            OutlinedButton(onClick = onError) {
                Text("Error")
            }
            Spacer(modifier = Modifier.width(10.dp))
            OutlinedButton(onClick = onReset) {
                Text("Idle")
            }
        }
    }
}

@Composable
private fun LookLatchTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
