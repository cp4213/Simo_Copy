package co.gov.cnsc.mobile.simo.storage

import android.content.SearchRecentSuggestionsProvider

/**
 * Provider para poder obtener las sugerencias de los campos autocompletables
 */
class SuggestionsProvider : SearchRecentSuggestionsProvider() {

    companion object {
        const val AUTHORITY = "co.gov.cnsc.mobile.simo.storage.SuggestionsProvider"
        const val MODE = DATABASE_MODE_QUERIES
    }


    init {
        setupSuggestions(AUTHORITY, MODE)
    }
}