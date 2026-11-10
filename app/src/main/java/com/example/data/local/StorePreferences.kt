package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "smartstore_prefs")

data class StoreSettings(
    val storeName: String = "SmartStore",
    val storeAddress: String = "Main Market Road, Shop #12",
    val storePhone: String = "+91 98765 00000",
    val currencySymbol: String = "₹",
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val receiptFormat: String = "THERMAL_58MM", // "THERMAL_58MM", "A4"
    val allowNegativeStock: Boolean = false,
    val defaultLowStockThreshold: Double = 5.0,
    val receiptFooterMessage: String = "Thank you for shopping with us! Please visit again."
)

class StorePreferences(private val context: Context) {

    private object PreferencesKeys {
        val STORE_NAME = stringPreferencesKey("store_name")
        val STORE_ADDRESS = stringPreferencesKey("store_address")
        val STORE_PHONE = stringPreferencesKey("store_phone")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val RECEIPT_FORMAT = stringPreferencesKey("receipt_format")
        val ALLOW_NEGATIVE_STOCK = booleanPreferencesKey("allow_negative_stock")
        val LOW_STOCK_THRESHOLD = doublePreferencesKey("low_stock_threshold")
        val RECEIPT_FOOTER = stringPreferencesKey("receipt_footer")
    }

    val storeSettings: Flow<StoreSettings> = context.dataStore.data.map { prefs ->
        StoreSettings(
            storeName = prefs[PreferencesKeys.STORE_NAME] ?: "SmartStore",
            storeAddress = prefs[PreferencesKeys.STORE_ADDRESS] ?: "Main Market Road, Shop #12",
            storePhone = prefs[PreferencesKeys.STORE_PHONE] ?: "+91 98765 00000",
            currencySymbol = prefs[PreferencesKeys.CURRENCY_SYMBOL] ?: "₹",
            themeMode = prefs[PreferencesKeys.THEME_MODE] ?: "SYSTEM",
            receiptFormat = prefs[PreferencesKeys.RECEIPT_FORMAT] ?: "THERMAL_58MM",
            allowNegativeStock = prefs[PreferencesKeys.ALLOW_NEGATIVE_STOCK] ?: false,
            defaultLowStockThreshold = prefs[PreferencesKeys.LOW_STOCK_THRESHOLD] ?: 5.0,
            receiptFooterMessage = prefs[PreferencesKeys.RECEIPT_FOOTER] ?: "Thank you for shopping with us! Please visit again."
        )
    }

    suspend fun updateSettings(settings: StoreSettings) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.STORE_NAME] = settings.storeName
            prefs[PreferencesKeys.STORE_ADDRESS] = settings.storeAddress
            prefs[PreferencesKeys.STORE_PHONE] = settings.storePhone
            prefs[PreferencesKeys.CURRENCY_SYMBOL] = settings.currencySymbol
            prefs[PreferencesKeys.THEME_MODE] = settings.themeMode
            prefs[PreferencesKeys.RECEIPT_FORMAT] = settings.receiptFormat
            prefs[PreferencesKeys.ALLOW_NEGATIVE_STOCK] = settings.allowNegativeStock
            prefs[PreferencesKeys.LOW_STOCK_THRESHOLD] = settings.defaultLowStockThreshold
            prefs[PreferencesKeys.RECEIPT_FOOTER] = settings.receiptFooterMessage
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.THEME_MODE] = mode
        }
    }
}
