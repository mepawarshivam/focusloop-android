package com.focusloop.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusloop.app.data.repository.UserDataRepository
import com.focusloop.app.domain.model.HobbyTags
import com.focusloop.app.domain.model.KnownDistractingApps
import com.focusloop.app.domain.model.UserSettings
import com.focusloop.app.ui.components.AppSelectionCard
import com.focusloop.app.ui.components.HobbySelectionCard
import compose.icons.FeatherIcons
import compose.icons.feathericons.ArrowLeft
import compose.icons.feathericons.Plus
import compose.icons.feathericons.Star
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EditHobbiesViewModel(private val settingsDataStore: UserDataRepository) : ViewModel() {
    val settings: StateFlow<UserSettings> = settingsDataStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    fun toggleHobby(hobby: String, current: Set<String>) {
        val updated = if (hobby in current) current - hobby else current + hobby
        viewModelScope.launch { settingsDataStore.setHobbies(updated) }
    }
}

class EditAppsViewModel(private val settingsDataStore: UserDataRepository) : ViewModel() {
    val settings: StateFlow<UserSettings> = settingsDataStore.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserSettings())

    fun togglePackage(packageName: String, current: UserSettings) {
        val updatedPackages = if (packageName in current.monitoredPackages) {
            current.monitoredPackages - packageName
        } else {
            current.monitoredPackages + packageName
        }
        viewModelScope.launch { settingsDataStore.updateSettings(current.copy(monitoredPackages = updatedPackages)) }
    }
}

@Composable
fun EditHobbiesScreen(viewModel: EditHobbiesViewModel, onBack: () -> Unit) {
    val settings by viewModel.settings.collectAsState()
    var customHobby by remember { mutableStateOf("") }
    val predefinedNames = remember { HobbyTags.all.map { it.first }.toSet() }
    val customHobbies = settings.hobbies.filterNot { it in predefinedNames }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(FeatherIcons.ArrowLeft, contentDescription = "Back")
            }
            Text("Hobbies & Interests", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Text(
            "Used to personalize your reset breaks. Changes save automatically.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(HobbyTags.all) { (hobby, icon) ->
                HobbySelectionCard(
                    hobby = hobby,
                    icon = icon,
                    isSelected = hobby in settings.hobbies,
                    onToggle = { viewModel.toggleHobby(hobby, settings.hobbies) }
                )
            }

            items(customHobbies) { hobby ->
                HobbySelectionCard(
                    hobby = hobby,
                    icon = FeatherIcons.Star,
                    isSelected = true,
                    onToggle = { viewModel.toggleHobby(hobby, settings.hobbies) }
                )
            }

            item {
                OutlinedTextField(
                    value = customHobby,
                    onValueChange = { customHobby = it },
                    label = { Text("Add your own") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (customHobby.isNotBlank()) {
                            viewModel.toggleHobby(customHobby.trim(), settings.hobbies)
                            customHobby = ""
                        }
                    }),
                    trailingIcon = {
                        IconButton(onClick = {
                            if (customHobby.isNotBlank()) {
                                viewModel.toggleHobby(customHobby.trim(), settings.hobbies)
                                customHobby = ""
                            }
                        }) {
                            Icon(FeatherIcons.Plus, "Add")
                        }
                    }
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun EditAppsScreen(viewModel: EditAppsViewModel, onBack: () -> Unit) {
    val settings by viewModel.settings.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(FeatherIcons.ArrowLeft, contentDescription = "Back")
            }
            Text("Monitored Apps", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        Text(
            "FocusLoop watches these for signs of endless scrolling. Changes save automatically.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 24.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(KnownDistractingApps.defaults) { app ->
                AppSelectionCard(
                    app = app,
                    isSelected = app.packageName in settings.monitoredPackages,
                    onToggle = { viewModel.togglePackage(app.packageName, settings) }
                )
            }
        }
    }
}
