package com.berat.socialhabitapp.data.repository

import android.util.Log
import com.berat.socialhabitapp.BuildConfig
import com.berat.socialhabitapp.domain.model.AuthResult
import com.berat.socialhabitapp.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.IOException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val supabaseClient: SupabaseClient
) : AuthRepository {

    override suspend fun register(
        email: String,
        password: String,
        username: String,
        displayName: String,
        timezone: String
    ): AuthResult {
        if (BuildConfig.SUPABASE_URL.isBlank() || BuildConfig.SUPABASE_PUBLISHABLE_KEY.isBlank()) {
            Log.e("AuthRepositoryImpl", "SUPABASE_URL or SUPABASE_PUBLISHABLE_KEY is empty in BuildConfig!")
            return AuthResult.Failure.Unknown
        }

        return try {
            Log.d("AuthRepositoryImpl", "Starting registration for email: $email, username: $username, timezone: $timezone")
            val user = supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                this.data = buildJsonObject {
                    put("username", username)
                    put("display_name", displayName)
                    put("timezone", timezone)
                }
            }

            Log.d("AuthRepositoryImpl", "signUpWith succeeded. User: $user")
            if (user != null) {
                AuthResult.Success(userId = user.id, email = user.email ?: email)
            } else {
                val currentSessionUser = supabaseClient.auth.currentUserOrNull()
                if (currentSessionUser != null) {
                    AuthResult.Success(userId = currentSessionUser.id, email = currentSessionUser.email ?: email)
                } else {
                    AuthResult.Success(userId = "", email = email)
                }
            }
        } catch (e: RestException) {
            Log.e("AuthRepositoryImpl", "RestException during signup: status=${e.statusCode}, error=${e.error}, desc=${e.description}", e)
            val errorBody = e.error.lowercase()
            val description = e.description?.lowercase() ?: ""
            if (errorBody.contains("already registered") || errorBody.contains("already exists") ||
                errorBody.contains("user_already_exists") || description.contains("already registered") ||
                description.contains("unique constraint") || description.contains("profiles_username_key") ||
                description.contains("username is already taken")
            ) {
                AuthResult.Failure.UserAlreadyExists
            } else if (e.statusCode in 500..599) {
                AuthResult.Failure.ServerError
            } else {
                AuthResult.Failure.Unknown
            }
        } catch (e: HttpRequestException) {
            Log.e("AuthRepositoryImpl", "HttpRequestException during signup", e)
            AuthResult.Failure.NetworkError
        } catch (e: HttpRequestTimeoutException) {
            Log.e("AuthRepositoryImpl", "HttpRequestTimeoutException during signup", e)
            AuthResult.Failure.NetworkError
        } catch (e: IOException) {
            Log.e("AuthRepositoryImpl", "IOException during signup", e)
            AuthResult.Failure.NetworkError
        } catch (e: Exception) {
            Log.e("AuthRepositoryImpl", "Unexpected exception during signup", e)
            AuthResult.Failure.Unknown
        }
    }
}

