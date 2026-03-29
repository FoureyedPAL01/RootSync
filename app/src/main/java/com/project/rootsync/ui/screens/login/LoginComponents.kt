package com.project.rootsync.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reusable text field matching Flutter's _buildField widget.
 */
@Composable
fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    hint: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    obscureText: Boolean = false,
    onToggleObscure: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Email
) {
    val colors = MaterialTheme.colorScheme

    Column(modifier = modifier) {
        // Label text
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = colors.onSurface.copy(alpha = 0.55f)
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Text field
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            visualTransformation = if (isPassword && obscureText) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            cursorBrush = SolidColor(colors.primary),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                textDecoration = TextDecoration.None
            ),
            placeholder = {
                Text(
                    text = hint,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = colors.onSurface.copy(alpha = 0.28f)
                    )
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.onSurface.copy(alpha = 0.38f),
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = if (isPassword && onToggleObscure != null) {
                {
                    IconButton(onClick = onToggleObscure, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (obscureText) {
                                Icons.Outlined.Visibility
                            } else {
                                Icons.Outlined.VisibilityOff
                            },
                            contentDescription = if (obscureText) "Show password" else "Hide password",
                            tint = colors.onSurface.copy(alpha = 0.38f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                null
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.primary,
                unfocusedBorderColor = colors.onSurface.copy(alpha = 0.08f),
                focusedContainerColor = colors.surfaceContainerHighest.copy(alpha = 0.35f),
                unfocusedContainerColor = colors.surfaceContainerHighest.copy(alpha = 0.35f),
                cursorColor = colors.primary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Error message box matching Flutter's _buildErrorBox widget.
 */
@Composable
fun ErrorBox(message: String) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.errorContainer)
            .border(
                width = 1.dp,
                color = colors.error.copy(alpha = 0.3f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.ErrorOutline,
            contentDescription = null,
            tint = colors.error,
            modifier = Modifier.size(17.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = colors.onErrorContainer
            ),
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Primary action button matching Flutter's _buildPrimaryButton widget.
 */
@Composable
fun PrimaryButton(
    label: String,
    icon: ImageVector,
    isLoading: Boolean,
    onClick: (() -> Unit)?
) {
    val colors = MaterialTheme.colorScheme

    Button(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.primary,
            contentColor = colors.onPrimary,
            disabledContainerColor = colors.primary.copy(alpha = 0.6f),
            disabledContentColor = colors.onPrimary.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 24.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(17.dp),
                color = colors.onPrimary,
                strokeWidth = 2.2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = colors.onPrimary
            )
        )
    }
}
