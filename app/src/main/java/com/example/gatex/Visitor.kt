package com.example.gatex

data class Visitor(
    val id: String?,
    val securityId:String?,
    val inTime: String?,
    val dateEntery: String?,
    val name: String?,
    val outTime: String?,
    val security: String?,
    val phoneNumber: String?,
    val purpose: String?,
    val vehicleImageUrl: String?,
    val visitorIdImageUrl: String?,
    val visitorImageUrl: String?
){
    constructor() : this("", "","", "", "","", "", "", "", "", "", "")
}