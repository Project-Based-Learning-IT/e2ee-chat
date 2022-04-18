package me.siddheshkothadi.chat.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.flowlayout.FlowCrossAxisAlignment
import com.google.accompanist.flowlayout.FlowRow
import com.google.accompanist.flowlayout.MainAxisAlignment

private val OtherChatBubbleShape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)
private val MyChatBubbleShape = RoundedCornerShape(20.dp, 4.dp, 20.dp, 20.dp)

@Composable
fun ChatBubble(
    message: String,
    isUserMe: Boolean,
    time: String = "19:04"
) {
    val backgroundBubbleColor = if (isUserMe) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(3.dp),
        horizontalAlignment = if(isUserMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = backgroundBubbleColor,
            shape = if(isUserMe) MyChatBubbleShape else OtherChatBubbleShape,
            modifier = Modifier.widthIn(20.dp, 228.dp)
        ) {
            FlowRow(
                mainAxisAlignment = MainAxisAlignment.End,
                crossAxisAlignment = FlowCrossAxisAlignment.End
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge.copy(color = LocalContentColor.current),
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                )
                Text(
                    text = time,
                    modifier = Modifier.padding(end = 20.dp, bottom = 10.dp).alpha(0.4f),
                    textAlign = TextAlign.Right,
                    color = MaterialTheme.colorScheme.background,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun preview() {
    ChatBubble("Hi there I'm using WhatsApp!", true)
}
