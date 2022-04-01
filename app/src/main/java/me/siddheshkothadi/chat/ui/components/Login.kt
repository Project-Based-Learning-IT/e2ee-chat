package me.siddheshkothadi.chat.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@Composable
fun Login(
    navHostController: NavHostController
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Chat with", fontSize = 22.sp)
        Spacer(Modifier.height(24.dp))
        FilledTonalButton(onClick = {
            navHostController.navigate("chat/Alice")
        }) {
            Text("Alice as Bob")
        }
        FilledTonalButton(onClick = {
            navHostController.navigate("chat/Bob")
        }) {
            Text("Bob as Alice")
        }
    }
}