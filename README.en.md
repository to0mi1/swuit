# swuit

English | [日本語](README.md)

A library that provides layout managers equivalent to Android's `LinearLayout` / `RelativeLayout` and CSS `Flexbox` / `Grid`,
along with components such as a `RecyclerView` equivalent, for Swing.

## Installation

Distributed via GitHub Packages. Authentication to the GitHub Packages repository is required.

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

Add credentials to `~/.gradle/gradle.properties`:

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

Add credentials to `~/.m2/settings.xml`:

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

> **Note:** The GitHub Token requires the `read:packages` scope.
> [Creating a Personal Access Token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens)

## Features

### Layouts

| Class | Inspired By | Description |
|---|---|---|
| `LinearLayout` | Android LinearLayout | Arranges children horizontally or vertically in a single row/column |
| `RelativeLayout` | Android RelativeLayout | Positions children relative to siblings or the parent container |
| `CssFlexLayout` | CSS Flexbox | Flexible layout with wrapping, grow/shrink, and rich alignment |
| `CssGridLayout` | CSS Grid | Grid layout with row/column templates |
| `AspectRatioLayout` | CSS aspect-ratio | Maintains the aspect ratio of a single child |

### Components

| Class | Inspired By | Description |
|---|---|---|
| `RecyclerPane` | Android RecyclerView | Efficient scrollable display of large datasets with view recycling |
| `VirtualScrollPane` | — | Scroll pane that skips rendering off-screen components |
| `ImageView` | CSS object-fit / object-position | Image component with aspect ratio control |
| `AutoCompleteComboBox` | — | ComboBox with filtering and suggestion support |

---

## Layout Usage

### LinearLayout

A layout that arranges children horizontally or vertically in a single row/column. Supports distributing extra space via `weight`.

```java
import org.to0mi1.swuit.layout.Gravity;
import org.to0mi1.swuit.layout.Orientation;
import org.to0mi1.swuit.layout.linear.LinearLayout;
import org.to0mi1.swuit.layout.linear.LinearConstraints;
```

**Basic vertical arrangement:**

```java
JPanel panel = new JPanel(new LinearLayout(Orientation.VERTICAL, 5));
panel.add(new JButton("Button 1"));
panel.add(new JButton("Button 2"));
panel.add(new JButton("Button 3"));
```

**Distributing extra space with weight (1:2:1):**

```java
JPanel panel = new JPanel(new LinearLayout(Orientation.HORIZONTAL, 4));
panel.add(child1, new LinearConstraints(1));  // 25%
panel.add(child2, new LinearConstraints(2));  // 50%
panel.add(child3, new LinearConstraints(1));  // 25%
```

**Controlling cross-axis alignment with gravity:**

```java
JPanel panel = new JPanel(new LinearLayout(Orientation.VERTICAL, 5));
panel.add(left,   new LinearConstraints(0, Gravity.LEFT));
panel.add(center, new LinearConstraints(0, Gravity.CENTER_HORIZONTAL));
panel.add(right,  new LinearConstraints(0, Gravity.RIGHT));
panel.add(fill,   new LinearConstraints(0, Gravity.FILL_HORIZONTAL));
```

**Specifying margin:**

```java
panel.add(child, new LinearConstraints(1, Gravity.CENTER, new Insets(10, 10, 10, 10)));
```

#### LinearLayout Properties

| Method | Description |
|---|---|
| `setOrientation(Orientation)` | Main axis direction (`HORIZONTAL` / `VERTICAL`) |
| `setGap(int)` | Gap between children (px) |
| `setGravity(int)` | Alignment of all children as a group |
| `setWeightSum(float)` | Upper limit for total weight (0 = auto) |

#### LinearConstraints Properties

| Field | Default | Description |
|---|---|---|
| `weight` | `0` | Extra space distribution ratio. Uses preferredSize when 0 |
| `gravity` | `Gravity.NONE` | Cross-axis alignment |
| `margin` | `null` | Margin around the component |

---

### RelativeLayout

A layout that positions children relative to siblings or the parent container. Rules can be chained using a fluent API.

```java
import org.to0mi1.swuit.layout.relative.RelativeLayout;
import org.to0mi1.swuit.layout.relative.RelativeConstraints;
```

**Basic relative positioning:**

```java
JPanel panel = new JPanel(new RelativeLayout());

JLabel header  = new JLabel("Header");
JLabel sidebar = new JLabel("Sidebar");
JLabel content = new JLabel("Content");

// header: align to parent's top, left, and right edges
panel.add(header, new RelativeConstraints()
    .alignParentTop().alignParentLeft().alignParentRight());

// sidebar: below header, align to parent's left edge
panel.add(sidebar, new RelativeConstraints()
    .below(header).alignParentLeft());

// content: below header, to the right of sidebar
panel.add(content, new RelativeConstraints()
    .below(header).rightOf(sidebar).alignParentRight());
```

**Center positioning:**

```java
panel.add(child, new RelativeConstraints().centerInParent());
```

**With margin:**

```java
panel.add(child, new RelativeConstraints()
    .alignParentTop().alignParentLeft()
    .margin(10, 10, 10, 10));
```

#### RelativeConstraints Rules

| Method | Description |
|---|---|
| `leftOf(Component)` | Position to the left of the specified component |
| `rightOf(Component)` | Position to the right of the specified component |
| `above(Component)` | Position above the specified component |
| `below(Component)` | Position below the specified component |
| `alignLeft(Component)` | Align with the left edge of the specified component |
| `alignRight(Component)` | Align with the right edge of the specified component |
| `alignTop(Component)` | Align with the top edge of the specified component |
| `alignBottom(Component)` | Align with the bottom edge of the specified component |
| `alignParentLeft()` | Align with the parent's left edge |
| `alignParentRight()` | Align with the parent's right edge |
| `alignParentTop()` | Align with the parent's top edge |
| `alignParentBottom()` | Align with the parent's bottom edge |
| `centerInParent()` | Center within the parent |
| `centerHorizontal()` | Center horizontally within the parent |
| `centerVertical()` | Center vertically within the parent |
| `margin(top, left, bottom, right)` | Set margin |

---

### CssFlexLayout

A layout equivalent to CSS Flexbox. Supports wrapping, grow/shrink, and rich alignment options.

```java
import org.to0mi1.swuit.layout.flex.*;
```

**Basic flex layout:**

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

**Per-item alignment (alignSelf):**

```java
panel.add(child, new CssFlexConstraints()
    .flexGrow(1)
    .alignSelf(CssAlignSelf.FLEX_END));
```

**With size constraints:**

```java
panel.add(child, new CssFlexConstraints()
    .flexGrow(1)
    .minWidth(100).maxWidth(300));
```

#### CssFlexLayout Properties

| Method | Values | Description |
|---|---|---|
| `setCssFlexDirection` | `ROW`, `ROW_REVERSE`, `COLUMN`, `COLUMN_REVERSE` | Main axis direction |
| `setCssFlexWrap` | `NOWRAP`, `WRAP`, `WRAP_REVERSE` | Wrapping behavior |
| `setCssJustifyContent` | `FLEX_START`, `FLEX_END`, `CENTER`, `SPACE_BETWEEN`, `SPACE_AROUND`, `SPACE_EVENLY` | Main axis alignment |
| `setCssAlignItems` | `FLEX_START`, `FLEX_END`, `CENTER`, `STRETCH` | Cross axis alignment |
| `setCssAlignContent` | `FLEX_START`, `FLEX_END`, `CENTER`, `SPACE_BETWEEN`, `SPACE_AROUND`, `STRETCH` | Multi-line cross axis alignment |
| `setMainAxisGap(int)` | px | Main axis gap |
| `setCrossAxisGap(int)` | px | Cross axis gap |

#### CssFlexConstraints Properties

| Method | Default | Description |
|---|---|---|
| `flexGrow(float)` | `0` | Extra space distribution ratio |
| `flexShrink(float)` | `1` | Shrink ratio when space is insufficient |
| `flexBasisPercent(float)` | `-1` | Initial size (%). -1 uses preferredSize |
| `alignSelf(CssAlignSelf)` | `AUTO` | Per-item cross axis alignment |
| `order(int)` | `0` | Display order |
| `minWidth(int)`, `maxWidth(int)` | — | Width min/max constraints |
| `minHeight(int)`, `maxHeight(int)` | — | Height min/max constraints |
| `margin(top, left, bottom, right)` | — | Margin |

---

### CssGridLayout

A layout equivalent to CSS Grid. Supports row/column templates (`fixed` / `fr` / `auto`), spanning, and per-cell alignment.

```java
import org.to0mi1.swuit.layout.grid.*;
```

**Basic grid layout:**

```java
CssGridLayout layout = new CssGridLayout()
    .setColumnTemplate(CssTrackSize.fixed(100), CssTrackSize.fr(1), CssTrackSize.fr(2))
    .setRowTemplate(CssTrackSize.fixed(50), CssTrackSize.fr(1))
    .setColumnGap(8)
    .setRowGap(8);

JPanel panel = new JPanel(layout);

// header: row 0, spanning 3 columns
panel.add(header, new CssGridConstraints().column(0).row(0).columnSpan(3));

// sidebar: row 1, column 0
panel.add(sidebar, new CssGridConstraints().column(0).row(1));

// content: row 1, columns 1-2
panel.add(content, new CssGridConstraints().column(1).row(1).columnSpan(2));
```

**Per-cell alignment:**

```java
panel.add(child, new CssGridConstraints()
    .column(0).row(0)
    .justifySelf(CssJustifySelf.CENTER)
    .alignSelf(CssAlignSelf.CENTER));
```

**Auto-placement (omitting position):**

```java
// When column/row are not specified, items are placed automatically in empty cells
for (int i = 0; i < 9; i++) {
    panel.add(new JButton("" + i));
}
```

#### CssTrackSize (Track Size Definition)

| Method | Description |
|---|---|
| `CssTrackSize.fixed(int px)` | Fixed pixel size |
| `CssTrackSize.fr(float fraction)` | Fractional unit for distributing remaining space (CSS `fr` unit) |
| `CssTrackSize.auto()` | Size to fit content's preferredSize |

#### CssGridLayout Properties

| Method | Description |
|---|---|
| `setColumnTemplate(CssTrackSize...)` | Column track definitions |
| `setRowTemplate(CssTrackSize...)` | Row track definitions |
| `setColumnGap(int)` | Column gap (px) |
| `setRowGap(int)` | Row gap (px) |
| `setCssJustifyItems(CssJustifyItems)` | Horizontal alignment for all cells (`START`, `END`, `CENTER`, `STRETCH`) |
| `setCssAlignItems(CssAlignItems)` | Vertical alignment for all cells (`START`, `END`, `CENTER`, `STRETCH`) |

#### CssGridConstraints Properties

| Method | Default | Description |
|---|---|---|
| `column(int)` | `-1` | Column position (-1 for auto-placement) |
| `row(int)` | `-1` | Row position (-1 for auto-placement) |
| `columnSpan(int)` | `1` | Number of columns to span |
| `rowSpan(int)` | `1` | Number of rows to span |
| `justifySelf(CssJustifySelf)` | `AUTO` | Per-cell horizontal alignment |
| `alignSelf(CssAlignSelf)` | `AUTO` | Per-cell vertical alignment |
| `margin(top, left, bottom, right)` | — | Margin |

---

### AspectRatioLayout

A layout that maintains the aspect ratio of a single child component. Equivalent to CSS `aspect-ratio`.

```java
import org.to0mi1.swuit.layout.aspectratio.AspectRatioLayout;
```

```java
// Maintain a 16:9 aspect ratio
JPanel container = new JPanel(new AspectRatioLayout(16.0 / 9.0));
container.add(child);
```

The constructor argument is the aspect ratio (width / height). Height is automatically calculated from the child's `preferredWidth`.

---

## Component Usage

### RecyclerPane

A component equivalent to Android's `RecyclerView` that efficiently displays large datasets with view recycling.

```java
import org.to0mi1.swuit.component.recycler.*;
import org.to0mi1.swuit.layout.Orientation;
```

**Basic usage:**

```java
// 1. Define a ViewHolder
class MyViewHolder extends RecyclerPane.ViewHolder {
    JLabel label;
    MyViewHolder() {
        super(new JLabel());
        label = (JLabel) itemView;
    }
}

// 2. Define an Adapter
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

// 3. Assemble the RecyclerPane
RecyclerPane recycler = new RecyclerPane();
recycler.setLayoutManager(new LinearLayoutManager(Orientation.VERTICAL));
recycler.setAdapter(new MyAdapter(List.of("Apple", "Banana", "Cherry")));

JScrollPane scrollPane = new JScrollPane(recycler);
```

#### LayoutManager Types

| Class | Description |
|---|---|
| `LinearLayoutManager` | Vertical or horizontal single-row/column layout |
| `GridLayoutManager` | Grid layout with a specified number of columns |
| `StaggeredGridLayoutManager` | Variable-height tile layout (Pinterest-style) |

```java
// Grid (3 columns)
recycler.setLayoutManager(new GridLayoutManager(3));

// Staggered grid (2 columns)
recycler.setLayoutManager(new StaggeredGridLayoutManager(2));
```

#### Data Change Notifications

```java
adapter.notifyDataSetChanged();              // Full refresh
adapter.notifyItemInserted(index);           // Insertion
adapter.notifyItemRemoved(index);            // Removal
adapter.notifyItemChanged(index);            // Single-item differential update
adapter.notifyItemRangeChanged(start, n);    // Range differential update
```

`notifyItemChanged` / `notifyItemRangeChanged` perform a true differential update:
the visible ViewHolder for the target position is rebound via `onBindViewHolder`
in place, and any stale Cache entries for that position are evicted. Unless the
item's `preferredSize` changes, no full relayout is triggered, so the scroll
position is preserved.

For cases where the differential path is not enough (e.g. you need the
LayoutManager to invalidate its size cache), use the `force` overload to fall
back to a full relayout equivalent to `notifyDataSetChanged()`:

```java
adapter.notifyItemChanged(index, true);              // full relayout
adapter.notifyItemRangeChanged(start, n, true);      // same as above
```

To grab the currently-displayed ViewHolder for a position directly:

```java
RecyclerPane.ViewHolder vh = recycler.findViewHolderForAdapterPosition(index);
```

#### ItemDecoration

```java
recycler.addItemDecoration(new RecyclerPane.ItemDecoration() {
    @Override
    public Insets getItemOffsets(int position) {
        return new Insets(4, 4, 4, 4);  // Spacing between items
    }

    @Override
    public void onDraw(Graphics g, RecyclerPane parent) {
        // Draw on the background layer (e.g., dividers)
    }
});
```

---

### VirtualScrollPane

A scroll pane that optimizes rendering performance for panels with a large number of child components.
Components outside the visible area are set to `setVisible(false)` to skip rendering.

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

**Setting the buffer zone:**

```java
// Include components up to 100px outside the visible area (default: 50px)
scrollPane.setBufferZone(100);
```

Can be used as a drop-in replacement for `JScrollPane`. Works with any existing layout manager.

---

### ImageView

An image component equivalent to CSS `object-fit` / `object-position`.

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

#### ObjectFit (Rendering Mode)

| Value | Description |
|---|---|
| `FILL` | Stretch to fill the container, ignoring aspect ratio |
| `CONTAIN` | Scale to fit within the container while maintaining aspect ratio |
| `COVER` | Scale to cover the entire container while maintaining aspect ratio |
| `NONE` | Render at the image's intrinsic size |
| `SCALE_DOWN` | The smaller of `NONE` and `CONTAIN` (only scales down, never up) |

#### ObjectPosition (Rendering Position)

| Constant | Value | Description |
|---|---|---|
| `ObjectPosition.CENTER` | `(0.5, 0.5)` | Center (default) |
| `ObjectPosition.TOP_LEFT` | `(0.0, 0.0)` | Top-left |
| `ObjectPosition.BOTTOM_RIGHT` | `(1.0, 1.0)` | Bottom-right |
| `new ObjectPosition(x, y)` | `(0.0-1.0)` | Custom position |

**Combined with AspectRatioLayout:**

```java
JPanel container = new JPanel(new AspectRatioLayout(16.0 / 9.0));
ImageView view = new ImageView(myImage);
view.setObjectFit(ObjectFit.COVER);
container.add(view);
```

---

### AutoCompleteComboBox

A ComboBox with autocomplete that supports both filtering (dropdown narrowing) and suggestion (inline completion).

```java
import org.to0mi1.swuit.component.autocomplete.AutoCompleteComboBox;
import org.to0mi1.swuit.component.autocomplete.ContainsMatcher;
```

**Basic usage:**

```java
var combo = new AutoCompleteComboBox<>(List.of("Apple", "Banana", "Cherry", "Date"));
```

**Switch to contains matching (case-insensitive):**

```java
combo.setMatcher(new ContainsMatcher<>(true));
```

**Filter only (no inline suggestion):**

```java
combo.setSuggestEnabled(false);
```

**Using custom objects:**

```java
record Fruit(String name, int price) {}

var combo = new AutoCompleteComboBox<>(List.of(
    new Fruit("Apple", 100),
    new Fruit("Banana", 80)
));
combo.setStringConverter(Fruit::name);
```

#### AutoCompleteComboBox Properties

| Method | Default | Description |
|---|---|---|
| `setMatcher(AutoCompleteMatcher)` | `StartsWithMatcher` | Matching strategy |
| `setStringConverter(Function)` | `String::valueOf` | Function to convert items to display strings |
| `setFilterEnabled(boolean)` | `true` | Enable dropdown filtering |
| `setSuggestEnabled(boolean)` | `true` | Enable inline suggestion |
| `setSuggestIgnoreCase(boolean)` | `false` | Ignore case in suggestions |
| `setPopupOnInput(boolean)` | `true` | Automatically show popup on input |

#### Matcher Types

| Class | Description |
|---|---|
| `StartsWithMatcher` | Prefix matching (default) |
| `ContainsMatcher` | Substring matching |

Both accept `ignoreCase` in their constructor.

---

## Common Classes

### Gravity

Bit-flag constants equivalent to Android's `Gravity`. Used with `LinearLayout` and `RelativeLayout`.

| Constant | Description |
|---|---|
| `Gravity.LEFT` | Align left |
| `Gravity.CENTER_HORIZONTAL` | Center horizontally |
| `Gravity.RIGHT` | Align right |
| `Gravity.FILL_HORIZONTAL` | Stretch horizontally |
| `Gravity.TOP` | Align top |
| `Gravity.CENTER_VERTICAL` | Center vertically |
| `Gravity.BOTTOM` | Align bottom |
| `Gravity.FILL_VERTICAL` | Stretch vertically |
| `Gravity.CENTER` | Center both horizontally and vertically |
| `Gravity.FILL` | Stretch both horizontally and vertically |

### Orientation

An enum representing the main axis direction of a layout.

| Value | Description |
|---|---|
| `Orientation.HORIZONTAL` | Horizontal (left to right) |
| `Orientation.VERTICAL` | Vertical (top to bottom) |

---

## Requirements

- Java 17 or later

## For Developers

### Project Structure

| Module | Description |
|---|---|
| `swuit-core` | Layout & component library |
| `demo-common` | Shared panel generation logic for demos |
| `demo-swing` | Demo app using plain Swing |
| `demo-flatlaf` | Demo app with FlatLaf look and feel |

### Build

```bash
./gradlew build
```

### Running Demos

```bash
# Plain Swing
./gradlew :demo-swing:run

# FlatLaf
./gradlew :demo-flatlaf:run
```
