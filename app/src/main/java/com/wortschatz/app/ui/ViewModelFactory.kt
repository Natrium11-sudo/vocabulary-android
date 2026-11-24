package com.wortschatz.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.wortschatz.app.AppContainer
import com.wortschatz.app.WortschatzApplication

/**
 * Small factory helper; avoids a DI framework while keeping ViewModels constructor-injected.
 */
inline fun <reified VM : ViewModel> appViewModelFactory(
    crossinline create: (AppContainer) -> VM,
): ViewModelProvider.Factory = viewModelFactory {
    initializer {
        val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
            as WortschatzApplication
        create(application.container)
    }
}
