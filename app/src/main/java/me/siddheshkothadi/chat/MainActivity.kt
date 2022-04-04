package me.siddheshkothadi.chat

import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import me.siddheshkothadi.chat.ui.screens.ChatScreen
import me.siddheshkothadi.chat.ui.screens.LoginScreen
import me.siddheshkothadi.chat.ui.theme.ChatTheme
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import java.security.spec.*

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