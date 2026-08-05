@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package de.chennemann.plannr.prototype.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF171717)
private val Paper = Color(0xFFF7F6F2)
private val Muted = Color(0xFF686864)
private val Rule = Color(0xFFD4D2CB)
private val Danger = Color(0xFFB3261E)

private enum class Variant(val label: String) { A("Focus deck"), B("Guided path"), C("Compact sheet") }
private enum class Kind { EXPENSE, INCOME, TRANSFER }
private enum class Rhythm { ONE_OFF, RECURRING }
private enum class Field { AMOUNT, FROM, TO, PARTNER, PROFILE, CONTRACT, DATE, SCHEDULE, NOTE }

private class EditorState {
    var kind by mutableStateOf(Kind.EXPENSE)
    var rhythm by mutableStateOf(Rhythm.ONE_OFF)
    var amount by mutableStateOf("84.20")
    var from by mutableStateOf("Everyday · Available")
    var to by mutableStateOf("—")
    var partner by mutableStateOf("Nordmarkt")
    var profile by mutableStateOf("Household")
    var contract by mutableStateOf("Groceries")
    var date by mutableStateOf("Thu, 6 Aug")
    var schedule by mutableStateOf("Monthly · day 6 · no end")
    var note by mutableStateOf("")
    var online by mutableStateOf(true)
    var saveInterrupted by mutableStateOf(false)
    var activeField by mutableStateOf<Field?>(Field.AMOUNT)
    var step by mutableIntStateOf(0)

    fun chooseKind(value: Kind) {
        kind = value
        if (value == Kind.TRANSFER) { partner = "—"; contract = "—"; to = "Holiday · Travel" }
        else { if (partner == "—") partner = "Nordmarkt"; if (contract == "—") contract = "Groceries"; to = "—" }
    }
}

@Composable
fun TransactionEditorPrototype() {
    var variant by remember { mutableStateOf(Variant.A) }
    val state = remember { EditorState() }
    MaterialTheme(colorScheme = lightColorScheme(primary = Ink, onPrimary = Color.White, background = Paper, surface = Paper)) {
        Box(
            Modifier.fillMaxSize().background(Paper).focusable().onPreviewKeyEvent {
                if (it.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (it.key) {
                    Key.DirectionLeft -> { variant = Variant.entries[(variant.ordinal + 2) % 3]; true }
                    Key.DirectionRight -> { variant = Variant.entries[(variant.ordinal + 1) % 3]; true }
                    Key.O -> { state.online = !state.online; state.saveInterrupted = false; true }
                    Key.S -> { state.online = false; state.saveInterrupted = true; true }
                    Key.Escape -> { state.activeField = null; true }
                    else -> false
                }
            }
        ) {
            when (variant) {
                Variant.A -> FocusDeck(state)
                Variant.B -> GuidedPath(state)
                Variant.C -> CompactSheet(state)
            }
            ConnectivityBadge(state, Modifier.align(Alignment.TopEnd).padding(12.dp))
            VariantSwitcher(variant, { variant = Variant.entries[(variant.ordinal + 2) % 3] }, { variant = Variant.entries[(variant.ordinal + 1) % 3] }, Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable private fun EditorHeader(state: EditorState, title: String, subtitle: String) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 18.dp)) {
        Text("FINANCES  /  NEW ENTRY", fontSize = 11.sp, color = Muted, letterSpacing = 1.4.sp)
        Spacer(Modifier.height(12.dp)); Text(title, fontSize = 28.sp, fontWeight = FontWeight.SemiBold)
        Text(subtitle, fontSize = 13.sp, color = Muted)
        Spacer(Modifier.height(18.dp)); Segments(Kind.entries, state.kind, { it.name.lowercase().replaceFirstChar(Char::uppercase) }) { state.chooseKind(it) }
        Spacer(Modifier.height(8.dp)); Segments(Rhythm.entries, state.rhythm, { if (it == Rhythm.ONE_OFF) "One-off" else "Recurring" }) { state.rhythm = it }
    }
}

@Composable private fun <T> Segments(values: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    Row(Modifier.fillMaxWidth().border(1.dp, Ink)) {
        values.forEach { value ->
            Box(Modifier.weight(1f).background(if (value == selected) Ink else Color.Transparent).clickable { onSelect(value) }.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                Text(label(value), color = if (value == selected) Color.White else Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable private fun FocusDeck(state: EditorState) {
    Column(Modifier.fillMaxSize().padding(bottom = 72.dp)) {
        EditorHeader(state, "Transaction entry", "Keep the whole entry visible; focus one input below.")
        SummaryCard(state, Modifier.padding(horizontal = 24.dp))
        Spacer(Modifier.height(12.dp))
        FocusTabs(state)
        Box(Modifier.fillMaxWidth().weight(1f).background(Ink).padding(24.dp)) { FocusInput(state) }
        SaveBar(state)
    }
}

@Composable private fun SummaryCard(state: EditorState, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().border(1.dp, Rule).padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(state.kind.name, fontSize = 11.sp, color = Muted, letterSpacing = 1.sp)
            Text("€ ${state.amount}", fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(12.dp))
        SummaryLine("FROM", state.from)
        if (state.kind == Kind.TRANSFER) SummaryLine("TO", state.to) else SummaryLine("WITH", state.partner)
        SummaryLine("PROFILE", state.profile)
        if (state.kind != Kind.TRANSFER) SummaryLine("CONTRACT", state.contract)
        SummaryLine(if (state.rhythm == Rhythm.RECURRING) "STARTS" else "DATE", state.date)
        if (state.rhythm == Rhythm.RECURRING) SummaryLine("REPEATS", state.schedule)
    }
}

@Composable private fun SummaryLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 10.sp, color = Muted); Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable private fun FocusTabs(state: EditorState) {
    val fields = buildList { add(Field.AMOUNT); add(Field.FROM); if (state.kind == Kind.TRANSFER) add(Field.TO) else add(Field.PARTNER); add(Field.PROFILE); if (state.kind != Kind.TRANSFER) add(Field.CONTRACT); add(Field.DATE); if (state.rhythm == Rhythm.RECURRING) add(Field.SCHEDULE) }
    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        fields.forEach { field -> Text(field.name.take(3), Modifier.clickable { state.activeField = field }.padding(7.dp), color = if (state.activeField == field) Ink else Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable private fun BoxScope.FocusInput(state: EditorState) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text((state.activeField ?: Field.AMOUNT).name, color = Color(0xFFAAA9A4), fontSize = 11.sp, letterSpacing = 1.5.sp)
        Spacer(Modifier.height(14.dp))
        when (state.activeField) {
            Field.AMOUNT, null -> {
                Text("€ ${state.amount}", color = Color.White, fontSize = 40.sp)
                Spacer(Modifier.height(14.dp)); NumberPad { state.amount = if (it == "⌫") state.amount.dropLast(1) else state.amount + it }
            }
            Field.FROM, Field.TO -> Choices(listOf("Everyday · Available", "Bills · Available", "Holiday · Travel")) { if (state.activeField == Field.FROM) state.from = it else state.to = it }
            Field.PARTNER -> Choices(listOf("Nordmarkt", "Landlord", "Employer", "+ New partner")) { state.partner = it }
            Field.PROFILE -> Choices(listOf("Household", "Personal", "Work", "No profile")) { state.profile = it }
            Field.CONTRACT -> Choices(listOf("Groceries", "Rent", "Salary", "No contract")) { state.contract = it }
            Field.DATE -> Choices(listOf("Today · Thu, 6 Aug", "Tomorrow · Fri, 7 Aug", "Choose date…")) { state.date = it.substringAfter("· ", it) }
            Field.SCHEDULE -> Choices(listOf("Weekly · Thursday", "Monthly · day 6 · no end", "Yearly · 6 August")) { state.schedule = it }
            Field.NOTE -> Unit
        }
    }
}

@Composable private fun NumberPad(onKey: (String) -> Unit) {
    Column(Modifier.width(260.dp)) { listOf(listOf("1","2","3"), listOf("4","5","6"), listOf("7","8","9"), listOf(".","0","⌫")).forEach { row -> Row { row.forEach { key -> Text(key, Modifier.weight(1f).clickable { onKey(key) }.padding(10.dp), color = Color.White, fontSize = 20.sp) } } } }
}

@Composable private fun Choices(items: List<String>, onPick: (String) -> Unit) {
    Column(Modifier.fillMaxWidth()) { items.forEach { item -> Row(Modifier.fillMaxWidth().clickable { onPick(item) }.padding(vertical = 13.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(item, color = Color.White, fontSize = 17.sp); Text("→", color = Color.White) }; HorizontalDivider(color = Color(0xFF444444)) } }
}

@Composable private fun GuidedPath(state: EditorState) {
    val steps = guidedSteps(state)
    Column(Modifier.fillMaxSize().padding(bottom = 72.dp)) {
        EditorHeader(state, "Build the entry", "A short path changes with transaction type and recurrence.")
        Row(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 24.dp)) {
            Column(Modifier.width(112.dp).fillMaxHeight().border(1.dp, Rule)) {
                steps.forEachIndexed { index, field ->
                    Column(Modifier.fillMaxWidth().background(if (index == state.step.coerceAtMost(steps.lastIndex)) Ink else Color.Transparent).clickable { state.step = index }.padding(12.dp)) {
                        Text("${index + 1}", color = if (index == state.step) Color.White else Muted, fontSize = 10.sp)
                        Text(stepLabel(field), color = if (index == state.step) Color.White else Ink, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Column(Modifier.weight(1f).fillMaxHeight().padding(start = 18.dp)) {
                val field = steps[state.step.coerceAtMost(steps.lastIndex)]
                Text(stepLabel(field), fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                Text(stepPrompt(field), color = Muted, fontSize = 13.sp)
                Spacer(Modifier.height(16.dp)); GuidedChoices(state, field)
                Spacer(Modifier.weight(1f)); HorizontalDivider(color = Rule); Spacer(Modifier.height(10.dp))
                Text("LIVE RECEIPT", fontSize = 10.sp, color = Muted, letterSpacing = 1.sp)
                Text("${state.kind.name.lowercase()}  ·  € ${state.amount}", fontWeight = FontWeight.Bold)
                Text(if (state.kind == Kind.TRANSFER) "${state.from} → ${state.to}" else "${state.partner} · ${state.from}", fontSize = 12.sp)
                Text(if (state.rhythm == Rhythm.RECURRING) state.schedule else state.date, fontSize = 12.sp, color = Muted)
            }
        }
        Row(Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton({ state.step = (state.step - 1).coerceAtLeast(0) }, Modifier.weight(1f)) { Text("Back") }
            Button({ state.step = (state.step + 1).coerceAtMost(steps.lastIndex) }, Modifier.weight(1f), enabled = state.online) { Text(if (state.step == steps.lastIndex) "Create" else "Continue") }
        }
        OfflineMessage(state)
    }
}

private fun guidedSteps(state: EditorState) = buildList { add(Field.AMOUNT); add(Field.FROM); add(if (state.kind == Kind.TRANSFER) Field.TO else Field.PARTNER); add(Field.PROFILE); if (state.kind != Kind.TRANSFER) add(Field.CONTRACT); add(if (state.rhythm == Rhythm.RECURRING) Field.SCHEDULE else Field.DATE) }
private fun stepLabel(field: Field) = when (field) { Field.FROM -> "Source"; Field.TO -> "Destination"; Field.PARTNER -> "Partner"; Field.PROFILE -> "Profile"; Field.CONTRACT -> "Contract"; Field.SCHEDULE -> "Schedule"; else -> field.name.lowercase().replaceFirstChar(Char::uppercase) }
private fun stepPrompt(field: Field) = when (field) { Field.FROM -> "Which pocket provides the funds?"; Field.TO -> "Where should the funds arrive?"; Field.PARTNER -> "Who is on the other side?"; Field.PROFILE -> "Which financial context owns this entry?"; Field.CONTRACT -> "Does an obligation explain this entry?"; Field.SCHEDULE -> "When should future entries occur?"; Field.DATE -> "When does this entry occur?"; else -> "Enter the signed transaction amount." }

@Composable private fun GuidedChoices(state: EditorState, field: Field) {
    val choices = when (field) { Field.AMOUNT -> listOf("€ 84.20", "€ 125.00", "Custom amount"); Field.FROM -> listOf("Everyday · Available", "Bills · Available"); Field.TO -> listOf("Holiday · Travel", "Bills · Available"); Field.PARTNER -> listOf("Nordmarkt", "Landlord", "Employer"); Field.PROFILE -> listOf("Household", "Personal", "No profile"); Field.CONTRACT -> listOf("Groceries", "Rent", "No contract"); Field.SCHEDULE -> listOf("Weekly · Thursday", "Monthly · day 6 · no end", "Custom schedule"); else -> listOf("Today · Thu, 6 Aug", "Tomorrow · Fri, 7 Aug", "Choose date") }
    Column { choices.forEach { value -> Surface(Modifier.fillMaxWidth().padding(bottom = 8.dp).clickable { assign(state, field, value) }, border = androidx.compose.foundation.BorderStroke(1.dp, Rule), color = Color.Transparent) { Text(value, Modifier.padding(12.dp), fontSize = 13.sp) } } }
}

@Composable private fun CompactSheet(state: EditorState) {
    Box(Modifier.fillMaxSize().padding(bottom = 72.dp)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            EditorHeader(state, if (state.kind == Kind.EXPENSE) "New expense" else if (state.kind == Kind.INCOME) "New income" else "New transfer", "Everything scannable; tap a row for a focused picker.")
            Column(Modifier.padding(horizontal = 24.dp)) {
                LargeAmount(state)
                FormRow("From", state.from) { state.activeField = Field.FROM }
                if (state.kind == Kind.TRANSFER) FormRow("To", state.to) { state.activeField = Field.TO } else FormRow("Partner", state.partner) { state.activeField = Field.PARTNER }
                FormRow("Financial profile", state.profile) { state.activeField = Field.PROFILE }
                if (state.kind != Kind.TRANSFER) FormRow("Contract", state.contract) { state.activeField = Field.CONTRACT }
                FormRow(if (state.rhythm == Rhythm.RECURRING) "First occurrence" else "Date", state.date) { state.activeField = Field.DATE }
                if (state.rhythm == Rhythm.RECURRING) FormRow("Schedule", state.schedule) { state.activeField = Field.SCHEDULE }
                FormRow("Note", if (state.note.isEmpty()) "Optional" else state.note) { state.activeField = Field.NOTE }
                Spacer(Modifier.height(22.dp)); Button({}, Modifier.fillMaxWidth(), enabled = state.online) { Text(if (state.online) "Create transaction entry" else "Connect to create") }
                OfflineMessage(state); Spacer(Modifier.height(80.dp))
            }
        }
        if (state.activeField != null && state.activeField != Field.AMOUNT) FullPicker(state, state.activeField!!)
    }
}

@Composable private fun LargeAmount(state: EditorState) { Row(Modifier.fillMaxWidth().padding(vertical = 18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) { Text("Amount", color = Muted, fontSize = 12.sp); Text("€ ${state.amount}", fontSize = 36.sp, fontWeight = FontWeight.SemiBold) }; HorizontalDivider(color = Ink) }
@Composable private fun FormRow(label: String, value: String, onClick: () -> Unit) { Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 15.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(label, color = Muted, fontSize = 13.sp); Row { Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium); Spacer(Modifier.width(10.dp)); Text("›") } }; HorizontalDivider(color = Rule) }

@Composable private fun FullPicker(state: EditorState, field: Field) {
    Surface(Modifier.fillMaxSize(), color = Paper) {
        Column(Modifier.fillMaxSize().padding(24.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(stepLabel(field), fontSize = 28.sp, fontWeight = FontWeight.SemiBold); Text("Close", Modifier.clickable { state.activeField = null }.padding(8.dp), fontSize = 12.sp) }
            Text(stepPrompt(field), color = Muted); Spacer(Modifier.height(24.dp))
            val values = when (field) { Field.FROM -> listOf("Everyday · Available", "Bills · Available", "Holiday · Travel"); Field.TO -> listOf("Holiday · Travel", "Bills · Available"); Field.PARTNER -> listOf("Nordmarkt", "Landlord", "Employer"); Field.PROFILE -> listOf("Household", "Personal", "No profile"); Field.CONTRACT -> listOf("Groceries", "Rent", "No contract"); Field.DATE -> listOf("Thu, 6 Aug", "Fri, 7 Aug", "Choose date…"); Field.SCHEDULE -> listOf("Weekly · Thursday", "Monthly · day 6 · no end", "Yearly · 6 August"); else -> listOf("Optional note") }
            values.forEach { value -> Row(Modifier.fillMaxWidth().clickable { assign(state, field, value); state.activeField = null }.padding(vertical = 18.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(value, fontSize = 17.sp); Text("○") }; HorizontalDivider(color = Rule) }
        }
    }
}

private fun assign(state: EditorState, field: Field, value: String) { when (field) { Field.AMOUNT -> state.amount = value.removePrefix("€ "); Field.FROM -> state.from = value; Field.TO -> state.to = value; Field.PARTNER -> state.partner = value; Field.PROFILE -> state.profile = value; Field.CONTRACT -> state.contract = value; Field.DATE -> state.date = value.substringAfter("· ", value); Field.SCHEDULE -> state.schedule = value; Field.NOTE -> state.note = value } }

@Composable private fun SaveBar(state: EditorState) { Column(Modifier.fillMaxWidth().background(Paper).padding(horizontal = 24.dp, vertical = 10.dp)) { Button({}, Modifier.fillMaxWidth(), enabled = state.online) { Text(if (state.online) "Create transaction entry" else "Connect to create") }; OfflineMessage(state) } }
@Composable private fun OfflineMessage(state: EditorState) { if (!state.online) Text(if (state.saveInterrupted) "Connection lost before confirmation. Nothing was queued; check the feed before retrying." else "Offline copy is read-only. This entry stays only on this screen and cannot be submitted.", color = Danger, fontSize = 11.sp, modifier = Modifier.padding(top = 6.dp)) }
@Composable private fun ConnectivityBadge(state: EditorState, modifier: Modifier) { Surface(modifier, shape = CircleShape, color = if (state.online) Ink else Danger) { Text(if (state.online) "ONLINE" else "OFFLINE", Modifier.padding(horizontal = 10.dp, vertical = 6.dp), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold) } }
@Composable private fun VariantSwitcher(variant: Variant, previous: () -> Unit, next: () -> Unit, modifier: Modifier) { Surface(modifier.padding(bottom = 12.dp), shape = RoundedCornerShape(24.dp), color = Color(0xFFEEECE6), shadowElevation = 8.dp, border = androidx.compose.foundation.BorderStroke(1.dp, Ink)) { Row(verticalAlignment = Alignment.CenterVertically) { Text("←", Modifier.clickable(onClick = previous).padding(14.dp), fontSize = 18.sp); Text("${variant.name} — ${variant.label}", Modifier.width(160.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center); Text("→", Modifier.clickable(onClick = next).padding(14.dp), fontSize = 18.sp) } } }
