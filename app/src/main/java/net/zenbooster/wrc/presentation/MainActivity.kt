/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package net.zenbooster.wrc.presentation

import android.os.Bundle
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Card
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PickerGroup
import androidx.wear.compose.material.PickerGroupItem
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.rememberPickerGroupState
import androidx.wear.compose.material.rememberPickerState
import net.zenbooster.wrc.R
import net.zenbooster.wrc.presentation.theme.WrcTheme

class Core {
    companion object {
        var btnChecked = mutableStateOf(false)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            WearApp(this)
        }
    }
}

@Composable
fun WearApp(ctx: Context?) {
    WrcTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()

            val pickerGroupState = rememberPickerGroupState()
            val pickerStateHour = rememberPickerState(initialNumberOfOptions = 24)
            val pickerStateMinute = rememberPickerState(initialNumberOfOptions = 60)
            val pickerStateSeconds = rememberPickerState(initialNumberOfOptions = 60)

            val btcap = listOf("▶", "❚❚")
            val btnChecked : MutableState<Boolean> = Core.btnChecked

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if(!btnChecked.value) {
                        val headingText = mapOf(0 to "часы", 1 to "минуты", 2 to "секунды", 3 to "Milli")
                        Spacer(modifier = Modifier.size(30.dp))
                        Text(text = headingText[pickerGroupState.selectedIndex]!!)
                        Spacer(modifier = Modifier.size(10.dp))

                        PickerGroup(
                            PickerGroupItem(
                                pickerState = pickerStateHour,
                                option = { optionIndex, _ -> Text(text = "%02d".format(optionIndex)) },
                                modifier = Modifier.size(40.dp, 100.dp)
                            ),
                            PickerGroupItem(
                                pickerState = pickerStateMinute,
                                option = { optionIndex, _ -> Text(text = "%02d".format(optionIndex)) },
                                modifier = Modifier.size(40.dp, 100.dp)
                            ),
                            PickerGroupItem(
                                pickerState = pickerStateSeconds,
                                option = { optionIndex, _ -> Text(text = "%02d".format(optionIndex)) },
                                modifier = Modifier.size(40.dp, 100.dp)
                            ),
                            pickerGroupState = pickerGroupState,
                            autoCenter = false
                        )
                    } else {
                        Text("00:00:00")
                    }
                }

                Button(
                    enabled = true,
                    //shape = RoundedCornerShape(size = 5.dp),
                    onClick = {
                        btnChecked.value = !btnChecked.value
                    },
                    modifier = Modifier
                        .padding(top = 1.dp, bottom = 5.dp)
                        //.size(ButtonDefaults.SmallIconSize)
                        .height(20.dp)
                )
                {
                    Text(btcap[if (btnChecked.value) 1 else 0])
                }
            } // Column
        }
    }
}

@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp(null)
}