package me.siddheshkothadi.chat.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.siddheshkothadi.chat.domain.model.Message
import me.siddheshkothadi.chat.ui.viewmodel.MainViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Messages(
    modifier: Modifier,
    mainViewModel: MainViewModel,
    fromUid: String,
    key: String
) {
    val chats: Map<String, List<Message>> by mainViewModel.chats.collectAsState(emptyMap())
    val scrollState = rememberLazyListState()

    DisposableEffect(key) {
        mainViewModel.addMessageEventListener(key)
        onDispose {
            mainViewModel.removeMessageEventListener(key)
            mainViewModel.clearChats()
        }
    }

    LazyColumn(
        modifier = modifier,
        state = scrollState,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        reverseLayout = true
    ) {
        chats.forEach { (date, messages) ->
            items(messages) { message ->
                ChatBubble(message = message.content, isUserMe = fromUid == message.from, time = message.time)
            }
            stickyHeader {
                DayHeader(dayString = date)
            }
        }
        item {
            DayHeader(dayString = "\uD83D\uDD12 Chats are end-to-end encrypted \uD83D\uDD12")
        }
    }
}