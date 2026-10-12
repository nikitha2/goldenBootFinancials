# GoldenbootTheme — Design Token Reference

This documents the design tokens used by the GoldenbootTheme.

## Source

Using default design tokens. To customize, provide a Figma URL when creating the project.

## Target Files

| File | What it contains |
|------|-----------------|
| `core/src/commonMain/kotlin/com/goldenboot/goldenbootfinancial/core/theme/Color.kt` | `GoldenbootColors` reference palette + light/dark semantic color mappings |
| `core/src/commonMain/kotlin/com/goldenboot/goldenbootfinancial/core/theme/Shape.kt` | Border radius tokens → Material 3 `Shapes` |
| `core/src/commonMain/kotlin/com/goldenboot/goldenbootfinancial/core/theme/Spacing.kt` | Spacing tokens → `GoldenbootSpacingValues` via `LocalSpacing` (also used for elevation) |
| `core/src/commonMain/kotlin/com/goldenboot/goldenbootfinancial/core/theme/AppBarStyling.kt` | Shared styling for TopAppBar and BottomNavigationBar (containerColor, shadow, tonalElevation) |
| `core/src/commonMain/kotlin/com/goldenboot/goldenbootfinancial/core/theme/Type.kt` | Typography (using `FontFamily.Default`) |
| `core/src/commonMain/kotlin/com/goldenboot/goldenbootfinancial/core/theme/GoldenbootTheme.kt` | Composes all tokens into `MaterialTheme` + `CompositionLocalProvider` |

## Color Mapping

### Reference Palette (`GoldenbootColors` object)

Contains full color palettes: Blue, Slate, Gray, Red, Green, Yellow, Orange, Indigo, Cyan, Violet, Pink, Grape, Lime.

Example: `GoldenbootColors.Blue500 = Color(0xFF106BC7)`

### Semantic → Material 3 Mapping (Light)

| Semantic Token | Color | Material 3 Role |
|---------------|-------|-----------------|
| background-primary (Blue500) | #106BC7 | `primary` |
| text-inverse (White) | #FFFFFF | `onPrimary` |
| background-primary-subtle (Blue100) | #E7F5FF | `primaryContainer` |
| text-strong (Blue800) | #0A2237 | `onPrimaryContainer` |
| background-strong (Blue700) | #0D375E | `secondary` |
| background-primary-weak (Blue200) | #A5D8FF | `secondaryContainer` |
| background-warning (Yellow400) | #FAB005 | `tertiary` |
| background-warning-subtle (Orange100) | #FFF4E6 | `tertiaryContainer` |
| canvas-default (White) | #FFFFFF | `background`, `surface` |
| text-default (Slate600) | #475569 | `onBackground`, `onSurface` |
| background-muted-subtle (Slate200) | #E2E8F0 | `surfaceVariant` |
| text-weak (Slate500) | #64748B | `onSurfaceVariant` |
| background-critical (Red500) | #E03131 | `error` |
| border-default (Slate400) | #8496AE | `outline` |
| border-subtle (Slate200) | #E2E8F0 | `outlineVariant` |

### Semantic → Material 3 Mapping (Dark)

Same structure with shifted values for dark backgrounds:
- Primary shifts lighter (Blue300 → Blue700 containers)
- Background/surface use Slate800
- Text uses Slate200 (lighter for dark backgrounds)

## Shape Mapping

| Token | Value | Material 3 Shape |
|-------|-------|-------------------|
| border-radius-sm | 4dp | `extraSmall` |
| border-radius-md | 8dp | `small` |
| border-radius-lg | 12dp | `medium` |
| border-radius-xl | 16dp | `large` |
| border-radius-2xl | 24dp | `extraLarge` |
| border-radius-round | 50% | `PillShape` (standalone) |

## Spacing Mapping

| Token | Value | Kotlin |
|-------|-------|--------|
| spacing-none | 0dp | `GoldenbootSpacing.none` |
| spacing-xs | 2dp | `GoldenbootSpacing.xs` |
| spacing-sm | 4dp | `GoldenbootSpacing.sm` |
| spacing-md | 8dp | `GoldenbootSpacing.md` |
| spacing-lg | 12dp | `GoldenbootSpacing.lg` |
| spacing-xl | 16dp | `GoldenbootSpacing.xl` |
| spacing-2xl | 24dp | `GoldenbootSpacing.xxl` |
| spacing-3xl | 32dp | `GoldenbootSpacing.xxxl` |
| spacing-4xl | 40dp | `GoldenbootSpacing.xxxxl` |
| spacing-5xl | 48dp | `GoldenbootSpacing.xxxxxl` |

## How to Customize

To use custom design tokens, provide a Figma URL or JSON token file when creating the project (question 11).
The theme generator will extract colors, spacing, and shapes from the provided source.
If no design tokens are provided, these defaults are used.
