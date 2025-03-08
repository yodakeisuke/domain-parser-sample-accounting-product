package common.infra

import com.github.michaelbull.result.mapBoth
import product.flow.AddProductError
import product.flow.AddProductRequest
import product.flow.addProductOrchestration
import product.model.readmodel.allProductNames
import product.model.readmodel.allDisplayOrders


data class HttpResponse<T>(
    val status: Int,
    val body: T
)

sealed interface ApiError {
    val message: String
    val status: Int
}

data class BadRequestError(override val message: String) : ApiError {
    override val status: Int = 400
}

data class InternalServerError(override val message: String) : ApiError {
    override val status: Int = 500
}

fun addProduct(
    request: AddProductRequest,
): HttpResponse<*> {
    return addProductOrchestration(
        // request
        request = request,
        // 依存性注入
        readProductNames = allProductNames,
        readDisplayOrder = allDisplayOrders,
        saveMerchandise = inMemorySaveProduct,
        restoreMerchandiseState = inMemoryRestoreMerchandiseState
    ).mapBoth(
        success = { added ->
            HttpResponse(
                status = 201,
                body = mapOf(
                    "productId" to added.product.metaData.productId.value.value,
                    "message" to "Product added successfully"
                )
            )
        },
        failure = { error ->
            when (error) {
                is AddProductError.InvalidRequest -> HttpResponse(
                    status = BadRequestError(error.message).status,
                    body = mapOf("error" to error.message)
                )
                is AddProductError.DomainError -> HttpResponse(
                    status = InternalServerError("Failed to add product: ${error.error}").status,
                    body = mapOf("error" to "Failed to add product: ${error.error}")
                )
            }
        }
    )
}
