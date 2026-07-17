package dev.looklatch.phone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.looklatch.phone.proximity.FakeProximityProvider
import dev.looklatch.phone.proximity.ProximityConfidence
import dev.looklatch.phone.proximity.ProximityEvent
import dev.looklatch.phone.proximity.ProximityTarget
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LookLatchPhoneTheme {
                PhoneCompanionApp()
            }
        }
    }
}

@Composable
fun PhoneCompanionApp() {
    val provider = remember { FakeProximityProvider() }
    val scope = rememberCoroutineScope()
    val demoTarget = remember { ProximityTarget(id = "demo-lock-001", label = "Demo vehicle lock") }
    val eventLog = remember { mutableStateListOf<String>() }
    var latestEvent by remember { mutableStateOf<ProximityEvent?>(null) }

    LaunchedEffect(provider) {
        provider.events.collect { event ->
            latestEvent = event
            eventLog.add(0, "${event.confidence}: ${event.detail}")
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF4F7F8),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = "LookLatch Phone Companion",
                color = Color(0xFF172026),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
            )
            StatusCard(target = demoTarget, event = latestEvent)
            ControlPanel(
                onStart = { scope.launch { provider.startSession(demoTarget) } },
                onNearby = {
                    scope.launch {
                        provider.simulate(
                            ProximityConfidence.Nearby,
                            "Fake BLE session is nearby but not verified.",
                        )
                    }
                },
                onVerified = {
                    scope.launch {
                        provider.simulate(
                            ProximityConfidence.Verified,
                            "Fake UWB/BLE proximity has been verified.",
                        )
                    }
                },
                onFailed = {
                    scope.launch {
                        provider.simulate(
                            ProximityConfidence.Failed,
                            "Fake proximity failed. The flow must not unlock.",
                        )
                    }
                },
                onStop = { scope.launch { provider.stopSession() } },
            )
            Text(
                text = "Event log",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF172026),
            )
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(eventLog) { item ->
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        text = item,
                        color = Color(0xFF34444D),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusCard(target: ProximityTarget, event: ProximityEvent?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = target.label,
            color = Color(0xFF285B73),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Target ID: ${target.id}",
            color = Color(0xFF5B6770),
        )
        Text(
            text = "Confidence: ${event?.confidence ?: ProximityConfidence.Unknown}",
            color = Color(0xFF172026),
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = event?.detail ?: "No active proximity session.",
            color = Color(0xFF5B6770),
        )
    }
}

@Composable
private fun ControlPanel(
    onStart: () -> Unit,
    onNearby: () -> Unit,
    onVerified: () -> Unit,
    onFailed: () -> Unit,
    onStop: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF285B73)),
        ) {
            Text("Start fake session")
        }
        Row {
            OutlinedButton(onClick = onNearby) {
                Text("Nearby")
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(onClick = onVerified) {
                Text("Verified")
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedButton(onClick = onFailed) {
                Text("Failed")
            }
        }
        OutlinedButton(onClick = onStop) {
            Text("Stop session")
        }
    }
}

@Composable
private fun LookLatchPhoneTheme(content: @Composable () -> Unit) {
    MaterialTheme(content = content)
}
