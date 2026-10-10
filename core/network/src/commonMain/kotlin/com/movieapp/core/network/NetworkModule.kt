package com.movieapp.core.network

import com.movieapp.core.common.Constants
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.koin.core.module.Module
import org.koin.dsl.module
import co.touchlab.kermit.Logger as KermitLogger
import io.ktor.client.plugins.logging.Logger as KtorLogger

data class NetworkConfig(val apiKey: String, val baseUrl: String, val debugLogging: Boolean)

@OptIn(ExperimentalSerializationApi::class)
val networkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = true
            namingStrategy = JsonNamingStrategy.SnakeCase
        }
    }
    single { createTmdbHttpClient(engine = get(), json = get(), config = get()) }
    single { TmdbApiService(get()) }
    single { TmdbWatchProviderApi(get()) }
    includes(networkPlatformModule)
}

internal expect val networkPlatformModule: Module

fun createTmdbHttpClient(engine: HttpClientEngine, json: Json, config: NetworkConfig): HttpClient =
    HttpClient(engine) {
        expectSuccess = true
        install(ContentNegotiation) { json(json) }
        install(HttpTimeout) {
            val millis = Constants.Api.TIMEOUT_SECONDS * 1_000
            connectTimeoutMillis = millis
            requestTimeoutMillis = millis
            socketTimeoutMillis = millis
        }
        if (config.debugLogging) {
            install(Logging) {
                level = LogLevel.BODY
                logger = object : KtorLogger {
                    override fun log(message: String) = KermitLogger.withTag("Http").d { message }
                }
                sanitizeHeader { it == HttpHeaders.Authorization }
            }
        }
        defaultRequest {
            url(config.baseUrl)
            header(HttpHeaders.Authorization, Constants.Api.API_KEY_PREFIX + config.apiKey)
        }
    }
