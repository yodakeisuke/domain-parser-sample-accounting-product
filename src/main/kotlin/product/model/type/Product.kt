package product.model.type

import common.model.type.primitive.ID
import common.model.type.primitive.NonEmptyString
import common.model.type.primitive.PositiveInt

sealed interface Product {
    val metaData: ProductMetaData
    data class ProductMetaData(
        val productId: ID<Product>,
        val name: NonEmptyString,
        val description: NonEmptyString,
        val category: NonEmptyString,
    )

    data class OnSale(
        override val metaData: ProductMetaData,
    ) : Product {
        fun discontinue(): Discontinued = Discontinued(metaData)
        fun feature(): Featured = Featured(metaData)
    }

    data class Featured(
        override val metaData: ProductMetaData,
    ) : Product {
        fun unfeature(): OnSale = OnSale(metaData)
        fun discontinue(): Discontinued = Discontinued(metaData)
    }

    data class Discontinued(
        override val metaData: ProductMetaData,
    ) : Product
}
