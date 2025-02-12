package product.model.command.merchandise

import com.github.michaelbull.result.getOrThrow
import common.model.type.primitive.NonEmptyString
import common.model.type.primitive.PositiveInt
import common.model.type.primitive.ID
import product.model.type.Product
import product.model.type.DisplayOrder
import product.model.type.addToFront


// input as commands
sealed interface CommandSchema
    data class AddProduct(
        val metaData: Product.ProductMetaData,
    ) : CommandSchema

    data class UpdateProduct(
        val metaData: Product.ProductMetaData,
    ) : CommandSchema

    data class ReorderProducts(
        val newOrders: Map<ID<Product>, PositiveInt>
    ) : CommandSchema

    data class SuspendStocking(
        val reason: NonEmptyString
    ) : CommandSchema

    data object ResumeStocking : CommandSchema


// output as events, and transition actions as command method
sealed interface Merchandise {
    data object Empty : Merchandise {
        // allowed transitions from current state
        fun addProduct(command: AddProduct, displayOrder: DisplayOrder): Open.Added = 
            registerNewProduct(command, displayOrder).let { Open.Added(it.product, it.displayOrder) }
        fun suspendStocking(command: SuspendStocking): Suspended = Suspended(command.reason)
    }

    sealed interface Open: Merchandise {
        data object Opened: Open
            fun addProduct(command: AddProduct, displayOrder: DisplayOrder): Open.Added = 
                registerNewProduct(command, displayOrder).let { Open.Added(it.product, it.displayOrder) }
            fun update(command: UpdateProduct): Updated = updateProductMetadata(command)
            fun suspendStocking(command: SuspendStocking): Suspended = Suspended(command.reason)
            
            data class Added(val product: Product.OnSale, val displayOrder: DisplayOrder): Open
            data class Updated(val product: Product.OnSale): Open
            data class Reordered(val newOrders: Map<ID<Product>, PositiveInt>): Open
    }

    data class Suspended(val reason: NonEmptyString): Merchandise {
        fun resumeStocking(): Merchandise = Open.Opened
    }
}

// actual operations
internal fun registerNewProduct(command: AddProduct, currentDisplayOrder: DisplayOrder): Merchandise.Open.Added {
    val productId = NonEmptyString.from("PRODUCT-" + System.currentTimeMillis()).getOrThrow { Throwable("エラー") }
    val product = Product.OnSale(
        Product.ProductMetaData(
            productId = productId,
            name = command.metaData.name,
            description = command.metaData.description,
            category = command.metaData.category
        )
    )
    return Merchandise.Open.Added(product, currentDisplayOrder.addToFront(productId))
}

internal fun updateProductMetadata(
    command: UpdateProduct
): Merchandise.Open.Updated =
    Merchandise.Open.Updated(
        Product.OnSale(
            Product.ProductMetaData(
                productId = command.metaData.productId,
                name = command.metaData.name,
                description = command.metaData.description,
                category = command.metaData.category
            )
        )
    )

// IO Protocol
interface MerchandiseRepository {
    fun findById(id: ID<Product>): Product.OnSale
    fun save(product: Product.OnSale): Product.OnSale
}