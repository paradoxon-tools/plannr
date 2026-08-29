package de.chennemann.plannr.database.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import okhttp3.Dns
import java.net.InetAddress

private const val DEFAULT_SERVER_HOST = "plannr-api.local.chennemann.de"
private const val DEFAULT_SERVER_IP = "192.168.1.202"

internal actual fun createPlannrHttpClient(): HttpClient =
    HttpClient(OkHttp) {
        configurePlannrHttpClient()

        engine {
            dns = Dns { hostname ->
                if (hostname != DEFAULT_SERVER_HOST) {
                    return@Dns Dns.SYSTEM.lookup(hostname)
                }

                runCatching {
                    Dns.SYSTEM.lookup(hostname)
                }.getOrElse {
                    listOf(InetAddress.getByName(DEFAULT_SERVER_IP))
                }
            }
        }
    }
