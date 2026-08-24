package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val imageUri: String,
    val filterPresetId: String? = null,
    val filterStrength: Float = 1.0f,
    val exposure: Float = 0f,
    val contrast: Float = 0f,
    val highlights: Float = 0f,
    val shadows: Float = 0f,
    val saturation: Float = 0f,
    val temperature: Float = 0f,
    val tint: Float = 0f,
    val vibrance: Float = 0f,
    val sharpen: Float = 0f,
    val grain: Float = 0f,
    val vignette: Float = 0f,
    val glow: Float = 0f,
    val blur: Float = 0f,
    val lightLeak: Float = 0f,
    val dustEffect: Float = 0f,
    val hueShift: Float = 0f,
    val rotationDegrees: Float = 0f,
    val flipHorizontal: Boolean = false,
    val flipVertical: Boolean = false,
    val dateStampText: String? = null,
    val lastModified: Long = System.currentTimeMillis()
)

@Entity(tableName = "custom_presets")
data class CustomPresetEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val exposure: Float = 0f,
    val contrast: Float = 0f,
    val highlights: Float = 0f,
    val shadows: Float = 0f,
    val saturation: Float = 0f,
    val temperature: Float = 0f,
    val tint: Float = 0f,
    val vibrance: Float = 0f,
    val grain: Float = 0f,
    val vignette: Float = 0f,
    val glow: Float = 0f,
    val lightLeak: Float = 0f,
    val dustEffect: Float = 0f,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY lastModified DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: Long): ProjectEntity?

    @Query("SELECT * FROM projects ORDER BY lastModified DESC LIMIT 1")
    suspend fun getLatestDraft(): ProjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(project: ProjectEntity): Long

    @Delete
    suspend fun delete(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface CustomPresetDao {
    @Query("SELECT * FROM custom_presets ORDER BY createdAt DESC")
    fun getAllCustomPresets(): Flow<List<CustomPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(preset: CustomPresetEntity)

    @Delete
    suspend fun delete(preset: CustomPresetEntity)

    @Query("UPDATE custom_presets SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavorite(id: String, isFav: Boolean)
}

@Database(
    entities = [ProjectEntity::class, CustomPresetEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun customPresetDao(): CustomPresetDao
}
