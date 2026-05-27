package com.example.voting.presentation.polls
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPollScreen(
    pollId: Int,
    viewModel: PollListViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var options by remember { mutableStateOf(listOf<String>()) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(pollId) {
        val poll = uiState.polls.find { it.id == pollId }
        if (poll != null) {
            title = poll.title
            description = poll.description
            options = poll.options.map { it.text }
            isLoading = false
        } else {
            val result = viewModel.getPollById(pollId)
            result.onSuccess { poll ->
                title = poll.title
                description = poll.description
                options = poll.options.map { it.text }
                isLoading = false
            }.onFailure {
                error = it.message
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Редактировать голосование") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Название") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Описание") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }

                item {
                    Text("Варианты ответов:", style = MaterialTheme.typography.titleMedium)
                }

                itemsIndexed(options) { index, option ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = option,
                            onValueChange = { newValue ->
                                options = options.toMutableList().apply { set(index, newValue) }
                            },
                            label = { Text("Вариант ${index + 1}") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        if (options.size > 2) {
                            IconButton(
                                onClick = {
                                    options = options.toMutableList().apply { removeAt(index) }
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Удалить")
                            }
                        }
                    }
                }

                item {
                    Button(
                        onClick = {
                            options = options + ""
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Добавить вариант")
                    }
                }

                item {
                    Button(
                        onClick = {
                            val nonEmptyOptions = options.filter { it.isNotBlank() }
                            if (nonEmptyOptions.size >= 2 && title.isNotBlank()) {
                                coroutineScope.launch {
                                    val result = viewModel.updatePoll(pollId, title, description, nonEmptyOptions)
                                    result.onSuccess {
                                        viewModel.loadPolls()
                                        onBack()
                                    }.onFailure { exception ->
                                        error = exception.message
                                    }
                                }
                            } else if (title.isBlank()) {
                                error = "Название не может быть пустым"
                            } else {
                                error = "Нужно минимум 2 варианта ответа"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = title.isNotBlank() && options.count { it.isNotBlank() } >= 2
                    ) {
                        Text("Сохранить изменения")
                    }
                }

                if (error != null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = error!!,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}