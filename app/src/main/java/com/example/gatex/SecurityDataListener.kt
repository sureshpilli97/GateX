package com.example.gatex

interface SecurityDataListener {
    fun onDataChanged(security: Security)
}