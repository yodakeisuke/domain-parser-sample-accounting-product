package product.model.command.merchandise

import com.github.michaelbull.result.*
import common.model.type.primitive.NonEmptyString
import common.model.type.primitive.ID
import product.model.type.Product
import product.model.type.DisplayOrder
import product.model.type.ProductNames
import product.model.type.addToFront
import common.model.type.primitive.PositiveInt


// 集約
sealed interface Merchandise {
    // それぞれの状態
    data object Empty : Merchandise {
        fun addProduct(
            metaData: Product.ProductMetaData,
            displayOrder: DisplayOrder,
            allProductNames: ProductNames
        ): Result<Open.Added, Open.MerchandiseError>
            = registerNewProduct(metaData, displayOrder, allProductNames)

        fun suspendStocking(reason: NonEmptyString): Suspended = Suspended(reason)
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

        fun addProduct(
            metaData: Product.ProductMetaData,
            displayOrder: DisplayOrder,
            allProductNames: ProductNames
        ): Result<Added, MerchandiseError>
            = registerNewProduct(metaData, displayOrder, allProductNames)

        fun update(
            metaData: Product.ProductMetaData,
            allProductNames: ProductNames
        ): Result<Updated, MerchandiseError> = updateProductMetadata(metaData, allProductNames)

        fun suspendStocking(reason: NonEmptyString): Suspended = Suspended(reason)
    }

    data class Suspended( // 入荷停止
        val reason: NonEmptyString
    ) : Merchandise {
        fun resumeStocking(): Merchandise = Open.Opened("resume")
    }
}

// actual operations（これらも膨らみそうなら「業務手順」としてモデルに切り出してもよさそう）
internal fun registerNewProduct(
    metaData: Product.ProductMetaData,
    currentDisplayOrder: DisplayOrder,
    allProductNames: ProductNames
): Result<Merchandise.Open.Added, Merchandise.Open.MerchandiseError> = binding {

    val checkedName = isProductNameExists(metaData.name, allProductNames).mapError {
        Merchandise.Open.MerchandiseError.ProductAlreadyExists(
            productId = metaData.productId,
            message = "陳列できる商品は10個までです"
        )
    }.bind()

    validateProductCount(metaData.productId, allProductNames).bind()

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
    metaData: Product.ProductMetaData,
    allProductNames: ProductNames,
): Result<Merchandise.Open.Updated, Merchandise.Open.MerchandiseError> = binding {
    // 名前の重複チェック（自身の商品は除外）
    val checkedName = isProductNameExists(metaData.name, allProductNames).mapError {
        Merchandise.Open.MerchandiseError.ProductAlreadyExists(
            productId = metaData.productId,
            message = "指定された商品名は既に使用されています"
        )
    }.bind()

    Merchandise.Open.Updated(
        Product.OnSale(
            Product.ProductMetaData(
                productId = metaData.productId,
                name = checkedName,
                description = metaData.description,
                category = metaData.category
            )
        )
    )
}

// sub steps
internal fun validateProductCount(
    productId: ID<Product>,
    productNames: ProductNames,
    ): Result<Unit, Merchandise.Open.MerchandiseError> {
    val max = 10
    if (productNames.size >= max) {
        return Err(
            Merchandise.Open.MerchandiseError.MaxProductCountExceeded(
                productId = productId,
                message = "Maximum product count of $max exceeded"
            )
        )
    }
    return Ok(Unit)
}

private fun isProductNameExists(
    name: NonEmptyString,
    existingNames: ProductNames,
): Result<NonEmptyString, NonEmptyString> {
    val nameExists = existingNames.contains(name)
    
    return if (nameExists) {
        Err(NonEmptyString.from("Product with name '$name' already exists")
            .getOrThrow { IllegalStateException("Invalid error message") })
    } else {
        Ok(name)
    }
}

// 集約の単位での書き込みプロトコル
typealias SaveProduct
    = (products: Product.OnSale, displayOrder: DisplayOrder) -> Product.OnSale