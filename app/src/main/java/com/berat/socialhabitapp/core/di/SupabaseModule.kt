package com.berat.socialhabitapp.core.di

import android.util.Log
import com.berat.socialhabitapp.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        Log.d(
            "SupabaseModule",
            "Supabase URL: '${BuildConfig.SUPABASE_URL}', Key length: ${BuildConfig.SUPABASE_PUBLISHABLE_KEY.length}"
        )
        return createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
        ) {
            install(Auth)
        }
    }
}

