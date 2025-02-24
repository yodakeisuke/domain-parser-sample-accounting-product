package product.model.command.merchandise

import com.github.michaelbull.result.*
import common.model.type.primitive.NonEmptyString
import common.model.type.primitive.PositiveInt
import common.model.type.primitive.ID
import product.model.type.Product
import product.model.type.DisplayOrder
import product.model.type.addToFront

sealed interface Merchandise {
    // それぞれの状態
    data object Empty : Merchandise {
        // この状態から遷移可能なアクション群(alternative: ここではなくの下の方に拡張関数としてもいいかも)
        fun addProduct(
            metaData: Product.ProductMetaData,
            displayOrder: DisplayOrder
        ): Result<Open.Added, Open.MerchandiseError> = registerNewProduct(metaData, displayOrder)

        fun suspendStocking(  // マネージャによる入荷停止(権限まわりはここでは無視)
            reason: NonEmptyString
        ): Suspended = Suspended(reason)
    }

    sealed interface Open : Merchandise {
        // Openのサブ状態たち
        data class Opened(
            val reason: String
        ): Open
        data class Added(
            val product: Product.OnSale,
            val displayOrder: DisplayOrder
        ) : Open

        data class Updated(
            val product: Product.OnSale
        ) : Open

        // error path
        sealed interface MerchandiseError {
            data class ProductAlreadyExists(
                val productId: ID<Product>,
                val message: String
            ) : MerchandiseError

            data class MaxProductCountExceeded(
                val productId: ID<Product>,
                val message: String
            ) : MerchandiseError
        }

        // Open状態から可能な遷移アクション
        fun addProduct(
            metaData: Product.ProductMetaData,
            displayOrder: DisplayOrder
        ): Result<Added, MerchandiseError> = registerNewProduct(metaData, displayOrder)

        fun update(
            metaData: Product.ProductMetaData
        ): Updated = updateProductMetadata(metaData)

        fun suspendStocking(
            reason: NonEmptyString
        ): Suspended = Suspended(reason)
    }

    data class Suspended( // 入荷停止
        val reason: NonEmptyString
    ) : Merchandise {
        fun resumeStocking(): Merchandise = Open.Opened("resume")
    }
}

// actual operations
internal fun registerNewProduct(
    metaData: Product.ProductMetaData,
    currentDisplayOrder: DisplayOrder
): Result<Merchandise.Open.Added, Merchandise.Open.MerchandiseError> = binding {

    val checkedName = isProductNameExists(metaData.name).mapError {
        Merchandise.Open.MerchandiseError.ProductAlreadyExists(
            productId = metaData.productId,
            message = "陳列できる商品は10個までです"
        )
    }.bind()

    validateProductCount(metaData.productId).bind()

    Merchandise.Open.Added(
        Product.OnSale(
            Product.ProductMetaData(
                productId = metaData.productId,
                name = checkedName,
                description = metaData.description,
                category = metaData.category
            )
        ),
        currentDisplayOrder.addToFront(metaData.productId),
    )
}

internal fun updateProductMetadata(
    metaData: Product.ProductMetaData
): Merchandise.Open.Updated =
    Merchandise.Open.Updated(
        Product.OnSale(
            Product.ProductMetaData(
                productId = metaData.productId,
                name = metaData.name,
                description = metaData.description,
                category = metaData.category
            )
        )
    )

// sub steps
internal fun validateProductCount(productId: ID<Product>): Result<Unit, Merchandise.Open.MerchandiseError> {
    val max = 10
    if (getCurrentProductCount() >= max) {
        return Err(
            Merchandise.Open.MerchandiseError.MaxProductCountExceeded(
                productId = productId,
                message = "Maximum product count of $max exceeded"
            )
        )
    }
    return Ok(Unit)
}

private fun isProductNameExists(name: NonEmptyString): Result<NonEmptyString, NonEmptyString> {
    // TODO: Implement actual check using repository
    // 成功の場合は商品名を返す
    return Ok(name)
    // 失敗の場合はエラーメッセージを返す
    return Err(NonEmptyString.from("Product with name '$name' already exists")
        .getOrThrow { IllegalStateException("Invalid error message") })
}

// helpers
private fun getCurrentProductCount(): Int {
    // TODO: Implement actual count using repository
    return 0
}

private fun getCurrentProductIds(): Set<ID<Product>> {
    // TODO: Implement actual product IDs retrieval using repository
    return emptySet()
}

// IO Protocol
interface MerchandiseRepository {
    fun findById(id: ID<Product>): Product.OnSale
    fun save(product: Product.OnSale): Product.OnSale
    fun isProductNameExists(name: NonEmptyString): Result<NonEmptyString, NonEmptyString>
    fun getCurrentProductCount(): Int
    fun getCurrentProductIds(): Set<ID<Product>>
}
