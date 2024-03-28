package com.example.gatex
data class Security(
    var id: String?,
    var name: String?,
    var phoneNumber: String?,
    var email: String?,
    var img: String?,
    var designation: String?,
    var role: String?
){
    constructor(): this("","","","","","",""){}
}
