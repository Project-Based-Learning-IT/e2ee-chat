package me.siddheshkothadi.chat.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
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
        colorScheme.primary
    } else {
        colorScheme.surfaceVariant
    }

    val uriHandler = LocalUriHandler.current

    val styledMessage = messageFormatter(
        text = message,
        primary = isUserMe
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(3.dp),
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
                SelectionContainer {
                    ClickableText(
                        text = styledMessage,
                        style = MaterialTheme.typography.bodyLarge.copy(color = LocalContentColor.current),
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                        onClick = {
                            styledMessage.getStringAnnotations(start = it, end = it).firstOrNull()?.let { annotation ->
                                when(annotation.tag) {
                                    SymbolAnnotationType.LINK.name -> uriHandler.openUri(annotation.item)
                                    else -> Unit
                                }
                            }
                        }
                    )
                }
                Text(
                    text = time,
                    modifier = Modifier
                        .padding(end = 20.dp, bottom = 10.dp)
                        .alpha(0.4f),
                    textAlign = TextAlign.Right,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

private val symbolPattern by lazy {
    Regex("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")
}

typealias StringAnnotation = AnnotatedString.Range<String>
// Pair returning styled content and annotation for ClickableText when matching syntax token
typealias SymbolAnnotation = Pair<AnnotatedString, StringAnnotation?>

// Accepted annotations for the ClickableTextWrapper
enum class SymbolAnnotationType {
    LINK
}

@Composable
private fun messageFormatter(
    text: String,
    primary: Boolean
): AnnotatedString {
    val tokens = symbolPattern.findAll(text)

    return buildAnnotatedString {
        var cursorPosition = 0

        for (token in tokens) {
            append(text.slice(cursorPosition until token.range.first))

            val (annotatedString, stringAnnotation) = SymbolAnnotation(
                AnnotatedString(
                    text = token.value,
                    spanStyle = SpanStyle(
//                        color = if (primary) colorScheme.inversePrimary else colorScheme.primary,
                        textDecoration = TextDecoration.Underline
                    )
                ),
                StringAnnotation(
                    item = token.value,
                    start = token.range.first,
                    end = token.range.last,
                    tag = SymbolAnnotationType.LINK.name
                )
            )
            append(annotatedString)

            if (stringAnnotation != null) {
                val (item, start, end, tag) = stringAnnotation
                addStringAnnotation(tag = tag, start = start, end = end, annotation = item)
            }

            cursorPosition = token.range.last + 1
        }

        if (!tokens.none()) {
            append(text.slice(cursorPosition..text.lastIndex))
        } else {
            append(text)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun preview() {
    ChatBubble("Hi there I'm using WhatsApp!", true)
}
