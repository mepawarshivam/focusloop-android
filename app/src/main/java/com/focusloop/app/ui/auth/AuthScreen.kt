package com.focusloop.app.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focusloop.app.ui.components.AppLogo
import com.focusloop.app.ui.components.GradientButton
import com.focusloop.app.ui.theme.FocusPurple
import com.focusloop.app.ui.theme.FocusRed
import com.focusloop.app.ui.theme.FocusTeal
import compose.icons.FeatherIcons
import compose.icons.feathericons.Eye
import compose.icons.feathericons.EyeOff
import compose.icons.feathericons.Lock
import compose.icons.feathericons.Mail

@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthenticated: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0F0E1A), Color(0xFF1A1040), Color(0xFF0F0E1A)))
            )
            .padding(28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        AppLogo(size = 72.dp)

        Spacer(Modifier.height(24.dp))

        AnimatedContent(targetState = state.isSignUpMode, label = "auth_title") { isSignUp ->
            Text(
                if (isSignUp) "Create your account" else "Welcome back",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            "Just an email and password — nothing else, and it never leaves your device.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFB0AEC8)
        )

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            label = { Text("Email") },
            leadingIcon = { Icon(FeatherIcons.Mail, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = authFieldColors()
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            label = { Text("Password") },
            leadingIcon = { Icon(FeatherIcons.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) FeatherIcons.EyeOff else FeatherIcons.Eye,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = authFieldColors()
        )

        if (state.errorMessage != null) {
            Spacer(Modifier.height(12.dp))
            Text(
                state.errorMessage ?: "",
                color = FocusRed,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(28.dp))

        GradientButton(
            text = if (state.isSubmitting) "Please wait..." else if (state.isSignUpMode) "Sign Up" else "Log In",
            onClick = { if (!state.isSubmitting) viewModel.submit(onAuthenticated) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                if (state.isSignUpMode) "Already have an account?" else "Don't have an account?",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFB0AEC8)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                if (state.isSignUpMode) "Log in" else "Sign up",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = FocusTeal,
                modifier = Modifier.clickable { viewModel.toggleMode() }
            )
        }
    }
}

@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = FocusTeal,
    unfocusedBorderColor = Color(0xFF3A3860),
    focusedLabelColor = FocusTeal,
    unfocusedLabelColor = Color(0xFFB0AEC8),
    cursorColor = FocusTeal,
    focusedLeadingIconColor = FocusTeal,
    unfocusedLeadingIconColor = Color(0xFFB0AEC8),
    focusedTrailingIconColor = FocusTeal,
    unfocusedTrailingIconColor = Color(0xFFB0AEC8)
)
