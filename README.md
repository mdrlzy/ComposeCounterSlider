# ComposeCounterSlider
[![](https://jitpack.io/v/mdrlzy/ComposeCounterSlider.svg)](https://jitpack.io/#mdrlzy/ComposeCounterSlider)

Fork of this [article](https://medium.com/@domen.lanisnik/creating-an-animated-counter-button-in-jetpack-compose-444d03129510) with refactoring and improvements such as custom size, colors, vertical variant and more.


https://github.com/user-attachments/assets/45ac77e0-58a4-418b-a8d2-0eb075012a48


### Installation

- Update your `settings.gradle`
```
dependencyResolutionManagement {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

- Update your module level `build.gradle`. Check latest version on the [tags page](https://github.com/mdrlzy/ComposeCounterSlider/tags)
```
dependencies {
    implementation 'com.github.mdrlzy:ComposeCounterSlider:$version'
}
```

### Usage
```kotlin
var valueCounter by remember {
    mutableStateOf(0)
}
HorizontalCounterSlider(
    modifier = Modifier,
    size = DpSize(200.dp, 80.dp),
    value = valueCounter.toString(),
    allowTopToReset = true,
    allowBottomToReset = true,
    customization = CounterSliderCustomization(),
    colors = CounterSliderColors(),
    onValueIncreaseClick = {
        valueCounter += 1
    },
    onValueDecreaseClick = {
        valueCounter = maxOf(valueCounter - 1, 0)
    },
    onValueClearClick = {
        valueCounter = 0
    }
)
```
