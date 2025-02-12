package product.model.command.product

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Nested
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AggregateTest {
    @Nested
    inner class ProductAddedStateTest {
        @Test
        fun `商品追加後に商品情報を更新できる`() {
            // Given
            val productAdded = Aggregate.ProductAdded(
                productId = "PRODUCT-001",
                name = "テスト商品",
                description = "テスト商品の説明",
                category = "テストカテゴリ",
                displayOrder = 1
            )

            // When
            val command = UpdateProduct(
                name = "更新後の商品名",
                description = "更新後の商品説明",
                category = "更新後のカテゴリ"
            )
            val result = productAdded.updateProduct(command)

            // Then
            assertTrue(result.isSuccess)
            result.onSuccess { event ->
                assertEquals("更新後の商品名", event.name)
                assertEquals("更新後の商品説明", event.description)
                assertEquals("更新後のカテゴリ", event.category)
                assertEquals(productAdded.productId, event.productId)
                assertEquals(productAdded.displayOrder, event.displayOrder)
            }
        }

        @Test
        fun `商品追加後に販売停止できる`() {
            // Given
            val productAdded = Aggregate.ProductAdded(
                productId = "PRODUCT-001",
                name = "テスト商品",
                description = "テスト商品の説明",
                category = "テストカテゴリ",
                displayOrder = 1
            )

            // When
            val command = StopSelling(
                reason = "在庫切れ"
            )
            val result = productAdded.stopSelling(command)

            // Then
            assertTrue(result.isSuccess)
            result.onSuccess { event ->
                assertEquals(productAdded.productId, event.productId)
                assertEquals("在庫切れ", event.reason)
            }
        }

        @Test
        fun `商品追加後に表示順序を変更できる`() {
            // Given
            val productAdded = Aggregate.ProductAdded(
                productId = "PRODUCT-001",
                name = "テスト商品",
                description = "テスト商品の説明",
                category = "テストカテゴリ",
                displayOrder = 1
            )

            // When
            val command = ReorderProducts(
                newDisplayOrder = 2
            )
            val result = productAdded.reorderProducts(command)

            // Then
            assertTrue(result.isSuccess)
            result.onSuccess { event ->
                assertEquals(productAdded.productId, event.productId)
                assertEquals(2, event.newDisplayOrder)
            }
        }

        @Test
        fun `商品追加後に同じ商品を追加しようとするとエラーになる`() {
            // Given
            val productAdded = Aggregate.ProductAdded(
                productId = "PRODUCT-001",
                name = "テスト商品",
                description = "テスト商品の説明",
                category = "テストカテゴリ",
                displayOrder = 1
            )

            // When
            val command = AddProduct(
                name = "テスト商品",
                description = "テスト商品の説明",
                category = "テストカテゴリ"
            )
            val result = productAdded.addProduct(command)

            // Then
            assertTrue(result.isFailure)
        }
    }
} 