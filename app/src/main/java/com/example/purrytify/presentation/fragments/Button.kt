//package com.example.purrytify.presentation.fragments
//
//import android.widget.Button
//import androidx.compose.ui.Modifier
//
//fun Button(
//    text: String,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier,
//    enabled: Boolean = true,
//    isLoading: Boolean = false
//) {
//    Button(
//        onClick = onClick,
//        modifier = modifier,
//        enabled = enabled,
//        colors = ButtonDefaults.buttonColors(
//            containerColor = MaterialTheme.colorScheme.primary,
//            contentColor = Color.White
//        ),
//        shape = RoundedCornerShape(8.dp),
//        elevation = ButtonDefaults.buttonElevation(
//            defaultElevation = 4.dp,
//            pressedElevation = 8.dp
//        )
//    ) {
//        if (isLoading) {
//            CircularProgressIndicator(
//                color = Color.White,
//                modifier = Modifier.size(16.dp)
//            )
//        } else {
//            Text(text)
//        }
//    }
//})