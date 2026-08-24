package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.CustomPresetEntity
import com.example.data.DefaultPresets
import com.example.data.ProjectEntity
import com.example.model.AdjustmentValues
import com.example.model.FilterCategory
import com.example.model.FilterPreset
import com.example.model.PastelTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "venusly_db"
    ).fallbackToDestructiveMigration().build()

    private val projectDao = db.projectDao()
    private val customPresetDao = db.customPresetDao()

    val recentProjects: StateFlow<List<ProjectEntity>> = projectDao.getAllProjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customPresets: StateFlow<List<CustomPresetEntity>> = customPresetDao.getAllCustomPresets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategory = MutableStateFlow(FilterCategory.ALL)
    val selectedCategory: StateFlow<FilterCategory> = _selectedCategory.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _selectedTheme = MutableStateFlow(PastelTheme.PASTEL_SKY)
    val selectedTheme: StateFlow<PastelTheme> = _selectedTheme.asStateFlow()

    private val _isCompactMode = MutableStateFlow(false)
    val isCompactMode: StateFlow<Boolean> = _isCompactMode.asStateFlow()

    private val _userProfile = MutableStateFlow(com.example.model.UserProfile())
    val userProfile: StateFlow<com.example.model.UserProfile> = _userProfile.asStateFlow()

    private val _favoritePresetIds = MutableStateFlow<Set<String>>(setOf("fuji_400", "retro_80s"))
    val favoritePresetIds: StateFlow<Set<String>> = _favoritePresetIds.asStateFlow()

    fun selectCategory(category: FilterCategory) {
        _selectedCategory.value = category
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun selectTheme(theme: PastelTheme) {
        _selectedTheme.value = theme
    }

    fun toggleCompactMode() {
        _isCompactMode.value = !_isCompactMode.value
    }

    fun setCompactMode(enabled: Boolean) {
        _isCompactMode.value = enabled
    }

    fun updateUserProfile(profile: com.example.model.UserProfile) {
        _userProfile.value = profile
    }

    fun updateAvatarUri(uri: String?) {
        _userProfile.value = _userProfile.value.copy(avatarUri = uri)
    }

    fun deleteCustomPreset(preset: CustomPresetEntity) {
        viewModelScope.launch {
            customPresetDao.delete(preset)
        }
    }

    fun toggleFavorite(presetId: String) {
        val current = _favoritePresetIds.value.toMutableSet()
        if (current.contains(presetId)) {
            current.remove(presetId)
        } else {
            current.add(presetId)
        }
        _favoritePresetIds.value = current
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            projectDao.delete(project)
        }
    }

    fun saveCustomPreset(name: String, category: String, description: String, adjustments: AdjustmentValues) {
        viewModelScope.launch {
            val entity = CustomPresetEntity(
                id = "custom_${System.currentTimeMillis()}",
                name = name,
                category = category,
                description = description,
                exposure = adjustments.exposure,
                contrast = adjustments.contrast,
                highlights = adjustments.highlights,
                shadows = adjustments.shadows,
                saturation = adjustments.saturation,
                temperature = adjustments.temperature,
                tint = adjustments.tint,
                vibrance = adjustments.vibrance,
                grain = adjustments.grain,
                vignette = adjustments.vignette,
                glow = adjustments.glow,
                lightLeak = adjustments.lightLeak,
                dustEffect = adjustments.dustEffect,
                createdAt = System.currentTimeMillis()
            )
            customPresetDao.insertOrUpdate(entity)
        }
    }
}
