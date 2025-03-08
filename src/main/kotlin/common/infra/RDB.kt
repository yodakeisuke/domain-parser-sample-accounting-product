package common.infra

import common.model.type.primitive.ID
import common.model.type.primitive.PositiveInt
import product.model.command.merchandise.SaveProduct
import product.model.command.merchandise.Merchandise
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

// Merchandiseの状態を復元するためのモック実装
val inMemoryRestoreMerchandiseState: () -> Merchandise = {
    // 空の場合はEmpty状態を返す
    if (products.isEmpty()) {
        Merchandise.Empty
    } else {
        // 実際の商品があるときはOpen.Added状態を返す
        Merchandise.Open.Added(
            product = products.values.first(),
            displayOrder = orders.toMap()
        )
    }
}