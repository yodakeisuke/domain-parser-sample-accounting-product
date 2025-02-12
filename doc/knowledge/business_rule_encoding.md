# ビジネスルールのエンコード指針

## 1. 基本原則

### 単一責務の原則
- 1つのルールは1つの関数として実装
- 各関数は1つの検証/変換のみを担当
- 複数のルールは合成して適用

### Railway Oriented Programming
- 検証と変換を`Result`型で表現
- エラーは明示的にハンドリング
- ルールの連鎖は`flatMap`で表現

## 2. 実装パターン

### 検証ルール
```kotlin
// 1. 存在チェック
private fun DisplayOrder.validateProductExists(
    productId: NonEmptyString
): Result<Unit, String> =
    if (productId in this) Ok(Unit)
    else Err("指定された商品ID: ${productId.value}は存在しません")

// 2. 値の範囲チェック
private fun DisplayOrder.validateNewOrder(
    newOrder: PositiveInt
): Result<Unit, String> {
    val maxOrder = this.values.maxOfOrNull { it.value } ?: 0
    return if (newOrder.value <= maxOrder) Ok(Unit)
    else Err("新しい表示順は現在の最大値(${maxOrder})までしか指定できません")
}
```

### 変換ルール
```kotlin
// 状態の変換
private fun DisplayOrder.recalculateOrders(
    productId: NonEmptyString,
    newOrder: PositiveInt
): Result<DisplayOrder, String> = binding {
    val currentOrder = this[productId]!!.value
    
    this.mapValues { (_, order) ->
        when (order.value) {
            currentOrder -> newOrder
            in newOrder.value..currentOrder -> PositiveInt.from(order.value + 1).bind()
            in (currentOrder + 1)..newOrder.value -> PositiveInt.from(order.value - 1).bind()
            else -> order
        }
    }
}
```

### 不変条件の検証
```kotlin
// 状態の一貫性チェック
private fun DisplayOrder.validateProductIdsUnchanged(
    newOrders: DisplayOrder
): Result<DisplayOrder, String> =
    if (newOrders.keys == this.keys) Ok(newOrders)
    else Err("商品IDの集合が変更されています")
```

## 3. ルールの合成

### Railway Oriented Patternによる合成
```kotlin
fun DisplayOrder.updateOrder(
    productId: NonEmptyString,
    newOrder: PositiveInt
): Result<DisplayOrder, String> =
    validateProductExists(productId)
        .flatMap { validateNewOrder(newOrder) }
        .flatMap { recalculateOrders(productId, newOrder) }
        .flatMap { validateProductIdsUnchanged(it) }
```

## 4. 設計上の注意点

### エラーメッセージ
- 業務的な文脈で理解できる表現を使用
- エラーの原因と対処方法を明確に
- 必要な情報（パラメータ値など）を含める

### コードの構造化
- 検証ルールは先に実行
- 変換ルールは検証後に実行
- 不変条件は最後に検証

### シンプル性の維持
- 複雑な条件は範囲式などで簡潔に表現
- 中間変数で意図を明確に
- 不要な修飾子は避ける

### 型の活用
- ドメインの制約は可能な限り型で表現
- `Result`型でエラーハンドリングを明示的に
- 値オブジェクトで不正な値の生成を防止

## 5. テスタビリティ

### テストの容易性
- 各ルールは独立してテスト可能
- 入力と出力が明確
- エラーケースが明示的

### エッジケースの考慮
- 境界値のテスト
- エラーケースのテスト
- 不変条件の検証

## 6. メンテナンス性

### 拡張性
- 新しいルールの追加が容易
- 既存のルールの修正が局所的
- ルールの順序変更が容易

### 可読性
- ルールの意図が明確
- エラーメッセージが理解しやすい
- コードの構造が直感的 