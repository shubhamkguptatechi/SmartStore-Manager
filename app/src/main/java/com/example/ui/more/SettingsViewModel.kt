package com.example.ui.more

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BackupManager
import com.example.data.local.BackupValidationInfo
import com.example.data.local.StorePreferences
import com.example.data.local.StoreSettings
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val preferences: StorePreferences,
    private val backupManager: BackupManager
) : ViewModel() {

    val settings: StateFlow<StoreSettings> = preferences.storeSettings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StoreSettings())

    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    private val _validationInfo = MutableStateFlow<BackupValidationInfo?>(null)
    val validationInfo: StateFlow<BackupValidationInfo?> = _validationInfo.asStateFlow()

    private val _pendingRestoreJson = MutableStateFlow<String?>(null)
    val pendingRestoreJson: StateFlow<String?> = _pendingRestoreJson.asStateFlow()

    fun updateSettings(newSettings: StoreSettings) {
        viewModelScope.launch {
            preferences.updateSettings(newSettings)
            _messages.emit("Settings saved successfully")
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferences.setThemeMode(mode)
        }
    }

    fun exportBackup(uri: Uri) {
        viewModelScope.launch {
            val result = backupManager.exportToUri(uri)
            result.onSuccess {
                _messages.emit("Backup exported successfully to selected file!")
            }.onFailure {
                _messages.emit("Failed to export backup: ${it.message}")
            }
        }
    }

    fun prepareRestoreFromUri(uri: Uri) {
        viewModelScope.launch {
            val readResult = backupManager.readJsonFromUri(uri)
            readResult.onSuccess { json ->
                val validation = backupManager.validateBackupJson(json)
                validation.onSuccess { info ->
                    _validationInfo.value = info
                    _pendingRestoreJson.value = json
                }.onFailure {
                    _messages.emit("Invalid backup file: ${it.message}")
                }
            }.onFailure {
                _messages.emit("Failed to read file: ${it.message}")
            }
        }
    }

    fun confirmRestore(onSuccess: () -> Unit) {
        val json = _pendingRestoreJson.value ?: return
        viewModelScope.launch {
            val result = backupManager.restoreFromJson(json)
            result.onSuccess {
                _validationInfo.value = null
                _pendingRestoreJson.value = null
                _messages.emit("Database restored successfully!")
                onSuccess()
            }.onFailure {
                _messages.emit("Restore failed: ${it.message}")
            }
        }
    }

    fun cancelRestore() {
        _validationInfo.value = null
        _pendingRestoreJson.value = null
    }
}
