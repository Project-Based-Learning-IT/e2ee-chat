package me.siddheshkothadi.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import me.siddheshkothadi.chat.ui.components.Login
import me.siddheshkothadi.chat.ui.screens.ChatScreen
import me.siddheshkothadi.chat.ui.screens.LoginScreen
import me.siddheshkothadi.chat.ui.theme.ChatTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class,
        androidx.compose.animation.ExperimentalAnimationApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val mainViewModel: MainViewModel by viewModels()

        setContent {
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = !isSystemInDarkTheme()

            val navController = rememberAnimatedNavController()

            SideEffect {
                systemUiController.setSystemBarsColor(
                    color = Color.Transparent,
                    darkIcons = useDarkIcons,
                )
            }

            ChatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AnimatedNavHost(navController, startDestination = "login") {
                        composable(
                            "login"
                        ) {
                            LoginScreen(navController)
                        }

                        composable(
                            "chat/{name}"
                        ) {
                            val name = navController.currentBackStackEntry?.arguments?.getString("name")
                            if (name != null) {
                                ChatScreen(navHostController = navController, name = name, mainViewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ChatTheme {
        Greeting("Android")
    }
}