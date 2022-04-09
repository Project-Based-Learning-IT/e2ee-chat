package me.siddheshkothadi.chat.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import me.siddheshkothadi.chat.MainViewModel

@Composable
fun Messages(
    modifier: Modifier,
    mainViewModel: MainViewModel,
    fromUid: String,
    key: String
) {
    val chats by mainViewModel.chats.collectAsState(listOf())
    val scrollState = rememberLazyListState()

    DisposableEffect(key) {
        mainViewModel.addMessageEventListener(key)
        onDispose {
            mainViewModel.removeMessageEventListener(key)
        }
    }

    LazyColumn(
        modifier = modifier,
        state = scrollState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        reverseLayout = true
    ) {
        items(chats) { message ->
            ChatBubble(message = message.content, isUserMe = fromUid == message.from)
        }
        item {
            DayHeader(dayString = "\uD83D\uDD12 Chats are end-to-end encrypted \uD83D\uDD12")
        }
    }
}