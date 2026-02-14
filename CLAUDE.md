# プロジェクト概要

Android の LinearLayout / RelativeLayout に相当するレイアウトマネージャーを Swing 向けに提供するライブラリ。
Gradle 9 マルチモジュール構成 (Groovy DSL)。

## モジュール構成

- `swuit-core` — ライブラリ本体 (`java-library` プラグイン)
- `demo-common` — デモ共通パネル生成 (`java-library` プラグイン)
- `demo-swing` — 素の Swing デモ (`application` プラグイン)
- `demo-flatlaf` — FlatLaf 適用デモ (`application` プラグイン)

## パッケージ構成 (library)

- `org.to0mi1.swuit.layout` — レイアウト共通 (Gravity, Orientation)
- `org.to0mi1.swuit.layout.linear` — LinearLayout (水平・垂直の一列配置)
- `org.to0mi1.swuit.layout.relative` — RelativeLayout (相対位置による配置)

## ビルドコマンド

- ビルド: `./gradlew build`
- テスト: `./gradlew test`
- デモ実行: `./gradlew :demo-swing:run` / `./gradlew :demo-flatlaf:run`

## コーディング規約

- Java 17
- パッケージ: `org.to0mi1.swuit.layout.*` (ライブラリ), `org.to0mi1.swuit.demo.common` (デモ共通), `org.to0mi1.swuit.demo.swing` / `org.to0mi1.swuit.demo.flatlaf` (デモ)
- テストは `swuit-core` モジュール内に JUnit 5 で記述
