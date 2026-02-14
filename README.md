# swuit

Android の `LinearLayout` / `RelativeLayout` に相当するレイアウトマネージャーを Swing 向けに提供するライブラリ。

## プロジェクト構成

| モジュール | 説明 |
|---|---|
| `library` | レイアウトライブラリ本体 |
| `demo-common` | デモ共通のパネル生成ロジック |
| `demo-swing` | 素の Swing によるデモアプリ |
| `demo-flatlaf` | FlatLaf 適用のデモアプリ |

## レイアウト一覧

| クラス | パッケージ | 説明 |
|---|---|---|
| `LinearLayout` | `o.t.swuit.linear` | 子コンポーネントを水平または垂直に一列に並べる |
| `RelativeLayout` | `o.t.swuit.relative` | 子コンポーネント同士や親コンテナとの相対位置で配置する |

## ビルド

```bash
./gradlew build
```

## デモの実行

```bash
# 素の Swing 版
./gradlew :demo-swing:run

# FlatLaf 版
./gradlew :demo-flatlaf:run
```

## 動作要件

- Java 17 以上
