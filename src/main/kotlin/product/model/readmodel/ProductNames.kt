package product.model.readmodel

import common.model.type.primitive.NonEmptyString
import product.model.type.ProductNames

// protocol
typealias ReadProductNames = () -> ProductNames

// サンプル用のインメモリ実装
val allProductNames: ReadProductNames = {
    val names = mutableSetOf<NonEmptyString>()
    names.toSet()
} 