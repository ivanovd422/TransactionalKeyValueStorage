package com.ivanovd422.transactionalkeyvaluestorage.main.presentation.dialog

import androidx.annotation.StringRes

sealed interface DialogMessage {
    data class Success(@StringRes val text: Int, val key: String, val value: String): DialogMessage
    data class Error(@StringRes val text: Int): DialogMessage
    data class Info(@StringRes val text: Int): DialogMessage
}