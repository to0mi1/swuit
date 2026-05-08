# swuit

[English](README.en.md) | 日本語

Android の `LinearLayout` / `RelativeLayout` や CSS の `Flexbox` / `Grid` に相当するレイアウトマネージャー、
および `RecyclerView` 相当のコンポーネントなどを Swing 向けに提供するライブラリ。

## インストール

GitHub Packages で配布しています。利用するには GitHub Packages リポジトリの認証設定が必要です。

### Gradle (Groovy DSL)

`build.gradle`:

```groovy
repositories {
    mavenCentral()
    maven {
        url = uri('https://maven.pkg.github.com/to0mi1/swuit')
        credentials {
            username = project.findProperty('gpr.user') ?: System.getenv('GITHUB_ACTOR')
            password = project.findProperty('gpr.key') ?: System.getenv('GITHUB_TOKEN')
        }
    }
}

dependencies {
    implementation 'org.to0mi1:swuit-core:0.2.0'
}
```

`~/.gradle/gradle.properties` に認証情報を設定:

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_GITHUB_TOKEN
```

### Gradle (Kotlin DSL)

`build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/to0mi1/swuit")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("org.to0mi1:swuit-core:0.2.0")
}
```

### Maven

`~/.m2/settings.xml` に認証情報を設定:

```xml
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>YOUR_GITHUB_USERNAME</username>
      <password>YOUR_GITHUB_TOKEN</password>
    </server>
  </servers>
</settings>
```

`pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/to0mi1/swuit</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>org.to0mi1</groupId>
        <artifactId>swuit-core</artifactId>
        <version>0.2.0</version>
    </dependency>
</dependencies>
```

> **Note:** GitHub Token には `read:packages` スコープが必要です。
> [Personal Access Token の作成方法](https://docs.github.com/ja/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens)

## 機能一覧

### レイアウト

| クラス | 元ネタ | 説明 |
|---|---|---|
| `LinearLayout` | Android LinearLayout | 子コンポーネントを水平または垂直に一列に並べる |
| `RelativeLayout` | Android RelativeLayout | 子コンポーネント同士や親コンテナとの相対位置で配置する |
| `CssFlexLayout` | CSS Flexbox | 折り返し・伸縮・多彩なアライメントを備えた柔軟な配置 |
| `CssGridLayout` | CSS Grid | 行列テンプレートによるグリッド配置 |
| `AspectRatioLayout` | CSS aspect-ratio | 子コンポーネントのアスペクト比を維持する |

### コンポーネント

| クラス | 元ネタ | 説明 |
|---|---|---|
| `RecyclerPane` | Android RecyclerView | ビューリサイクルによる大量アイテムの効率的スクロール表示 |
| `VirtualScrollPane` | — | 可視領域外の描画をスキップするスクロールペイン |
| `ImageView` | CSS object-fit / object-position | アスペクト比制御付き画像描画コンポーネント |
| `AutoCompleteComboBox` | — | フィルタ・サジェスト機能付き ComboBox |

---

## レイアウトの使い方

### LinearLayout

子コンポーネントを水平または垂直に一列に並べるレイアウト。`weight` による余剰スペース分配をサポート。

```java
import org.to0mi1.swuit.layout.Gravity;
import org.to0mi1.swuit.layout.Orientation;
import org.to0mi1.swuit.layout.linear.LinearLayout;
import org.to0mi1.swuit.layout.linear.LinearConstraints;
```

**基本的な垂直配置:**

```java
JPanel panel = new JPanel(new LinearLayout(Orientation.VERTICAL, 5));
panel.add(new JButton("Button 1"));
panel.add(new JButton("Button 2"));
panel.add(new JButton("Button 3"));
```

**weight による余剰スペースの分配 (1:2:1):**

```java
JPanel panel = new JPanel(new LinearLayout(Orientation.HORIZONTAL, 4));
panel.add(child1, new LinearConstraints(1));  // 25%
panel.add(child2, new LinearConstraints(2));  // 50%
panel.add(child3, new LinearConstraints(1));  // 25%
```

**gravity で副軸方向の配置を制御:**

```java
JPanel panel = new JPanel(new LinearLayout(Orientation.VERTICAL, 5));
panel.add(left,   new LinearConstraints(0, Gravity.LEFT));
panel.add(center, new LinearConstraints(0, Gravity.CENTER_HORIZONTAL));
panel.add(right,  new LinearConstraints(0, Gravity.RIGHT));
panel.add(fill,   new LinearConstraints(0, Gravity.FILL_HORIZONTAL));
```

**margin を指定:**

```java
panel.add(child, new LinearConstraints(1, Gravity.CENTER, new Insets(10, 10, 10, 10)));
```

#### LinearLayout プロパティ

| メソッド | 説明 |
|---|---|
| `setOrientation(Orientation)` | 主軸方向 (`HORIZONTAL` / `VERTICAL`) |
| `setGap(int)` | 子コンポーネント間のギャップ (px) |
| `setGravity(int)` | 子コンポーネント群全体の配置 |
| `setWeightSum(float)` | weight の合計上限 (0 = 自動計算) |

#### LinearConstraints プロパティ

| フィールド | デフォルト | 説明 |
|---|---|---|
| `weight` | `0` | 余剰スペース分配比率。0 の場合は preferredSize を使用 |
| `gravity` | `Gravity.NONE` | 副軸方向の配置 |
| `margin` | `null` | コンポーネント周囲の余白 |

---

### RelativeLayout

子コンポーネント同士や親コンテナとの相対位置で配置するレイアウト。Fluent API でルールをチェーン記述できる。

```java
import org.to0mi1.swuit.layout.relative.RelativeLayout;
import org.to0mi1.swuit.layout.relative.RelativeConstraints;
```

**基本的な相対配置:**

```java
JPanel panel = new JPanel(new RelativeLayout());

JLabel header  = new JLabel("Header");
JLabel sidebar = new JLabel("Sidebar");
JLabel content = new JLabel("Content");

// header: 親の上端・左端・右端に揃える
panel.add(header, new RelativeConstraints()
    .alignParentTop().alignParentLeft().alignParentRight());

// sidebar: header の下、親の左端に揃える
panel.add(sidebar, new RelativeConstraints()
    .below(header).alignParentLeft());

// content: header の下、sidebar の右
panel.add(content, new RelativeConstraints()
    .below(header).rightOf(sidebar).alignParentRight());
```

**中央配置:**

```java
panel.add(child, new RelativeConstraints().centerInParent());
```

**margin 付き:**

```java
panel.add(child, new RelativeConstraints()
    .alignParentTop().alignParentLeft()
    .margin(10, 10, 10, 10));
```

#### RelativeConstraints ルール

| メソッド | 説明 |
|---|---|
| `leftOf(Component)` | 指定コンポーネントの左に配置 |
| `rightOf(Component)` | 指定コンポーネントの右に配置 |
| `above(Component)` | 指定コンポーネントの上に配置 |
| `below(Component)` | 指定コンポーネントの下に配置 |
| `alignLeft(Component)` | 指定コンポーネントの左端に揃える |
| `alignRight(Component)` | 指定コンポーネントの右端に揃える |
| `alignTop(Component)` | 指定コンポーネントの上端に揃える |
| `alignBottom(Component)` | 指定コンポーネントの下端に揃える |
| `alignParentLeft()` | 親の左端に揃える |
| `alignParentRight()` | 親の右端に揃える |
| `alignParentTop()` | 親の上端に揃える |
| `alignParentBottom()` | 親の下端に揃える |
| `centerInParent()` | 親の中央に配置 |
| `centerHorizontal()` | 親の水平中央に配置 |
| `centerVertical()` | 親の垂直中央に配置 |
| `margin(top, left, bottom, right)` | 余白を設定 |

---

### CssFlexLayout

CSS Flexbox に相当するレイアウト。折り返し (wrap)、伸縮 (grow/shrink)、多彩なアライメントをサポート。

```java
import org.to0mi1.swuit.layout.flex.*;
```

**基本的な Flex 配置:**

```java
CssFlexLayout layout = new CssFlexLayout(CssFlexDirection.ROW)
    .setCssFlexWrap(CssFlexWrap.WRAP)
    .setCssJustifyContent(CssJustifyContent.SPACE_BETWEEN)
    .setCssAlignItems(CssAlignItems.CENTER)
    .setMainAxisGap(8)
    .setCrossAxisGap(8);

JPanel panel = new JPanel(layout);
panel.add(child1, new CssFlexConstraints().flexGrow(1));
panel.add(child2, new CssFlexConstraints().flexGrow(2));
panel.add(child3, new CssFlexConstraints().flexGrow(1));
```

**個別のアライメント (alignSelf):**

```java
panel.add(child, new CssFlexConstraints()
    .flexGrow(1)
    .alignSelf(CssAlignSelf.FLEX_END));
```

**サイズ制約付き:**

```java
panel.add(child, new CssFlexConstraints()
    .flexGrow(1)
    .minWidth(100).maxWidth(300));
```

#### CssFlexLayout プロパティ

| メソッド | 値 | 説明 |
|---|---|---|
| `setCssFlexDirection` | `ROW`, `ROW_REVERSE`, `COLUMN`, `COLUMN_REVERSE` | 主軸方向 |
| `setCssFlexWrap` | `NOWRAP`, `WRAP`, `WRAP_REVERSE` | 折り返し |
| `setCssJustifyContent` | `FLEX_START`, `FLEX_END`, `CENTER`, `SPACE_BETWEEN`, `SPACE_AROUND`, `SPACE_EVENLY` | 主軸配置 |
| `setCssAlignItems` | `FLEX_START`, `FLEX_END`, `CENTER`, `STRETCH` | 副軸配置 |
| `setCssAlignContent` | `FLEX_START`, `FLEX_END`, `CENTER`, `SPACE_BETWEEN`, `SPACE_AROUND`, `STRETCH` | 複数ライン時の副軸配置 |
| `setMainAxisGap(int)` | px | 主軸方向のギャップ |
| `setCrossAxisGap(int)` | px | 副軸方向のギャップ |

#### CssFlexConstraints プロパティ

| メソッド | デフォルト | 説明 |
|---|---|---|
| `flexGrow(float)` | `0` | 余剰スペース分配比率 |
| `flexShrink(float)` | `1` | 不足時の縮小比率 |
| `flexBasisPercent(float)` | `-1` | 初期サイズ (%)。-1 で preferredSize 使用 |
| `alignSelf(CssAlignSelf)` | `AUTO` | 個別の副軸配置 |
| `order(int)` | `0` | 表示順序 |
| `minWidth(int)`, `maxWidth(int)` | — | 幅の最小・最大制約 |
| `minHeight(int)`, `maxHeight(int)` | — | 高さの最小・最大制約 |
| `margin(top, left, bottom, right)` | — | 余白 |

---

### CssGridLayout

CSS Grid に相当するレイアウト。行列テンプレート (`fixed` / `fr` / `auto`)、スパン、セル内アライメントをサポート。

```java
import org.to0mi1.swuit.layout.grid.*;
```

**基本的な Grid 配置:**

```java
CssGridLayout layout = new CssGridLayout()
    .setColumnTemplate(CssTrackSize.fixed(100), CssTrackSize.fr(1), CssTrackSize.fr(2))
    .setRowTemplate(CssTrackSize.fixed(50), CssTrackSize.fr(1))
    .setColumnGap(8)
    .setRowGap(8);

JPanel panel = new JPanel(layout);

// header: 1行目、3列にまたがる
panel.add(header, new CssGridConstraints().column(0).row(0).columnSpan(3));

// sidebar: 2行目、1列目
panel.add(sidebar, new CssGridConstraints().column(0).row(1));

// content: 2行目、2〜3列目
panel.add(content, new CssGridConstraints().column(1).row(1).columnSpan(2));
```

**セル内の配置を個別に指定:**

```java
panel.add(child, new CssGridConstraints()
    .column(0).row(0)
    .justifySelf(CssJustifySelf.CENTER)
    .alignSelf(CssAlignSelf.CENTER));
```

**自動配置 (位置指定を省略):**

```java
// column/row を指定しなければ自動的に空きセルに配置される
for (int i = 0; i < 9; i++) {
    panel.add(new JButton("" + i));
}
```

#### CssTrackSize (トラックサイズ定義)

| メソッド | 説明 |
|---|---|
| `CssTrackSize.fixed(int px)` | 固定ピクセルサイズ |
| `CssTrackSize.fr(float fraction)` | 余剰スペース分配比率 (CSS の `fr` 単位) |
| `CssTrackSize.auto()` | コンテンツの preferredSize に合わせる |

#### CssGridLayout プロパティ

| メソッド | 説明 |
|---|---|
| `setColumnTemplate(CssTrackSize...)` | 列トラック定義 |
| `setRowTemplate(CssTrackSize...)` | 行トラック定義 |
| `setColumnGap(int)` | 列間ギャップ (px) |
| `setRowGap(int)` | 行間ギャップ (px) |
| `setCssJustifyItems(CssJustifyItems)` | 全セルの水平配置 (`START`, `END`, `CENTER`, `STRETCH`) |
| `setCssAlignItems(CssAlignItems)` | 全セルの垂直配置 (`START`, `END`, `CENTER`, `STRETCH`) |

#### CssGridConstraints プロパティ

| メソッド | デフォルト | 説明 |
|---|---|---|
| `column(int)` | `-1` | 列位置 (-1 で自動配置) |
| `row(int)` | `-1` | 行位置 (-1 で自動配置) |
| `columnSpan(int)` | `1` | 列方向の結合数 |
| `rowSpan(int)` | `1` | 行方向の結合数 |
| `justifySelf(CssJustifySelf)` | `AUTO` | 個別の水平配置 |
| `alignSelf(CssAlignSelf)` | `AUTO` | 個別の垂直配置 |
| `margin(top, left, bottom, right)` | — | 余白 |

---

### AspectRatioLayout

単一の子コンポーネントのアスペクト比を維持するレイアウト。CSS の `aspect-ratio` プロパティに相当。

```java
import org.to0mi1.swuit.layout.aspectratio.AspectRatioLayout;
```

```java
// 16:9 のアスペクト比を維持
JPanel container = new JPanel(new AspectRatioLayout(16.0 / 9.0));
container.add(child);
```

コンストラクタ引数はアスペクト比 (幅 / 高さ)。子の `preferredWidth` から高さを自動計算する。

---

## コンポーネントの使い方

### RecyclerPane

Android の `RecyclerView` に相当する、ビューリサイクルによる大量アイテムの効率的スクロール表示コンポーネント。

```java
import org.to0mi1.swuit.component.recycler.*;
import org.to0mi1.swuit.layout.Orientation;
```

**基本的な使い方:**

```java
// 1. ViewHolder を定義
class MyViewHolder extends RecyclerPane.ViewHolder {
    JLabel label;
    MyViewHolder() {
        super(new JLabel());
        label = (JLabel) itemView;
    }
}

// 2. Adapter を定義
class MyAdapter extends RecyclerPane.Adapter<MyViewHolder> {
    private final List<String> items;

    MyAdapter(List<String> items) { this.items = items; }

    @Override
    public MyViewHolder onCreateViewHolder(RecyclerPane parent, int viewType) {
        return new MyViewHolder();
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.label.setText(items.get(position));
    }

    @Override
    public int getItemCount() { return items.size(); }
}

// 3. RecyclerPane を組み立てる
RecyclerPane recycler = new RecyclerPane();
recycler.setLayoutManager(new LinearLayoutManager(Orientation.VERTICAL));
recycler.setAdapter(new MyAdapter(List.of("Apple", "Banana", "Cherry")));

JScrollPane scrollPane = new JScrollPane(recycler);
```

#### LayoutManager の種類

| クラス | 説明 |
|---|---|
| `LinearLayoutManager` | 垂直または水平の一列配置 |
| `GridLayoutManager` | 指定列数のグリッド配置 |
| `StaggeredGridLayoutManager` | 高さ可変のタイル (Pinterest 風) 配置 |

```java
// グリッド (3列)
recycler.setLayoutManager(new GridLayoutManager(3));

// スタッガードグリッド (2列)
recycler.setLayoutManager(new StaggeredGridLayoutManager(2));
```

#### データ変更通知

```java
adapter.notifyDataSetChanged();              // 全体更新
adapter.notifyItemInserted(index);           // 挿入
adapter.notifyItemRemoved(index);            // 削除
adapter.notifyItemChanged(index);            // 単一アイテムの差分更新
adapter.notifyItemRangeChanged(start, n);    // 範囲の差分更新
```

`notifyItemChanged` / `notifyItemRangeChanged` は差分更新として動作する。
表示中のホルダーがあれば該当 position だけを `onBindViewHolder` で再バインドし、
非表示で Cache に残っている古いエントリは破棄する。`preferredSize` が変化しない限り
全体レイアウトは走らないため、スクロール位置はそのまま維持される。

差分更新では追従できないケース (LayoutManager 側のサイズキャッシュを再計算したい等)
のために、`force` フラグ付きのオーバーロードで `notifyDataSetChanged()` 相当の
全体リレイアウトにフォールバックできる:

```java
adapter.notifyItemChanged(index, true);              // 全体リレイアウト
adapter.notifyItemRangeChanged(start, n, true);      // 同上
```

表示中の特定 ViewHolder を直接取得することもできる:

```java
RecyclerPane.ViewHolder vh = recycler.findViewHolderForAdapterPosition(index);
```

#### ItemDecoration

```java
recycler.addItemDecoration(new RecyclerPane.ItemDecoration() {
    @Override
    public Insets getItemOffsets(int position) {
        return new Insets(4, 4, 4, 4);  // アイテム間のスペース
    }

    @Override
    public void onDraw(Graphics g, RecyclerPane parent) {
        // 背景レイヤーに描画 (区切り線など)
    }
});
```

---

### VirtualScrollPane

大量の子コンポーネントを含むパネルの描画パフォーマンスを最適化するスクロールペイン。
可視領域外のコンポーネントを `setVisible(false)` にして描画をスキップする。

```java
import org.to0mi1.swuit.component.virtualscroll.VirtualScrollPane;
```

```java
JPanel panel = new JPanel(new LinearLayout(Orientation.VERTICAL, 4));
for (int i = 0; i < 10000; i++) {
    panel.add(new JLabel("Item " + i));
}
VirtualScrollPane scrollPane = new VirtualScrollPane(panel);
```

**バッファゾーンの設定:**

```java
// 可視領域の外側 100px まで描画対象にする (デフォルト: 50px)
scrollPane.setBufferZone(100);
```

`JScrollPane` のドロップイン代替として使用できる。既存のレイアウトマネージャーをそのまま利用可能。

---

### ImageView

CSS の `object-fit` / `object-position` に相当する画像描画コンポーネント。

```java
import org.to0mi1.swuit.component.image.ImageView;
import org.to0mi1.swuit.component.image.ObjectFit;
import org.to0mi1.swuit.component.image.ObjectPosition;
```

```java
ImageView view = new ImageView(myImage);
view.setObjectFit(ObjectFit.COVER);
view.setObjectPosition(ObjectPosition.CENTER);
```

#### ObjectFit (描画モード)

| 値 | 説明 |
|---|---|
| `FILL` | アスペクト比を無視してコンテナいっぱいに引き伸ばす |
| `CONTAIN` | アスペクト比を維持し、画像全体がコンテナに収まる最大サイズ |
| `COVER` | アスペクト比を維持し、コンテナ全体を覆う最小サイズ |
| `NONE` | 画像の固有サイズのまま描画 |
| `SCALE_DOWN` | `NONE` と `CONTAIN` の小さい方 (縮小のみ、拡大しない) |

#### ObjectPosition (描画位置)

| 定数 | 値 | 説明 |
|---|---|---|
| `ObjectPosition.CENTER` | `(0.5, 0.5)` | 中央 (デフォルト) |
| `ObjectPosition.TOP_LEFT` | `(0.0, 0.0)` | 左上 |
| `ObjectPosition.BOTTOM_RIGHT` | `(1.0, 1.0)` | 右下 |
| `new ObjectPosition(x, y)` | `(0.0〜1.0)` | 任意の位置 |

**AspectRatioLayout と組み合わせた例:**

```java
JPanel container = new JPanel(new AspectRatioLayout(16.0 / 9.0));
ImageView view = new ImageView(myImage);
view.setObjectFit(ObjectFit.COVER);
container.add(view);
```

---

### AutoCompleteComboBox

フィルタリング (ドロップダウン絞り込み) とサジェスト (インライン補完) の両方をサポートするオートコンプリート付き ComboBox。

```java
import org.to0mi1.swuit.component.autocomplete.AutoCompleteComboBox;
import org.to0mi1.swuit.component.autocomplete.ContainsMatcher;
```

**基本的な使い方:**

```java
var combo = new AutoCompleteComboBox<>(List.of("Apple", "Banana", "Cherry", "Date"));
```

**部分一致 (大文字小文字無視) に変更:**

```java
combo.setMatcher(new ContainsMatcher<>(true));
```

**フィルタのみ (インライン補完なし):**

```java
combo.setSuggestEnabled(false);
```

**カスタムオブジェクトを使用:**

```java
record Fruit(String name, int price) {}

var combo = new AutoCompleteComboBox<>(List.of(
    new Fruit("Apple", 100),
    new Fruit("Banana", 80)
));
combo.setStringConverter(Fruit::name);
```

#### AutoCompleteComboBox プロパティ

| メソッド | デフォルト | 説明 |
|---|---|---|
| `setMatcher(AutoCompleteMatcher)` | `StartsWithMatcher` | マッチング戦略 |
| `setStringConverter(Function)` | `String::valueOf` | 表示文字列への変換関数 |
| `setFilterEnabled(boolean)` | `true` | ドロップダウンの絞り込みを有効にする |
| `setSuggestEnabled(boolean)` | `true` | インライン補完を有効にする |
| `setSuggestIgnoreCase(boolean)` | `false` | サジェストで大文字小文字を無視する |
| `setPopupOnInput(boolean)` | `true` | 入力時にポップアップを自動表示する |

#### マッチャーの種類

| クラス | 説明 |
|---|---|
| `StartsWithMatcher` | 前方一致 (デフォルト) |
| `ContainsMatcher` | 部分一致 |

いずれもコンストラクタで `ignoreCase` (大文字小文字無視) を指定可能。

---

## 共通クラス

### Gravity

Android の `Gravity` に相当するビットフラグ定数。`LinearLayout` や `RelativeLayout` で使用する。

| 定数 | 説明 |
|---|---|
| `Gravity.LEFT` | 左寄せ |
| `Gravity.CENTER_HORIZONTAL` | 水平中央 |
| `Gravity.RIGHT` | 右寄せ |
| `Gravity.FILL_HORIZONTAL` | 水平方向に引き伸ばす |
| `Gravity.TOP` | 上寄せ |
| `Gravity.CENTER_VERTICAL` | 垂直中央 |
| `Gravity.BOTTOM` | 下寄せ |
| `Gravity.FILL_VERTICAL` | 垂直方向に引き伸ばす |
| `Gravity.CENTER` | 水平・垂直の中央 |
| `Gravity.FILL` | 水平・垂直に引き伸ばす |

### Orientation

レイアウトの主軸方向を表す enum。

| 値 | 説明 |
|---|---|
| `Orientation.HORIZONTAL` | 水平方向 (左から右) |
| `Orientation.VERTICAL` | 垂直方向 (上から下) |

---

## 動作要件

- Java 17 以上

## 開発者向け

### プロジェクト構成

| モジュール | 説明 |
|---|---|
| `swuit-core` | レイアウト・コンポーネントライブラリ本体 |
| `demo-common` | デモ共通のパネル生成ロジック |
| `demo-swing` | 素の Swing によるデモアプリ |
| `demo-flatlaf` | FlatLaf 適用のデモアプリ |

### ビルド

```bash
./gradlew build
```

### デモの実行

```bash
# 素の Swing 版
./gradlew :demo-swing:run

# FlatLaf 版
./gradlew :demo-flatlaf:run
```
