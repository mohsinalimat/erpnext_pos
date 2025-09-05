package com.erpnext.pos.remoteSource.dto

import androidx.room.PrimaryKey

data class ItemDto(
    @PrimaryKey
    var id: String,
    var name: String = "",
    var description: String,
    var barcode: String = "",
    var image: String = "",
    var price: Double = 0.0,
    var discount: Double = 0.0,
    var isService: Boolean = false,
    var isStocked: Boolean = false,
    var uom: String,
)