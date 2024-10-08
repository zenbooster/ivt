/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package net.zenbooster.ivt.presentation

import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PickerGroup
import androidx.wear.compose.material.PickerGroupItem
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.rememberPickerGroupState
import androidx.wear.compose.material.rememberPickerState
import net.zenbooster.ivt.presentation.theme.IvtTheme
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.PickerState

class Core {
    companion object {
        var sharedPref : SharedPreferences? = null
        var s_time = mutableStateOf("00:00:00")
        val def_timerColor = Color.Green
        var VibrationLevel = mutableStateOf(255f)
        var VibrationDuration = mutableStateOf(750f)
        var timerColor = mutableStateOf(Core.def_timerColor)

        var pickerStateH : PickerState? = null
        var pickerStateM : PickerState? = null
        var pickerStateS : PickerState? = null

        var totalSeconds:Long = 0;
        var mysvcIntent: Intent = Intent()

        fun init(ctx : Context?) {
            if(ctx != null) {
                sharedPref = ctx.getSharedPreferences("myPref", Context.MODE_PRIVATE)
                pickerStateH = PickerState(initialNumberOfOptions = 24, initiallySelectedOption = sharedPref!!.getInt("pkrH", 0))
                pickerStateM = PickerState(initialNumberOfOptions = 60, initiallySelectedOption = sharedPref!!.getInt("pkrM", 0))
                pickerStateS = PickerState(initialNumberOfOptions = 60, initiallySelectedOption = sharedPref!!.getInt("pkrS", 15))

                mysvcIntent.setClass(ctx, MyService::class.java)
            }
        }

        fun save() {
            if (sharedPref != null) {
                with(sharedPref!!.edit())
                {
                    putInt("pkrH", pickerStateH!!.selectedOption)
                    putInt("pkrM", pickerStateM!!.selectedOption)
                    putInt("pkrS", pickerStateS!!.selectedOption)

                    apply()
                }
            }
        }
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

    override fun onPause() {
        Core.save()
        super.onPause()
    }
}

@Composable
fun WearApp(ctx: Context?) {

    Core.init(ctx)

    IvtTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()

            val pickerGroupState = rememberPickerGroupState()
            val btcap = listOf("▶", "❚❚")
            val btnEnabled = remember { mutableStateOf(false) }
            val btnChecked = remember { mutableStateOf(false) }
            val timerColor : MutableState<Color> = Core.timerColor

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
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
                                pickerState = Core.pickerStateH!!,
                                option = { optionIndex, _ -> Text(text = "%02d".format(optionIndex)) },
                                modifier = Modifier.size(40.dp, 100.dp)
                            ),
                            PickerGroupItem(
                                pickerState = Core.pickerStateM!!,
                                option = { optionIndex, _ -> Text(text = "%02d".format(optionIndex)) },
                                modifier = Modifier.size(40.dp, 100.dp)
                            ),
                            PickerGroupItem(
                                pickerState = Core.pickerStateS!!,
                                option = { optionIndex, _ -> Text(text = "%02d".format(optionIndex)) },
                                modifier = Modifier.size(40.dp, 100.dp)
                            ),
                            pickerGroupState = pickerGroupState,
                            autoCenter = false
                        )

                        LaunchedEffect(
                            Core.pickerStateH!!.selectedOption,
                            Core.pickerStateM!!.selectedOption,
                            Core.pickerStateS!!.selectedOption
                        ) {
                            btnEnabled.value = Core.pickerStateH!!.selectedOption > 0 ||
                                    Core.pickerStateM!!.selectedOption > 0 ||
                                    Core.pickerStateS!!.selectedOption > 0

                        }
                    } else {
                        Text(
                            text = Core.s_time.value,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = timerColor.value
                        )
                    }
                }

                Button(
                    enabled = btnEnabled.value,
                    onClick = {
                        fun StartMainWork() {
                            Core.totalSeconds = Core.pickerStateS!!.selectedOption +
                                    Core.pickerStateM!!.selectedOption * 60L +
                                    Core.pickerStateH!!.selectedOption * 3600L;

                            ctx?.startForegroundService(
                                Core.mysvcIntent
                            )
                        }

                        fun StopMainWork() {
                            ctx?.stopService(Core.mysvcIntent)
                        }

                        if(btnChecked.value)
                        { // stop
                            StopMainWork();

                            btnChecked.value = false;
                        }
                        else
                        { // start
                            StartMainWork();

                            btnChecked.value = true;
                        }
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