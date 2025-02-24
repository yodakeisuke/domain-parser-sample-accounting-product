package common.infra

import common.model.type.primitive.ID
import common.model.type.primitive.PositiveInt
import product.model.command.merchandise.SaveProduct
import product.model.type.DisplayOrder
import product.model.type.Product

// インメモリのモック
val products = mutableMapOf<ID<Product>, Product.OnSale>()
val orders = mutableMapOf<ID<Product>, PositiveInt>()

val inMemorySaveProduct: SaveProduct = { product: Product.OnSale, displayOrder: DisplayOrder ->
    products[product.metaData.productId] = product
    orders.putAll(displayOrder)
    product
}