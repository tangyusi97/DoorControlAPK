package cn.yusi97.doorcontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import cn.yusi97.doorcontrol.ui.theme.AppTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController


private var hasAdvertisePermission by mutableStateOf(false)
private var isBluetoothAdapterEnabled by mutableStateOf(false)

private lateinit var myBleAdvertiser: MyBleAdvertiser
private lateinit var myVibrator: MyVibrator

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        myVibrator = MyVibrator(this)
        myBleAdvertiser = MyBleAdvertiser(this) { event, value ->
            when (event) {
                MyBleAdvertiserEvent.ADVERTISE_PERMISSION -> {
                    hasAdvertisePermission = value
                }
                MyBleAdvertiserEvent.OPEN_BLE -> {
                    isBluetoothAdapterEnabled = value
                }
            }
        }

        setContent {
            AppTheme {
                FullScreen()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainView()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        myBleAdvertiser.setUp()
    }

}


@Composable
fun FullScreen() {
    val systemUiController = rememberSystemUiController()
    val useDarkIcons = !isSystemInDarkTheme()
    SideEffect {
        systemUiController.setSystemBarsColor(Color.Transparent, darkIcons = useDarkIcons)
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AppTheme {
        MainView()
    }
}

@Composable
fun MainView() {
    Column(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.height(300.vh), verticalArrangement = Arrangement.Center
        ) {
            Title()
        }
        Column(
            modifier = Modifier.height(550.vh), verticalArrangement = Arrangement.SpaceBetween
        ) {
            ControlPanel()
        }
        Column(
            modifier = Modifier.height(150.vh), verticalArrangement = Arrangement.Center
        ) {
            Text(text = stringResource(R.string.tip), fontSize = 16.th)
        }
    }
}

@Composable
fun Title() {
    Text(
        modifier = Modifier,
        text = stringResource(R.string.title),
        fontSize = 60.th,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
fun ControlPanel() {
    val enabledList = remember { mutableStateListOf(false, false, false, false) }
    var disabled by remember { mutableStateOf(false) }

    fun enableAll(enabled: Boolean) {
        for (j in 0 until enabledList.size) {
            enabledList[j] = enabled && !disabled
        }
    }

    fun setEnabled(i: Int) {
        enableAll(false)
        enabledList[i] = true && !disabled
    }

    LaunchedEffect(hasAdvertisePermission, isBluetoothAdapterEnabled) {
        disabled = !(hasAdvertisePermission && isBluetoothAdapterEnabled)
        enableAll(true)
    }

    ControlButton(
        name = stringResource(R.string.open),
        enabled = enabledList[0],
        setEnabled = { setEnabled(0) },
        resetEnabled = { enableAll(true) },
        cmdData = DoorActionCmd.open
    )
    ControlButton(
        name = stringResource(R.string.stop),
        enabled = enabledList[1],
        setEnabled = { setEnabled(1) },
        resetEnabled = { enableAll(true) },
        cmdData = DoorActionCmd.stop
    )
    ControlButton(
        name = stringResource(R.string.close),
        enabled = enabledList[2],
        setEnabled = { setEnabled(2) },
        resetEnabled = { enableAll(true) },
        cmdData = DoorActionCmd.close
    )
    ControlButton(
        name = stringResource(R.string.open_and_close),
        enabled = enabledList[3],
        setEnabled = { setEnabled(3) },
        resetEnabled = { enableAll(true) },
        cmdData = DoorActionCmd.openAndClose,
        true
    )
}

@Composable
fun ControlButton(
    name: String,
    enabled: Boolean,
    setEnabled: () -> Unit,
    resetEnabled: () -> Unit,
    cmdData: ByteArray,
    primary: Boolean? = false,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressState by interactionSource.collectIsPressedAsState()
    DisposableEffect(pressState) {
        if (pressState) {
            setEnabled()
            myBleAdvertiser.advertiseCommand(cmdData)
            myVibrator.vibrate(longArrayOf(0, 50, 250), 0)

        } else {
            resetEnabled()
            myBleAdvertiser.advertiseCancel()
            myVibrator.cancel()
        }

        onDispose {
            myBleAdvertiser.advertiseCancel()
            myVibrator.cancel()
        }
    }

    Button(modifier = Modifier
        .padding(horizontal = 60.dp)
        .fillMaxWidth()
        .height(120.vh),
        interactionSource = interactionSource,
        enabled = enabled,
        shape = if (primary == true) ButtonDefaults.shape else ButtonDefaults.filledTonalShape,
        colors = if (primary == true) ButtonDefaults.buttonColors() else ButtonDefaults.filledTonalButtonColors(),
        elevation = if (primary == true) ButtonDefaults.buttonElevation() else ButtonDefaults.filledTonalButtonElevation(),
        onClick = {}) {
        Text(
            modifier = Modifier,
            text = name,
            fontSize = 50.th,
            fontWeight = FontWeight.Bold,
        )
    }
}


