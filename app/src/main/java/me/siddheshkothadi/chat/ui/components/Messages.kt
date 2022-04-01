package me.siddheshkothadi.chat.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.siddheshkothadi.chat.data.messages
import kotlin.random.Random

@Composable
fun Messages(
    modifier: Modifier,
    scrollState: LazyListState,
    name: String
) {
    LazyColumn(
        modifier = modifier,
        state = scrollState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        reverseLayout = true
    ) {
        items(messages.sortedByDescending { it.timestamp }) { message ->
            ChatBubble(message = message.content, isUserMe = name == message.from)
        }
        item {
            DayHeader(dayString = "Today")
        }
        item {
            DayHeader(dayString = "\uD83D\uDD12 Chats are end-to-end encrypted \uD83D\uDD12")
        }
    }
}