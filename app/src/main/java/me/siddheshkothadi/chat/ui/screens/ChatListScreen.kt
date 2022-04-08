package me.siddheshkothadi.chat.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import me.siddheshkothadi.chat.MainViewModel
import me.siddheshkothadi.chat.ui.components.ChatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navHostController: NavHostController,
    mainViewModel: MainViewModel
) {
    SideEffect {
        mainViewModel.fetchUsers()
    }

    val isUserListLoading by mainViewModel.isUserListLoading.collectAsState()
    val listOfUsers by mainViewModel.users

    Scaffold(
        topBar = {
            Surface(tonalElevation = 2.dp) {
                CenterAlignedTopAppBar(
                    title = {
                        Text("Chat")
                    },
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.statusBars.only(
                            WindowInsetsSides.Top
                        )
                    ),
                    actions = {
                        IconButton(onClick = {
                            mainViewModel.signOut()
                        }) {
                            Icon(Icons.Default.ExitToApp, null)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(Icons.Default.AccountCircle, "Account")
                        }
                    }
                )
            }
        },
    ) {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(listOfUsers) {
                ChatCard(user = it, onClickAction = {
                    navHostController.navigate("chat/${it.uid}")
                })
            }
            if(isUserListLoading) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(Modifier.width(24.dp).height(24.dp))
                    }
                }
            }
        }
    }
}