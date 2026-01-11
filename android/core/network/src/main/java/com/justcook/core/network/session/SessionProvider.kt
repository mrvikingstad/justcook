package com.justcook.core.network.session

/**
 * Interface for providing session token to the network layer.
 * Implemented by the data layer to avoid circular dependencies.
 */
interface SessionProvider {
    fun getSessionToken(): String?
    fun onSessionExpired()
}
