package me.siddheshkothadi.chat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import me.siddheshkothadi.chat.ui.components.Login
import me.siddheshkothadi.chat.ui.components.Messages

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navHostController: NavHostController,
    name: String
) {
    Scaffold(
        topBar = {
            Surface(tonalElevation = 2.dp) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(name)
                    },
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.statusBars.only(
                            WindowInsetsSides.Top)),
                    navigationIcon = {
                        IconButton(onClick = {
                            navHostController.popBackStack()
                        }) {
                            Icon(Icons.Default.ArrowBack, null)
                        }
                    }
                )
            }
        },
    ) {
        val scrollState = rememberLazyListState()

        var textState by remember { mutableStateOf("") }

        Column(Modifier.fillMaxSize()) {
            Messages(
                Modifier
                    .fillMaxWidth()
                    .weight(1f), scrollState, name)
            Surface(
                tonalElevation = 2.dp,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding().padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    BasicTextField(
                        value = textState,
                        onValueChange = { textState = it },
                        modifier = Modifier.weight(1f).padding(horizontal = 24.dp, vertical = 18.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Send
                        ),
                        maxLines = 3,
                        cursorBrush = SolidColor(LocalContentColor.current),
                        textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current, fontSize = 16.sp)
                    )
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(Icons.Default.Send, "Send")
                    }
                }
            }
        }
    }
}