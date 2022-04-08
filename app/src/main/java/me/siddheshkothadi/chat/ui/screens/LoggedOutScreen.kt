package me.siddheshkothadi.chat.ui.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import me.siddheshkothadi.chat.MainViewModel
import me.siddheshkothadi.chat.R

@Composable
fun LoggedOutScreen(
    navHostController: NavHostController,
    mainViewModel: MainViewModel
) {
    val context = LocalContext.current
    val lottieCompositionSpec = if(isSystemInDarkTheme()) LottieCompositionSpec.RawRes(R.raw.chat_dark) else LottieCompositionSpec.RawRes(R.raw.chat_light)
    val composition by rememberLottieComposition(lottieCompositionSpec)
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever,
    )

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
            mainViewModel.signWithCredential(credential)
        } catch (e: ApiException) {
            Log.w("TAG", "Google sign in failed", e)
        }
    }

    fun launchGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("91794365719-fn0c30j3sqfstf3bf50ilkbmtfs3crl0.apps.googleusercontent.com")
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        launcher.launch(googleSignInClient.signInIntent)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("\uD83D\uDD10", fontWeight = FontWeight.SemiBold, fontSize = 36.sp, textAlign = TextAlign.Center)
        Text("Chat", fontWeight = FontWeight.SemiBold, fontSize = 48.sp, textAlign = TextAlign.Center)

        LottieAnimation(
            composition,
            progress,
            modifier = Modifier.height(300.dp).width(300.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth().systemBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = { launchGoogleSignIn() },
                modifier = Modifier.width(300.dp)
            ) {
                Text("Login")
            }
        }
    }
}