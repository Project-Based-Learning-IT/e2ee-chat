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
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import me.siddheshkothadi.chat.ui.screens.ChatListScreen
import me.siddheshkothadi.chat.ui.screens.ChatScreen
import me.siddheshkothadi.chat.ui.screens.LoggedOutScreen
import me.siddheshkothadi.chat.ui.screens.LoginScreen
import me.siddheshkothadi.chat.ui.theme.ChatTheme
import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPublicKey
import java.security.spec.*

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class,
        androidx.compose.animation.ExperimentalAnimationApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = !isSystemInDarkTheme()

            val navController = rememberAnimatedNavController()

            val isSignedIn by mainViewModel.isSignedIn.collectAsState()

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
                    AnimatedNavHost(navController, startDestination = if(isSignedIn) "chatList" else "login") {
                        composable(
                            "login"
                        ) {
                            LoggedOutScreen(navController, mainViewModel)
                        }

                        composable(
                            "chatList"
                        ) {
                            ChatListScreen(navController, mainViewModel)
                        }

                        composable(
                            "chat/{uid}"
                        ) {
                            val uid = navController.currentBackStackEntry?.arguments?.getString("uid")

                            if (uid != null) {
                                val user = mainViewModel.users.value.first {
                                    it.uid == uid
                                }
                                ChatScreen(
                                    navHostController = navController,
                                    mainViewModel = mainViewModel,
                                    user = user,
                                )
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