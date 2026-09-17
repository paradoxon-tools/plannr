package de.chennemann.plannr.ui.screen.finances.partners

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import coil3.compose.SubcomposeAsyncImage
import de.chennemann.plannr.data.Partner
import de.chennemann.plannr.resources.Res
import dev.icerock.moko.resources.compose.painterResource

@Composable
fun PartnerLogo(partner: Partner?, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.size(40.dp), shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF5F5F5).copy(alpha = 0.7f), contentColor = Color(0xFF222222)) {
        Box(Modifier.fillMaxSize().padding(5.dp), contentAlignment = Alignment.Center) {
            val fallback: @Composable () -> Unit = {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (partner != null && partner.partnerId >= 0) {
                    Text(partner.name.trim().split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull() }.joinToString("").uppercase(),
                        modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center, maxLines = 1,
                        autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = 14.sp, stepSize = 0.5.sp),
                        color = Color(0xFF222222), style = MaterialTheme.typography.titleSmall)
                } else {
                    Icon(painterResource(Res.images.wallet), null, Modifier.size(24.dp), tint = Color(0xFF222222))
                }
                }
            }
            if (partner?.logoUrl != null) {
                SubcomposeAsyncImage(model = partner.logoUrl, contentDescription = "${partner.name} logo",
                    contentScale = ContentScale.Fit, alignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(6.dp)),
                    loading = { fallback() }, error = { fallback() })
            } else fallback()
        }
    }
}
