package me.siddheshkothadi.chat.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import me.siddheshkothadi.chat.ui.components.Login

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navHostController: NavHostController
) {
    Scaffold(
        topBar = {
            Surface(tonalElevation = 2.dp) {
                CenterAlignedTopAppBar(
                    title = {
                        Text("Chat")
                    },
                    Modifier.windowInsetsPadding(WindowInsets.statusBars.only(WindowInsetsSides.Top))
                )
            }
        },
    ) {
        Login(navHostController = navHostController)
    }
}