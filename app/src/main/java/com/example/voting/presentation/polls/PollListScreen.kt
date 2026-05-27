package com.example.voting.presentation.polls
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.isSystemInDarkTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollListScreen(
    viewModel: PollListViewModel,
    onCreateClick: () -> Unit,
    onVoteClick: (Int) -> Unit,
    onResultsClick: (Int) -> Unit,
    onEditClick: (Int) -> Unit,
    onToggleTheme: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isFocused by remember { mutableStateOf(false) }
    val isDarkTheme = isSystemInDarkTheme()
    val currentUserEmail = uiState.currentUserEmail
    val coroutineScope = rememberCoroutineScope()
    var isSearching by remember { mutableStateOf(false) }

    val showSearchHistory = isFocused && uiState.searchQuery.isBlank() && uiState.searchHistory.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Голосования") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Выйти")
                    }
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkTheme) "Светлая тема" else "Тёмная тема"
                        )
                    }
                    IconButton(onClick = { viewModel.toggleShowOnlyMyPolls() }) {
                        Icon(
                            if (uiState.showOnlyMyPolls) Icons.Default.Person else Icons.Default.People,
                            contentDescription = if (uiState.showOnlyMyPolls) "Мои" else "Все"
                        )
                    }
                    IconButton(onClick = onCreateClick) {
                        Icon(Icons.Default.Add, contentDescription = "Создать")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { newValue ->
                    viewModel.updateSearchQuery(newValue)
                    isSearching = true
                    coroutineScope.launch {
                        delay(500)
                        isSearching = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                    },
                placeholder = { Text("Поиск голосований...") },
                leadingIcon = null,
                trailingIcon = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (uiState.searchQuery.isNotBlank()) {
                            IconButton(onClick = {
                                viewModel.clearSearch()
                                isSearching = true
                                coroutineScope.launch {
                                    delay(300)
                                    isSearching = false
                                }
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Очистить")
                            }
                        }
                        IconButton(onClick = {
                            if (uiState.searchQuery.isNotBlank()) {
                                viewModel.addToSearchHistory(uiState.searchQuery)
                            }
                        }) {
                            Icon(Icons.Default.Search, contentDescription = "Найти")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        if (uiState.searchQuery.isNotBlank()) {
                            viewModel.addToSearchHistory(uiState.searchQuery)
                        }
                    }
                )
            )

            if (showSearchHistory) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Недавние поиски", style = MaterialTheme.typography.labelLarge)
                        TextButton(onClick = { viewModel.clearSearchHistory() }) {
                            Text("Очистить историю")
                        }
                    }

                    uiState.searchHistory.take(10).forEach { historyItem ->
                        TextButton(
                            onClick = {
                                viewModel.updateSearchQuery(historyItem)
                                viewModel.addToSearchHistory(historyItem)
                                isFocused = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Icon(Icons.Default.History, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(historyItem)
                            }
                        }
                    }
                }
            }

            when {
                isSearching || uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Поиск...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(uiState.error!!, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.loadPolls() }) {
                                Text("Обновить")
                            }
                        }
                    }
                }
                viewModel.filteredPolls.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Нет голосований", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            if (uiState.searchQuery.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("По запросу \"${uiState.searchQuery}\" ничего не найдено", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(viewModel.filteredPolls) { poll ->
                            val isOwner = currentUserEmail != null && poll.createdBy != null &&
                                    poll.createdBy.equals(currentUserEmail, ignoreCase = true)

                            PollCard(
                                poll = poll,
                                isOwner = isOwner,
                                onVoteClick = { onVoteClick(poll.id) },
                                onResultsClick = { onResultsClick(poll.id) },
                                onDeleteClick = { viewModel.deletePoll(poll.id) },
                                onEditClick = { onEditClick(poll.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PollCard(
    poll: com.example.voting.domain.model.Poll,
    isOwner: Boolean,
    onVoteClick: () -> Unit,
    onResultsClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = poll.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                if (isOwner) {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Меню")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Редактировать") },
                            onClick = {
                                showMenu = false
                                onEditClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Удалить") },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = poll.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onVoteClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Голосовать")
                }
                Button(
                    onClick = onResultsClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Результаты")
                }
            }
        }
    }
}