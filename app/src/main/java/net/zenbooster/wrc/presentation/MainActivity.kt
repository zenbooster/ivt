/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package net.zenbooster.wrc.presentation

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.os.Bundle
import android.content.Context
import android.content.Context.POWER_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.os.CountDownTimer
import android.os.PowerManager
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import androidx.wear.compose.material.PickerState
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.rememberPickerGroupState
import androidx.wear.compose.material.rememberPickerState
import net.zenbooster.wrc.R
import net.zenbooster.wrc.presentation.theme.WrcTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.math.ceil
import kotlin.math.round
import kotlin.time.Duration
import android.os.Vibrator
import android.os.VibrationEffect;
import androidx.core.content.ContextCompat.getSystemService

class Core {
    companion object {
        var btnEnabled = mutableStateOf(false)
        var btnChecked = mutableStateOf(false)
        var timer: CountDownTimer? = null;
        var s_time = mutableStateOf("00:00:00")
        val def_timerColor = Color.Green
        var VibrationLevel = mutableStateOf(255f)
        var VibrationDuration = mutableStateOf(375f)
        var timerColor = mutableStateOf(Core.def_timerColor)

        var totalSeconds:Long = 0;
        var mysvcIntent: Intent = Intent()

        fun init(ctx : Context?) {
            if(ctx != null) {
                mysvcIntent.setClass(ctx, MyService::class.java)
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
        super.onPause()
    }
    override fun onDestroy() {
        super.onDestroy()
    }
}

@Composable
fun WearApp(ctx: Context?) {

    Core.init(ctx)

    WrcTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()

            val pickerGroupState = rememberPickerGroupState()
            val pickerStateH = rememberPickerState(initialNumberOfOptions = 24)
            val pickerStateM = rememberPickerState(initialNumberOfOptions = 60)
            val pickerStateS = rememberPickerState(initialNumberOfOptions = 60)

            val btcap = listOf("▶", "❚❚")
            val btnEnabled : MutableState<Boolean> = Core.btnEnabled
            val btnChecked : MutableState<Boolean> = Core.btnChecked
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
                                pickerState = pickerStateH,
                                option = { optionIndex, _ -> Text(text = "%02d".format(optionIndex)) },
                                modifier = Modifier.size(40.dp, 100.dp)
                            ),
                            PickerGroupItem(
                                pickerState = pickerStateM,
                                option = { optionIndex, _ -> Text(text = "%02d".format(optionIndex)) },
                                modifier = Modifier.size(40.dp, 100.dp)
                            ),
                            PickerGroupItem(
                                pickerState = pickerStateS,
                                option = { optionIndex, _ -> Text(text = "%02d".format(optionIndex)) },
                                modifier = Modifier.size(40.dp, 100.dp)
                            ),
                            pickerGroupState = pickerGroupState,
                            autoCenter = false
                        )

                        LaunchedEffect(
                            pickerStateH.selectedOption,
                            pickerStateM.selectedOption,
                            pickerStateS.selectedOption
                        ) {
                            btnEnabled.value = pickerStateH.selectedOption > 0 ||
                                    pickerStateM.selectedOption > 0 ||
                                    pickerStateS.selectedOption > 0

                        }
                    } else {
                        Text(
                            text = Core.s_time.value,
                            fontWeight = FontWeight.Bold,
                            color = timerColor.value
                        )
                    }
                }

                Button(
                    enabled = btnEnabled.value,
                    onClick = {
                        fun StartMainWork() {
                            Core.totalSeconds = pickerStateS.selectedOption +
                                    pickerStateM.selectedOption * 60L +
                                    pickerStateH.selectedOption * 3600L;

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