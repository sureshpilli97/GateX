package com.example.gatex

import android.util.Log
object ServiceUser {
    private val listeners = mutableListOf<SecurityDataListener>()
    private var securityData: Security? = null

    fun registerListener(listener: SecurityDataListener) {
        listeners.add(listener)
        securityData?.let { listener.onDataChanged(it) }
    }

    fun unregisterListener(listener: SecurityDataListener) {
        listeners.remove(listener)
    }

    fun setUserData(userData: Security) {
        securityData = userData
        listeners.forEach { it.onDataChanged(userData) }
    }

    fun getUserData(): Security? {
        return securityData
    }
}
