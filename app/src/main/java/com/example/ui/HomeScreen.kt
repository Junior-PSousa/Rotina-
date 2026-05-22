package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ServiceRecord
import com.example.ui.viewmodel.ActiveOutputState
import com.example.ui.viewmodel.ServiceViewModel
import java.util.Locale

// Dynamic Custom Theme Colors holder to fully support Modo Noturno & Agradável background
data class AppThemeColors(
    val bgStart: Color,
    val bgEnd: Color,
    val surface: Color,
    val primary: Color,
    val accent: Color,
    val textPrimary: Color,
    val border: Color,
    val textSecondary: Color,
    val textHelper: Color,
    val highlightBg: Color,
    val highlightText: Color
)

val LocalAppColors = staticCompositionLocalOf {
    AppThemeColors(
        bgStart = Color(0xFFFDFBFF),
        bgEnd = Color(0xFFF3F4F9),
        surface = Color(0xFFFFFFFF),
        primary = Color(0xFF005AC1),
        accent = Color(0xFF001A41),
        textPrimary = Color(0xFF1B1B1F),
        border = Color(0xFFDDE1EB),
        textSecondary = Color(0xFF44474E),
        textHelper = Color(0xFF74777F),
        highlightBg = Color(0xFFD8E2FF),
        highlightText = Color(0xFF001A41)
    )
}

// Composition-local proxies that transparently fetch active values from current provider
private val DarkBackgroundStart: Color @Composable get() = LocalAppColors.current.bgStart
private val DarkBackgroundEnd: Color @Composable get() = LocalAppColors.current.bgEnd
private val SurfaceCardColor: Color @Composable get() = LocalAppColors.current.surface
private val TealPrimary: Color @Composable get() = LocalAppColors.current.primary
private val TealAccent: Color @Composable get() = LocalAppColors.current.accent
private val TextPrimary: Color @Composable get() = LocalAppColors.current.textPrimary
private val BorderColor: Color @Composable get() = LocalAppColors.current.border
private val TextSecondary: Color @Composable get() = LocalAppColors.current.textSecondary
private val TextHelper: Color @Composable get() = LocalAppColors.current.textHelper
private val HighlightAccentBg: Color @Composable get() = LocalAppColors.current.highlightBg
private val HighlightAccentText: Color @Composable get() = LocalAppColors.current.highlightText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: ServiceViewModel) {
    val context = LocalContext.current
    val services by viewModel.filteredServices.collectAsStateWithLifecycle()
    val activeOutput = viewModel.activeOutput

    // User-controlled dark mode persistence & menu toggle
    var isDarkMode by rememberSaveable { mutableStateOf(false) }
    var showInfoDialog by rememberSaveable { mutableStateOf(false) }

    // Dynamic colors configuration
    val colors = if (isDarkMode) {
        AppThemeColors(
            bgStart = Color(0xFF0F172A),      // Slate 900 Dark pleasant gradient start
            bgEnd = Color(0xFF090D16),        // Slate 950 Dark deep tone end
            surface = Color(0xFF1E293B),      // Clean dark card background (slate 800)
            primary = Color(0xFF38BDF8),      // Energetic Sky Blue for contrast text/major highlights
            accent = Color(0xFFF8FAFC),       // Crisp slate text for headings
            textPrimary = Color(0xFFF8FAFC),  // Very bright white-blue slate text
            border = Color(0xFF334155),       // Dark subtle slate outlines
            textSecondary = Color(0xFF94A3B8), // slate 400
            textHelper = Color(0xFF64748B),    // slate 500
            highlightBg = Color(0xFF1E1E38),   // Dark purple/indigo splash
            highlightText = Color(0xFFC7D2FE)  // indigo 200
        )
    } else {
        AppThemeColors(
            bgStart = Color(0xFFF3F8FF),      // Agradável, soft sky-blue light background start
            bgEnd = Color(0xFFE2EDFB),        // Agradável, light blue grey gradient end
            surface = Color(0xFFFFFFFF),
            primary = Color(0xFF005AC1),
            accent = Color(0xFF001A41),
            textPrimary = Color(0xFF1B1B1F),
            border = Color(0xFFDDE1EB),
            textSecondary = Color(0xFF44474E),
            textHelper = Color(0xFF74777F),
            highlightBg = Color(0xFFD8E2FF),
            highlightText = Color(0xFF001A41)
        )
    }

    CompositionLocalProvider(LocalAppColors provides colors) {
        // Background gradient for elite atmosphere
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DarkBackgroundStart, DarkBackgroundEnd)
                    )
                )
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Dynamic App Icon Badge to emphasize identity
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(TealPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Logo Rotina+",
                                        tint = if (isDarkMode) Color.Black else Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "ROTINA+",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 2.sp,
                                            color = TealAccent
                                        )
                                    )
                                    Text(
                                        text = "Gestão Inteligente",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = TextSecondary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        },
                        actions = {
                            // Dark Mode toggle action
                            IconButton(
                                onClick = { isDarkMode = !isDarkMode },
                                modifier = Modifier.testTag("dark_mode_toggle")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = if (isDarkMode) "Mudar para modo claro" else "Mudar para modo noturno",
                                    tint = if (isDarkMode) Color(0xFFFFD700) else TealPrimary
                                )
                            }
                            // Info menu action
                            IconButton(
                                onClick = { showInfoDialog = true },
                                modifier = Modifier.testTag("info_menu_toggle")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Ajuda e Informações",
                                    tint = TealPrimary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
            ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Header & IA Input Section
                item {
                    IaCommandCard(viewModel = viewModel)
                }

                // Interactive Manual Form Fields
                item {
                    ManualFormCard(viewModel = viewModel)
                }

                // Search & Filter Interface
                item {
                    SearchSection(viewModel = viewModel)
                }

                // History Records
                if (services.isEmpty()) {
                    item {
                        EmptyStateCard()
                    }
                } else {
                    items(services, key = { it.id }) { service ->
                        ServiceItemRow(
                            service = service,
                            onClick = {
                                viewModel.selectRecordForOutput(service, isNew = false)
                            },
                            onDelete = {
                                viewModel.deleteRecord(service)
                                Toast.makeText(context, "Serviço deletado", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }

        // Show Payload detail Dialog when a service is registered or selected
        if (activeOutput != null) {
            IntegrationPayloadDialog(
                state = activeOutput,
                onDismiss = { viewModel.activeOutput = null }
            )
        }

        // Show Help & Information Dialog to keep the UI clean
        if (showInfoDialog) {
            AlertDialog(
                onDismissRequest = { showInfoDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Ajuda",
                            tint = TealPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Sobre o Rotina+",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TealAccent
                            )
                        )
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "O Rotina+ é o seu assistente de gestão inteligente definitivo, projetado para simplificar e automatizar o seu dia a dia.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                        )
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(BorderColor.copy(alpha = 0.5f))
                        )
                        
                        Text(
                            text = "Como utilizar o comando de IA:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TealAccent)
                        )
                        Text(
                            text = "• Digite em linguagem natural o que foi feito. Exemplo:\n\"Corte e barba para o cliente Marcos por R$ 60, com fone, salvar contato e marcar na agenda amanhã às 14h.\"\n• O assistente processará o texto e gerará o payload estruturado instantaneamente pronto para integração.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 18.sp)
                        )
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(BorderColor.copy(alpha = 0.5f))
                        )
                        
                        Text(
                            text = "Recursos Disponíveis:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = TealAccent)
                        )
                        Text(
                            text = "✔ Cadastro via Comando de Texto/Voz com IA\n✔ Criação manual expressa de registros\n✔ Busca rápida por cliente ou data\n✔ Exportação de dados estruturados em JSON\n✔ Integração facilitada com Planilhas, Calendários e Contatos",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 18.sp)
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showInfoDialog = false }
                    ) {
                        Text("Entendi", color = TealPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = SurfaceCardColor,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}
}

@Composable
fun IaCommandCard(viewModel: ServiceViewModel) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = SurfaceCardColor),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "IA",
                    tint = TealPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Cadastro Rápido por IA",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TealAccent
                    )
                )
            }

            Text(
                text = "Digite ou fale o relato do serviço em linguagem natural para que o Rotina+ preencha todos os campos automaticamente.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = TextSecondary
                )
            )

            OutlinedTextField(
                value = viewModel.naturalLanguageInput,
                onValueChange = { viewModel.naturalLanguageInput = it },
                placeholder = {
                    Text(
                        text = "Ex: Consertei o chuveiro do Marcos de tarde por R$180, fone (11) 99999-8888, salvar contato.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextHelper)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("ai_input_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = TealPrimary,
                    unfocusedBorderColor = BorderColor,
                    focusedContainerColor = DarkBackgroundEnd,
                    unfocusedContainerColor = DarkBackgroundEnd.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = { viewModel.processTextWithAI() },
                enabled = !viewModel.isAiLoading && viewModel.naturalLanguageInput.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("ai_process_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealPrimary,
                    contentColor = Color.White,
                    disabledContainerColor = TealPrimary.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (viewModel.isAiLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Processar")
                        Text(
                            text = "Extrair Dados com IA",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            // Error / Success feedback
            viewModel.aiError?.let { err ->
                Text(
                    text = err,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            viewModel.aiSuccessState?.let { success ->
                Text(
                    text = success,
                    color = TealPrimary,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun ManualFormCard(viewModel: ServiceViewModel) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = SurfaceCardColor),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Formulário",
                    tint = TealPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Campos do Serviço",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TealAccent
                    )
                )
            }

            // Nome do Cliente
            OutlinedTextField(
                value = viewModel.clientNameInput,
                onValueChange = { 
                    viewModel.clientNameInput = it
                    viewModel.triggerFilterUpdate()
                },
                label = { Text("Nome do Cliente *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_client_name"),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Person, "Cliente", tint = TealPrimary) },
                colors = textFieldColors()
            )

            // Serviço Executado
            OutlinedTextField(
                value = viewModel.serviceInput,
                onValueChange = { 
                    viewModel.serviceInput = it
                    viewModel.triggerFilterUpdate()
                },
                label = { Text("Serviço Executado *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_service_executed"),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Info, "Serviço", tint = TealPrimary) },
                colors = textFieldColors()
            )

            // Valor Cobrado (Moeda R$)
            OutlinedTextField(
                value = viewModel.amountInput,
                onValueChange = { 
                    viewModel.amountInput = it
                    viewModel.triggerFilterUpdate()
                },
                label = { Text("Valor Cobrado (R$)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_amount_charged"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Icon(Icons.Default.Star, "Preço", tint = TealPrimary) },
                colors = textFieldColors()
            )

            // Data/Hora
            OutlinedTextField(
                value = viewModel.dateTimeInput,
                onValueChange = { 
                    viewModel.dateTimeInput = it
                    viewModel.triggerFilterUpdate()
                },
                label = { Text("Data e Hora (dd/mm/aaaa hh:mm)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_date_time"),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Home, "Data", tint = TealPrimary) },
                colors = textFieldColors()
            )

            // Telefone
            OutlinedTextField(
                value = viewModel.phoneInput,
                onValueChange = { 
                    viewModel.phoneInput = it
                    viewModel.triggerFilterUpdate()
                },
                label = { Text("Telefone / WhatsApp") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_phone"),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                leadingIcon = { Icon(Icons.Default.Phone, "Telefone", tint = TealPrimary) },
                colors = textFieldColors()
            )

            // Endereço
            OutlinedTextField(
                value = viewModel.addressInput,
                onValueChange = { 
                    viewModel.addressInput = it
                    viewModel.triggerFilterUpdate()
                },
                label = { Text("Endereço Completo do Local") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_address"),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.LocationOn, "Endereço", tint = TealPrimary) },
                colors = textFieldColors()
            )

            // Salvar cliente como contato (Switch)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Contatos",
                        tint = TextSecondary
                    )
                    Column {
                        Text(
                            text = "Importar no Google Contatos",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Gera payload para criar contato automático",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextHelper)
                        )
                    }
                }
                Switch(
                    checked = viewModel.saveAsContactInput,
                    onCheckedChange = { viewModel.saveAsContactInput = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = TealPrimary,
                        uncheckedThumbColor = Color.LightGray,
                        uncheckedTrackColor = BorderColor
                    )
                )
            }

            // Save service button
            Button(
                onClick = { viewModel.submitService() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("form_submit_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TealPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar")
                    Text(
                        text = "Cadastrar Serviço no histórico",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SearchSection(viewModel: ServiceViewModel) {
    Column(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Histórico de Serviços",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = viewModel.searchClientQuery,
                onValueChange = {
                    viewModel.searchClientQuery = it
                    viewModel.triggerFilterUpdate()
                },
                placeholder = { Text("Filtrar por Cliente...", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_client_input"),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, "Buscar", tint = TealPrimary) },
                colors = searchFieldColors()
            )

            OutlinedTextField(
                value = viewModel.searchDateQuery,
                onValueChange = {
                    viewModel.searchDateQuery = it
                    viewModel.triggerFilterUpdate()
                },
                placeholder = { Text("Data ou Mês...", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_date_input"),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Home, "Calendário", tint = TealPrimary) },
                colors = searchFieldColors()
            )
        }
    }
}

@Composable
fun ServiceItemRow(
    service: ServiceRecord,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.outlinedCardColors(containerColor = SurfaceCardColor),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = service.clientName,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    if (service.saveAsContact) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFEADDFF)) // Soft Lavender badge color
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Salvar Contato",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF21005D) // Deep purple text
                                )
                            )
                        }
                    }
                }

                Text(
                    text = service.serviceExecuted,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Calendário",
                            tint = TextHelper,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = service.formattedDateTime,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextHelper
                            )
                        )
                    }

                    if (service.phone.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Phone,
                                contentDescription = "Fone",
                                tint = TextHelper,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = service.phone,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TextHelper
                                )
                            )
                        }
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "R$ %.2f".format(Locale.getDefault(), service.amountCharged),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = TealPrimary
                    )
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { onClick() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Copiar integrações",
                            tint = TealPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { onDelete() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Deletar registro",
                            tint = Color(0xFFBA1A1A),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard() {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = SurfaceCardColor),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = "Vazio",
                tint = TextHelper,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = "Nenhum serviço registrado",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = "Digite seu relato em linguagem natural na caixa de IA ou preencha o formulário acima para inserir no histórico.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun IntegrationPayloadDialog(
    state: ActiveOutputState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 -> Sheets, 1 -> Agenda, 2 -> Contatos

    Dialog(onDismissRequest = onDismiss) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .heightIn(max = 550.dp),
            colors = CardDefaults.outlinedCardColors(containerColor = SurfaceCardColor),
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxSize()
            ) {
                // Topic: Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = TealPrimary
                        )
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 1: Confirmação Visual
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = HighlightAccentBg),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "1. CONFIRMAÇÃO VISUAL",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TealAccent
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Serviço para o cliente ${state.serviceRecord.clientName} de \"${state.serviceRecord.serviceExecuted}\" registrado por R$ ${String.format(Locale.getDefault(), "%.2f", state.serviceRecord.amountCharged)} em ${state.serviceRecord.formattedDateTime}.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = HighlightAccentText,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 2: Estrutura de Dados (JSON / APIs)
                Text(
                    text = "2. ESTRUTURA DE DADOS (Payload JSON)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TealPrimary
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Custom Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf("Sheets", "Agenda/Calendar", "Contatos/People")
                    tabs.forEachIndexed { index, title ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedTab == index) TealPrimary else DarkBackgroundEnd)
                                .clickable { selectedTab = index }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTab == index) Color.White else TextSecondary
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Selected JSON Payload Viewer
                val activePayload = when (selectedTab) {
                    0 -> state.sheetsJson
                    1 -> state.agendaJson
                    else -> state.contactsJson
                }

                val testTagButton = when (selectedTab) {
                    0 -> "copy_sheets_json_button"
                    1 -> "copy_agenda_json_button"
                    else -> "copy_contacts_json_button"
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A)) // dark themed terminal for JSON code highlight readability
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "JSON pronto para integração:",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.LightGray)
                            )
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Payload Integrador", activePayload)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Payload copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag(testTagButton)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Copiar", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyColumn(modifier = Modifier.weight(1f)) {
                            item {
                                Text(
                                    text = activePayload,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        color = Color(0xFF4ADE80) // nice soft green syntax color
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section 3: Próximo Passo
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "3. PRÓXIMO PASSO 🚀",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val nextStepMessage = when (selectedTab) {
                            0 -> "Copie este payload das linhas do Sheets para alimentar automações via Apps Script, n8n ou webhooks."
                            1 -> "Submeta esta estrutura de dados via REST API do Google Calendar para criar o compromisso na agenda do profissional."
                            else -> "Envie este objeto de contato formatado para a People API do Google para salvar o cliente de forma duradoura."
                        }
                        Text(
                            text = nextStepMessage,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = TealPrimary,
    unfocusedBorderColor = BorderColor,
    focusedLabelColor = TealPrimary,
    unfocusedLabelColor = TextSecondary,
    focusedContainerColor = DarkBackgroundEnd,
    unfocusedContainerColor = DarkBackgroundEnd.copy(alpha = 0.4f)
)

@Composable
fun searchFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = TealPrimary,
    unfocusedBorderColor = BorderColor,
    focusedContainerColor = SurfaceCardColor,
    unfocusedContainerColor = SurfaceCardColor.copy(alpha = 0.6f)
)
