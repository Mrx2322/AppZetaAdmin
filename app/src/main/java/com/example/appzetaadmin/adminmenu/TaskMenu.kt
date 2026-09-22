package com.example.appzetaadmin.adminmenu

data class TaskMenu(
    val id: Int,
    var name: String,
    var precio: Double,
    var stock: Int = 0
)