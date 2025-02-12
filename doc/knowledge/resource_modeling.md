# リソースモデリングの指針

リソースモデルは、その性質や要件に応じて適切な実装方法を選択する。
複雑な状態遷移や関係性を持つ場合は圏論的アプローチが有効だが、
単純な値の集合や参照系のモデルの場合は、よりシンプルな実装で十分な場合もある。

## 1. シンプルなモデリング

### 基本原則
- リソースモデルは必要以上に複雑にしない
- 基本的なデータ構造（Map等）の機能を活かしつつ、ドメインの意図を表現する
- 不要な抽象化層は避ける
- ReadModelの場合、DBから読み込むデータは既にバリデーション済みとみなせる

### 実装パターン
1. **typealiasと拡張関数の活用**
```kotlin
// 基本的なデータ構造をドメインの型として定義
typealias DisplayOrder = Map<NonEmptyString, PositiveInt>

// 必要な操作を拡張関数として追加
fun DisplayOrder.getOrder(productId: NonEmptyString): PositiveInt? = 
    this[productId]

fun DisplayOrder.updateOrder(productId: NonEmptyString, newOrder: PositiveInt): DisplayOrder =
    this + (productId to newOrder)
```

## 2. 状態を持つリソースのモデリング

### 基本原則
- リソースの状態は、sealed interfaceを使用して表現する
- 状態間の遷移を射として定義する
- 状態自体は属性を持たず、メタデータとして分離する

### 実装例
```kotlin
sealed interface Product {
    val metaData: ProductMetaData

    data class OnSale(override val metaData: ProductMetaData) : Product {
        fun discontinue(): Discontinued = Discontinued(metaData)  // 状態間の射
    }
    data class Featured(override val metaData: ProductMetaData) : Product
    data class Discontinued(override val metaData: ProductMetaData) : Product
}
```

## 3. メタデータの分離

### 基本原則
- リソースの属性はメタデータとして分離する
- メタデータはdata classとして実装する
- メタデータはイミュータブルな値オブジェクトとして扱う

### 実装例
```kotlin
data class ProductMetaData(
    val productId: NonEmptyString,
    val name: NonEmptyString,
    val description: NonEmptyString,
    val category: NonEmptyString,
    val displayOrder: PositiveInt
)
```

## 4. 圏論的アプローチ

### 基本構造
- **対象（Objects）**: 
  - 状態（例：OnSale, Discontinued）
  - 分類（例：商品カテゴリ、価格帯）
  - バリエーション（例：商品の仕様、サイズ）
  - その他、モデル化する概念に応じた対象

- **射（Morphisms）**: 対象間の関係性を表す変換や対応
  - 状態遷移（例：販売中 → 販売終了）
  - 分類関係（例：サブカテゴリ → 親カテゴリ）
  - 包含関係（例：基本仕様 → 拡張仕様）
  - 依存関係（例：補完商品間の関係）
  - その他、対象間の意味のある関係性全て

### モデリングの視点
- 対象と射の選択は、モデル化する領域の本質に基づいて行う
- 単なる状態遷移だけでなく、概念間の関係性全体を射として捉える
- 射の合成可能性を考慮してモデリングを行う

## 5. 命名規則

### 基本原則
- 技術的な実装の詳細を型名に含めない（例：Map, Sequence等）
- ドメインエキスパートが使用する業務用語を使用する
- 日本語の業務用語を適切に英訳する

### 例
```kotlin
// 良い例
typealias DisplayOrder = Map<NonEmptyString, PositiveInt>
sealed interface Product
data class Price

// 悪い例
typealias DisplayOrderMap = Map<NonEmptyString, PositiveInt>
sealed interface ProductAggregate
data class PriceValue
```

## 6. 値の表現

### 基本原則
- プリミティブな値は適切な制約付き型で表現する
- 共通の制約は共通のValue Objectとして実装する
- ドメイン固有の制約が必要な場合は、専用の型を定義する

### 実装例
```kotlin
// 共通の制約付き型の使用
import common.model.type.primitive.NonEmptyString
import common.model.type.primitive.PositiveInt
```

## 7. ReadModelとしてのリソースモデル

### 基本原則
- DBから読み込むデータは既にバリデーション済みとみなせる
- ReadModelでは不要なバリデーションを避ける
- 必要最小限のインターフェースを提供する

### 実装例
```kotlin
// シンプルなReadModel
typealias DisplayOrder = Map<NonEmptyString, PositiveInt>

// 必要最小限のインターフェース
fun DisplayOrder.getOrder(productId: NonEmptyString): PositiveInt? = this[productId]
fun DisplayOrder.toMap(): Map<NonEmptyString, PositiveInt> = this
```

## 8. 設計上の注意点

### シンプル性の維持
- 不要な抽象化層を避ける
- 基本的なデータ構造の機能を活用する
- 必要な機能のみを追加する
- モデルの性質に応じて適切な実装方法を選択する

### 型安全性の確保
- 値の制約は型レベルで表現する
- nullabilityは明示的に扱う
- 不正な状態遷移は型システムで防止する

### 拡張性への配慮
- 新しい操作の追加が容易な構造にする
- 基本的なデータ構造の機能を損なわない
- インターフェースはシンプルに保つ
- 状態や関係性の追加が容易な構造にする 