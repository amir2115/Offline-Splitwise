package com.encer.splitwise.ui.components

import android.app.Activity
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavBackStackEntry
import androidx.savedstate.SavedStateRegistryOwner
import dagger.hilt.android.internal.lifecycle.HiltViewModelFactory

@Composable
internal inline fun <reified VM : ViewModel> appHiltViewModel(): VM {
    val context = LocalContext.current
    val owner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided"
    }
    val activity = generateSequence(context) { current ->
        when (current) {
            is ContextWrapper -> current.baseContext
            else -> null
        }
    }.filterIsInstance<Activity>().firstOrNull() as? ComponentActivity
        ?: error("A ComponentActivity context is required for Hilt view models")
    val savedStateOwner = owner as? SavedStateRegistryOwner
        ?: error("ViewModelStoreOwner must also be a SavedStateRegistryOwner")
    val defaultFactoryOwner = owner as? HasDefaultViewModelProviderFactory
        ?: error("ViewModelStoreOwner must provide a default ViewModelProvider.Factory")
    val defaultArgs = (owner as? NavBackStackEntry)?.arguments
    val factory = remember(owner, activity, defaultFactoryOwner) {
        HiltViewModelFactory.createInternal(
            activity,
            savedStateOwner,
            defaultArgs,
            defaultFactoryOwner.defaultViewModelProviderFactory
        )
    }
    return viewModel(
        viewModelStoreOwner = owner,
        factory = factory
    )
}
