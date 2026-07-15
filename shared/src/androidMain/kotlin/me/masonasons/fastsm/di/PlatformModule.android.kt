package me.masonasons.fastsm.di

import io.ktor.client.engine.okhttp.OkHttp

actual fun platformHttpClientEngine() = OkHttp.create()
