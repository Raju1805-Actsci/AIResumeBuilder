package com.airesume.builder.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airesume.builder.viewmodel.FormStep

// ─── Step Progress Indicator ──────────────────────────────────────────────────

@Composable
fun StepProgressIndicator(
    steps: List<FormStep>,
    currentStep: FormStep,
    onStepClick: (FormStep) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSteps = steps.size
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, step ->
            val isPast    = step.index < currentStep.index
            val isCurrent = step == currentStep

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onStepClick(step) }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isPast    -> MaterialTheme.colorScheme.primary
                                isCurrent -> MaterialTheme.colorScheme.primary
                                else      -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                ) {
                    if (isPast) {
                        Icon(
                            Icons.Default.Check, contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isCurrent) Color.White
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (isCurrent) {
                    Text(
                        text = step.title.take(8),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            if (index < totalSteps - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .padding(horizontal = 2.dp)
                        .background(
                            if (step.index < currentStep.index)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        )
                )
            }
        }
    }
}

// ─── Section Card ─────────────────────────────────────────────────────────────

@Composable
fun SectionCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon, contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

// ─── Resume Form TextField ────────────────────────────────────────────────────

@Composable
fun ResumeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: ImageVector? = null,
    isRequired: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                if (isRequired) "$label *" else label,
                style = MaterialTheme.typography.bodySmall
            )
        },
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodySmall) },
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, modifier = Modifier.size(18.dp)) } },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            focusedLabelColor    = MaterialTheme.colorScheme.primary
        )
    )
}

// ─── AI Generate Button ───────────────────────────────────────────────────────

@Composable
fun AiGenerateButton(
    onClick: () -> Unit,
    isLoading: Boolean,
    progress: String = "",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ai_anim")
    val gradientAngle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "angle"
    )

    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        if (isLoading) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Text(
                    progress.ifEmpty { "Generating with AI…" },
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Text("✨ Generate Resume with AI", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─── Skill Chip ───────────────────────────────────────────────────────────────

@Composable
fun SkillChip(
    text: String,
    onRemove: (() -> Unit)? = null,
    isAiSuggested: Boolean = false
) {
    val containerColor = if (isAiSuggested)
        MaterialTheme.colorScheme.secondaryContainer
    else
        MaterialTheme.colorScheme.primaryContainer

    val contentColor = if (isAiSuggested)
        MaterialTheme.colorScheme.onSecondaryContainer
    else
        MaterialTheme.colorScheme.onPrimaryContainer

    AssistChip(
        onClick = { onRemove?.invoke() },
        label = {
            Text(text, style = MaterialTheme.typography.labelMedium, color = contentColor)
        },
        trailingIcon = onRemove?.let {
            { Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp), tint = contentColor) }
        },
        leadingIcon = if (isAiSuggested) {
            { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp), tint = contentColor) }
        } else null,
        colors = AssistChipDefaults.assistChipColors(containerColor = containerColor),
        border = AssistChipDefaults.assistChipBorder(enabled = true, borderColor = containerColor)
    )
}

// ─── Resume Card (Dashboard list) ────────────────────────────────────────────

@Composable
fun ResumeCard(
    title: String,
    updatedAt: Long,
    isComplete: Boolean,
    template: String,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onImprove: () -> Unit
) {
    val date = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        .format(java.util.Date(updatedAt))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Resume icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    Icons.Default.Description,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    if (isComplete) {
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.CheckCircle, contentDescription = null,
                            tint = Color(0xFF00C853), modifier = Modifier.size(14.dp))
                    }
                }
                Text("Updated $date", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(template, style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary)
            }
            // Actions
            var menuExpanded by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Default.Edit, null) },
                        onClick = { menuExpanded = false; onEdit() })
                    DropdownMenuItem(text = { Text("AI Improve") },
                        leadingIcon = { Icon(Icons.Default.AutoAwesome, null) },
                        onClick = { menuExpanded = false; onImprove() })
                    DropdownMenuItem(text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                        onClick = { menuExpanded = false; onDelete() })
                }
            }
        }
    }
}

// ─── Empty State ─────────────────────────────────────────────────────────────

@Composable
fun EmptyState(
    icon: ImageVector = Icons.Default.Description,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(24.dp))
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}
