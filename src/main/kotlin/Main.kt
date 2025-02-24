import common.infra.addProduct
import product.flow.AddProductRequest

fun main() {
    val request = AddProductRequest(
        name = "Test Product",
        description = "This is a test product",
        category = "Test Category"
    )

    val response = addProduct(request = request)

    println("Status: ${response.status}")
    println("Body: ${response.body}")
} 