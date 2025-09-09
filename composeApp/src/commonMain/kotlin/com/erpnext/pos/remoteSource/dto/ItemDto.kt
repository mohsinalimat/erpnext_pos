package com.erpnext.pos.remoteSource.dto

data class ItemDto(
    var id: String,
    var name: String = "",
    var itemCode: String = "",
    var description: String,
    var barcode: String = "",
    var image: String = "",
    var price: Double = 0.0,
    var actualQty: Double = 0.0,
    var discount: Double = 0.0,
    var isService: Boolean = false,
    var isStocked: Boolean = false,
    var uom: String,
)