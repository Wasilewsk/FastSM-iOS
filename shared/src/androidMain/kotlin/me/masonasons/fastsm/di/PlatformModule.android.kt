package me.masonasons.fastsm.di

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

actual fun platformHttpClientEngine() = OkHttp.create()
