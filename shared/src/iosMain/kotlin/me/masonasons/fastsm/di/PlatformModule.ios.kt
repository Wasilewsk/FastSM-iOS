package me.masonasons.fastsm.di

import io.ktor.client.engine.darwin.Darwin

actual fun platformHttpClientEngine() = Darwin.create()
