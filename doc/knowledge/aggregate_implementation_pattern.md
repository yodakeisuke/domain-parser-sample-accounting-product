# 集約の実装パターン

## 基本構造

集約は以下の要素で構成される：

1. `CommandSchema`: 入力となるコマンドを定義
2. 集約のルート型（例：`sealed interface Merchandise`）: 集約全体の状態を表現
3. ヘルパー関数: 状態遷移のロジックを実装

## 命名規則

### コマンド
- 操作の意図を明確に表現する動詞から始める
  - 良い例: `AddProduct`, `UpdateProduct`, `FreezeOrder`
  - 悪い例: `ProductCommand`, `OrderCommand`

### 状態
- 状態を表す形容詞や過去分詞を使用
  - 良い例: `Empty`, `Added`, `Updated`, `OrderFrozen`
  - 悪い例: `Init`, `AddProduct`, `UpdateProduct`

### 実際の手順 関数
- ドメインの用語を使用し、操作の意図を明確に表現
  - 良い例: 
    - `registerNewProduct`: 新商品の登録
    - `updateProductMetadata`: 商品のメタデータ更新
    - `freezeOrderAcceptance`: 注文受付の停止
    - `changeProductOrder`: 商品の表示順序変更
  - 悪い例:
    - `createProduct`: 操作が抽象的すぎる
    - `processOrder`: 具体的な操作が不明確
    - `updateState`: ドメインの文脈が欠如

## 状態遷移の制約

状態遷移の制約は、型システムを活用して表現する：

1. sealed interfaceによる状態の列挙
```kotlin
sealed interface Merchandise {
    data object Empty : Merchandise
    data class Added(val product: Product.OnSale) : Merchandise
    data class Updated(val product: Product.OnSale) : Merchandise
    data class OrderFrozen(val product: Product.Discontinued) : Merchandise
}
```

2. 各状態で許可される操作をメソッドとして直接定義
```kotlin
data class Added(val product: Product.OnSale) : Merchandise {
    fun update(command: UpdateProduct): Updated
    fun freeze(command: FreezeOrder): OrderFrozen
    fun reorder(command: ReorderProducts): Reordered
}
```

注意: 状態遷移のインターフェース（Updatable, Freezableなど）は使用しない。
理由：
- 各状態で可能な操作は、その状態のクラスに直接定義する方が明確
- インターフェースによる間接的な制約よりも、型システムによる直接的な制約の方が安全
- コードの見通しが良くなる

## 実際の手順の役割

実際の手順は以下の責務を持つ：

1. 状態遷移の実際のロジックをカプセル化
2. 共通処理の再利用
3. コードの重複を防ぐ

```kotlin
private fun updateProductMetadata(
    product: Product.OnSale,
    command: UpdateProduct
): Updated =
    Updated(
        Product.OnSale(
            Product.ProductMetaData(
                productId = product.metaData.productId,
                name = command.name,
                description = command.description,
                category = command.category,
                displayOrder = product.metaData.displayOrder
            )
        )
    )
```

## 値オブジェクトの活用

プリミティブ型の代わりに、ドメインの制約を表現する値オブジェクトを使用：

```kotlin
data class ProductMetaData(
    val productId: NonEmptyString,
    val name: NonEmptyString,
    val description: NonEmptyString,
    val category: NonEmptyString,
    val displayOrder: PositiveInt
)
```

これにより：
- ドメインの制約が型レベルで表現される
- 不正な値の生成を防ぐ
- コードの意図が明確になる
