package com.mdrlzy.counterslider

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.mdrlzy.counterslider.ui.theme.CounterSliderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CounterSliderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        var valueCounter by remember {
                            mutableStateOf(0)
                        }
                        HorizontalCounterSlider(
                            size = DpSize(300.dp, 50.dp),
                            value = valueCounter.toString(),
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
                        var valueCounter2 by remember {
                            mutableStateOf(0)
                        }
                        VerticalCounterSlider(
                            size = DpSize(80.dp, 200.dp),
                            value = valueCounter2.toString(),
                            onValueIncreaseClick = {
                                valueCounter2 += 1
                            },
                            onValueDecreaseClick = {
                                valueCounter2 = maxOf(valueCounter2 - 1, 0)
                            },
                            onValueClearClick = {
                                valueCounter2 = 0
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CounterSliderTheme {
        Greeting("Android")
    }
}