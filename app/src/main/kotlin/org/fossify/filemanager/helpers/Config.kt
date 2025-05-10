package org.fossify.filemanager.helpers

import android.content.Context
import android.content.res.Configuration
import org.fossify.commons.extensions.getInternalStoragePath
import org.fossify.commons.helpers.BaseConfig
import org.fossify.commons.helpers.SORT_DESCENDING
import java.io.File
import java.util.Locale

class Config(context: Context) : BaseConfig(context) {
    companion object {
        fun newInstance(context: Context) = Config(context)
    }

    var showHidden: Boolean
        get() = prefs.getBoolean(SHOW_HIDDEN, false)
        set(show) = prefs.edit().putBoolean(SHOW_HIDDEN, show).apply()

    var temporarilyShowHidden: Boolean
        get() = prefs.getBoolean(TEMPORARILY_SHOW_HIDDEN, false)
        set(temporarilyShowHidden) = prefs.edit().putBoolean(TEMPORARILY_SHOW_HIDDEN, temporarilyShowHidden).apply()

    fun shouldShowHidden() = showHidden || temporarilyShowHidden

    var pressBackTwice: Boolean
        get() = prefs.getBoolean(PRESS_BACK_TWICE, true)
        set(pressBackTwice) = prefs.edit().putBoolean(PRESS_BACK_TWICE, pressBackTwice).apply()

    var homeFolder: String
        get(): String {
            var path = prefs.getString(HOME_FOLDER, "")!!
            if (path.isEmpty() || !File(path).isDirectory) {
                path = context.getInternalStoragePath()
                homeFolder = path
            }
            return path
        }
        set(homeFolder) = prefs.edit().putString(HOME_FOLDER, homeFolder).apply()

    fun addFavorite(path: String) {
        val currFavorites = HashSet<String>(favorites)
        currFavorites.add(path)
        favorites = currFavorites
    }

    fun moveFavorite(oldPath: String, newPath: String) {
        if (!favorites.contains(oldPath)) {
            return
        }

        val currFavorites = HashSet<String>(favorites)
        currFavorites.remove(oldPath)
        currFavorites.add(newPath)
        favorites = currFavorites
    }

    fun removeFavorite(path: String) {
        if (!favorites.contains(path)) {
            return
        }

        val currFavorites = HashSet<String>(favorites)
        currFavorites.remove(path)
        favorites = currFavorites
    }

    var isRootAvailable: Boolean
        get() = prefs.getBoolean(IS_ROOT_AVAILABLE, false)
        set(isRootAvailable) = prefs.edit().putBoolean(IS_ROOT_AVAILABLE, isRootAvailable).apply()

    var enableRootAccess: Boolean
        get() = prefs.getBoolean(ENABLE_ROOT_ACCESS, false)
        set(enableRootAccess) = prefs.edit().putBoolean(ENABLE_ROOT_ACCESS, enableRootAccess).apply()

    var editorTextZoom: Float
        get() = prefs.getFloat(EDITOR_TEXT_ZOOM, 1.2f)
        set(editorTextZoom) = prefs.edit().putFloat(EDITOR_TEXT_ZOOM, editorTextZoom).apply()

    fun saveFolderViewType(path: String, value: Int) {
        if (path.isEmpty()) {
            viewType = value
        } else {
            prefs.edit().putInt(VIEW_TYPE_PREFIX + path.lowercase(Locale.getDefault()), value).apply()
        }
    }

    fun getFolderViewType(path: String) = prefs.getInt(VIEW_TYPE_PREFIX + path.lowercase(Locale.getDefault()), viewType)

    fun removeFolderViewType(path: String) {
        prefs.edit().remove(VIEW_TYPE_PREFIX + path.lowercase(Locale.getDefault())).apply()
    }

    fun hasCustomViewType(path: String) = prefs.contains(VIEW_TYPE_PREFIX + path.lowercase(Locale.getDefault()))

    var fileColumnCnt: Int
        get() = prefs.getInt(getFileColumnsField(), getDefaultFileColumnCount())
        set(fileColumnCnt) = prefs.edit().putInt(getFileColumnsField(), fileColumnCnt).apply()

    private fun getFileColumnsField(): String {
        val isPortrait = context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        return if (isPortrait) {
            FILE_COLUMN_CNT
        } else {
            FILE_LANDSCAPE_COLUMN_CNT
        }
    }

    private fun getDefaultFileColumnCount(): Int {
        val isPortrait = context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        return if (isPortrait) 4 else 8
    }

    var displayFilenames: Boolean
        get() = prefs.getBoolean(DISPLAY_FILE_NAMES, true)
        set(displayFilenames) = prefs.edit().putBoolean(DISPLAY_FILE_NAMES, displayFilenames).apply()

    var showTabs: Int
        get() = prefs.getInt(SHOW_TABS, ALL_TABS_MASK)
        set(showTabs) = prefs.edit().putInt(SHOW_TABS, showTabs).apply()

    var wasStorageAnalysisTabAdded: Boolean
        get() = prefs.getBoolean(WAS_STORAGE_ANALYSIS_TAB_ADDED, false)
        set(wasStorageAnalysisTabAdded) = prefs.edit().putBoolean(WAS_STORAGE_ANALYSIS_TAB_ADDED, wasStorageAnalysisTabAdded).apply()
        
    var enableShakeToggleSorting: Boolean
        get() = prefs.getBoolean(ENABLE_SHAKE_TOGGLE_SORTING, true)
        set(enableShakeToggleSorting) = prefs.edit().putBoolean(ENABLE_SHAKE_TOGGLE_SORTING, enableShakeToggleSorting).apply()
        
    /**
     * Lista de elementos protegidos
     */
    var protectedItems: Set<String>
        get() = prefs.getStringSet(PROTECTED_ITEMS, HashSet<String>()) ?: HashSet()
        set(protectedItems) = prefs.edit().putStringSet(PROTECTED_ITEMS, protectedItems).apply()
    
    /**
     * Tipo de protección: PROTECTION_FINGERPRINT o PROTECTION_PIN
     */
    var protectionType: Int
        get() = prefs.getInt(PROTECTION_TYPE, PROTECTION_FINGERPRINT)
        set(protectionType) = prefs.edit().putInt(PROTECTION_TYPE, protectionType).apply()
    
    /**
     * PIN para protección de elementos (solo si se usa PROTECTION_PIN)
     */
    var protectionPin: String
        get() = prefs.getString(PROTECTION_PIN, "") ?: ""
        set(protectionPin) = prefs.edit().putString(PROTECTION_PIN, protectionPin).apply()
    
    /**
     * Indica si la protección de elementos está activada globalmente
     */
    var isProtectionEnabled: Boolean
        get() = prefs.getBoolean(PROTECTION_ENABLED, true)
        set(isProtectionEnabled) = prefs.edit().putBoolean(PROTECTION_ENABLED, isProtectionEnabled).apply()
    
    /**
     * Verifica si un elemento está protegido
     */
    fun isItemProtected(path: String): Boolean {
        // Si la protección está desactivada, retorna falso sin importar si el elemento está en la lista
        if (!isProtectionEnabled) {
            return false
        }
        return protectedItems.contains(path)
    }
    
    /**
     * Añade un elemento a la lista de protegidos
     */
    fun addProtectedItem(path: String) {
        val currentProtectedItems = HashSet<String>(protectedItems)
        currentProtectedItems.add(path)
        protectedItems = currentProtectedItems
    }
    
    /**
     * Elimina un elemento de la lista de protegidos
     */
    fun removeProtectedItem(path: String) {
        val currentProtectedItems = HashSet<String>(protectedItems)
        currentProtectedItems.remove(path)
        protectedItems = currentProtectedItems
    }
    
    /**
     * Invierte el orden de clasificación (ascendente/descendente) para la ruta dada
     * @param path La ruta de la carpeta para invertir el orden
     */
    fun toggleSortOrder(path: String) {
        val currentSorting = getFolderSorting(path)
        val newSorting = if (currentSorting and SORT_DESCENDING != 0) {
            // Si es descendente, quitamos el flag SORT_DESCENDING
            currentSorting and SORT_DESCENDING.inv()
        } else {
            // Si es ascendente, añadimos el flag SORT_DESCENDING
            currentSorting or SORT_DESCENDING
        }
        
        // Guardamos el nuevo orden
        if (hasCustomSorting(path)) {
            saveCustomSorting(path, newSorting)
        } else {
            sorting = newSorting
        }
    }
}
