package com.berat.socialhabitapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.berat.socialhabitapp.domain.model.AuthState
import com.berat.socialhabitapp.domain.usecase.GetAuthStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getAuthStateUseCase: GetAuthStateUseCase
) : ViewModel() {

    val authState: StateFlow<AuthState> = getAuthStateUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AuthState.Initializing
        )
}
