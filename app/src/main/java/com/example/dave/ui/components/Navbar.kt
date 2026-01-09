package com.example.dave.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dave.R
import com.example.dave.ui.theme.BrownPrimary
import com.example.dave.ui.theme.GreenPrimary

enum class DaveNavItem { HOME, ACCOUNT }

@Composable
fun NavBar(
    modifier: Modifier = Modifier,
    barHeight: Dp = 70.dp,
    fabSize: Dp = 92.dp,
    topRadius: Dp = 34.dp,
    selected: DaveNavItem = DaveNavItem.HOME,
    onHomeClick: () -> Unit,
    onAddClick: () -> Unit,
    onAccountClick: () -> Unit,
) {
    // Le Box doit être collé en bas (le parent s’en charge),
    // et assez haut pour laisser le bouton + dépasser vers le haut.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight + fabSize / 2),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Barre principale : pleine largeur, arrondie seulement en haut
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(
                    RoundedCornerShape(
                        topStart = topRadius,
                        topEnd = topRadius,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp
                    )
                )
                .background(BrownPrimary)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 60.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NavIcon(
                    drawableId = R.drawable.logo_navbar,
                    selected = selected == DaveNavItem.HOME,
                    onClick = onHomeClick
                )

                Spacer(Modifier.width(fabSize)) // réserve la place du +
                NavIcon(
                    drawableId = R.drawable.ic_user,
                    selected = selected == DaveNavItem.ACCOUNT,
                    onClick = onAccountClick
                )
            }
        }

        // Bouton central +
        Box(
            modifier = Modifier
                .offset(y = -(barHeight / 4))
                .size(fabSize)
                .clip(CircleShape)
                .background(GreenPrimary)
                .clickable { onAddClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.add),
                contentDescription = "Add",
                tint = Color.White,
                modifier = Modifier.size(50.dp)
            )
        }
    }
}

@Composable
private fun NavIcon(
    drawableId: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(60.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(drawableId),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(38.dp)
        )
    }
}
