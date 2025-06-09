---
description: When reading the event storming diagram and creating the aggregate design map.
globs: 
---
# Aggregate Design Canvas Sample

## 1. Name
**OrderAggregate**

## 2. Description
- このアグリゲートは、ECサイトにおける注文のライフサイクル全体（生成、確認、配送準備、出荷、配達、キャンセル）を管理する役割を担います。  

## 3. State Transitions

```mermaid
stateDiagram-v2
    [*] --> Created
    Created --> Confirmed : ConfirmOrder
    Confirmed --> ReadyForDelivery : PrepareDelivery
    ReadyForDelivery --> Shipped : ShipOrder
    Shipped --> Delivered : DeliverOrder
    Created --> Cancelled : CancelOrder
    Confirmed --> Cancelled : CancelOrder
    ReadyForDelivery --> Cancelled : CancelOrder
``` 

## 4. Enforced Invariants & Corrective Policies
Enforced Invariants:
- 注文には最低1つ以上のアイテムが含まれていること。
- 注文の合計金額は1円以上100万円未満であること。
- キャンセル済みまたは確定後の注文は、再度内容変更ができないこと。
- 配送準備状態では在庫引当が完了していること。

Corrective Policies:
- 在庫不足などの問題発生時には、該当注文をキャンセルする。
- 引き落とし失敗などの問題発生時には、該当注文をキャンセルする。
※ 補足: 上記の2つのポリシーは、それぞれ在庫集約および支払い集約が別であることに基づいている

## 5. Handled Commands & Created Events
| Handled Command  | Created Event     |
|------------------|-------------------|
| CreateOrder      | OrderCreated      |
| ConfirmOrder     | OrderConfirmed    |
| PrepareDelivery  | DeliveryPrepared  |
| ShipOrder        | OrderShipped      |
| DeliverOrder     | OrderDelivered    |
| CancelOrder      | OrderCancelled    |

## 6. Throughput
|                           | Average | Maximum |
|---------------------------|---------|---------|
| **Command handling rate** | 50 command/day | 200 command/day |
| **Total number of clients** | 10 clients | 50 clients |
| **Concurrency conflict chance** | medium | high |

## 7. Size
|                           | Average | Maximum |
|---------------------------|---------|---------|
| **Event growth rate** | 5 events/day | 20 events/day |
| **Lifetime of a single instance** | 7 days | 30 days |
| **Number of events persisted** | medium | large |
