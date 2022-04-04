package me.siddheshkothadi.chat.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.siddheshkothadi.chat.MainViewModel
import me.siddheshkothadi.chat.model.Message

@Composable
fun Messages(
    modifier: Modifier,
    scrollState: LazyListState,
    name: String,
    mainViewModel: MainViewModel
) {
    val chats = mainViewModel.chats.observeAsState(emptyList<Message>().toMutableList())

    LazyColumn(
        modifier = modifier,
        state = scrollState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        reverseLayout = true
    ) {
        items(chats.value) { message ->
            ChatBubble(message = message.content, isUserMe = name == message.to)
        }
        item {
            DayHeader(dayString = "Today")
        }
        item {
            DayHeader(dayString = "\uD83D\uDD12 Chats are end-to-end encrypted \uD83D\uDD12")
        }
    }
}