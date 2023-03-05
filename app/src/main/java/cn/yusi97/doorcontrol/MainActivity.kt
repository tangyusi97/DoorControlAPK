package cn.yusi97.doorcontrol

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import cn.yusi97.doorcontrol.ui.theme.AppTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController


var hasAdvertisePermission by mutableStateOf(false)
var isBluetoothAdapterEnabled by mutableStateOf(false)

var bluetoothAdapter: BluetoothAdapter? = null
var bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null
lateinit var advertiseSettings: AdvertiseSettings
lateinit var advertiseCallback: AdvertiseCallback

class MainActivity : ComponentActivity() {
    private val ctx = this
    private var advertiseDenied = false
    private var openBluetoothDenied = false

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        it.forEach { (permission, isGranted) ->
            when (permission) {
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH -> {
                    hasAdvertisePermission = isGranted
                    if (!isGranted) {
                        advertiseDenied = true
                        Toast.makeText(this, getString(R.string.no_permission), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADMIN -> {
                    //
                }
            }
        }
    }

    private val openBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        isBluetoothAdapterEnabled = it.resultCode == Activity.RESULT_OK
        if (it.resultCode == Activity.RESULT_CANCELED) openBluetoothDenied = true
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
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

    override fun onStart() {
        super.onStart()
        advertiseSettings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setTimeout(0)
            .setConnectable(false)
            .build()
        advertiseCallback = advertiseCallback1()
    }

    override fun onResume() {
        super.onResume()
        checkAdvertisePermission { setUpBluetooth() }
    }

    private fun setUpBluetooth() {
        hasAdvertisePermission = true

        bluetoothAdapter = getSystemService(BluetoothManager::class.java).adapter

        // 检测蓝牙
        if (bluetoothAdapter == null) {
            Toast.makeText(this, getString(R.string.no_bluetooth), Toast.LENGTH_SHORT).show()
            hasAdvertisePermission = false
            return
        }
        // 初始化广播器
        bluetoothLeAdvertiser = bluetoothAdapter!!.bluetoothLeAdvertiser
        // 打开蓝牙
        if (!bluetoothAdapter!!.isEnabled) {
            if (!openBluetoothDenied) {
                checkOpenBluetoothPermission {
                    val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    openBluetoothLauncher.launch(enableBluetoothIntent)
                }
            }
        } else {
            isBluetoothAdapterEnabled = true
        }
    }

    private fun checkAdvertisePermission(onPass: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.BLUETOOTH_ADVERTISE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                onPass()
            } else if (!advertiseDenied) {
                requestPermissionLauncher.launch(
                    arrayOf(Manifest.permission.BLUETOOTH_ADVERTISE)
                )
            }
        } else {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.BLUETOOTH
                ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.BLUETOOTH_ADMIN
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                onPass()
            } else if (!advertiseDenied) {
                requestPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN
                    )
                )
            }
        }
    }

    private fun checkOpenBluetoothPermission(onPass: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                onPass()
            } else {
                requestPermissionLauncher.launch(
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT)
                )
            }
        } else {
            onPass()
        }
    }

    private fun advertiseCallback1() = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Toast.makeText(ctx, getString(R.string.advertise_failed), Toast.LENGTH_SHORT).show()
        }
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

    fun enableAll(enabled: Boolean) {
        for (j in 0 until enabledList.size) {
            enabledList[j] = enabled
        }
    }

    fun setEnabled(i: Int) {
        enableAll(false)
        enabledList[i] = true
    }

    LaunchedEffect(hasAdvertisePermission, isBluetoothAdapterEnabled) {
        enableAll(hasAdvertisePermission && isBluetoothAdapterEnabled)
    }

    ControlButton(
        stringResource(R.string.open),
        enabledList[0],
        { setEnabled(0) },
        { enableAll(true) },
        DoorActionCmd.open
    )
    ControlButton(
        stringResource(R.string.stop),
        enabledList[1],
        { setEnabled(1) },
        { enableAll(true) },
        DoorActionCmd.stop
    )
    ControlButton(
        stringResource(R.string.close),
        enabledList[2],
        { setEnabled(2) },
        { enableAll(true) },
        DoorActionCmd.close
    )
    ControlButton(
        stringResource(R.string.open_and_close),
        enabledList[3],
        { setEnabled(3) },
        { enableAll(true) },
        DoorActionCmd.openAndClose,
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
    val ctx = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressState by interactionSource.collectIsPressedAsState()
    DisposableEffect(pressState) {
        if (bluetoothAdapter?.isEnabled != true) {
            isBluetoothAdapterEnabled = false
        } else if (pressState) {
            setEnabled()
            VibrateUtils.vibrate(ctx, longArrayOf(0, 50, 250), 0)
            val data = AdvertiseData.Builder().addManufacturerData(0xFFF0, cmdData).build()
            if (ActivityCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                bluetoothLeAdvertiser?.startAdvertising(advertiseSettings, data, advertiseCallback)
            }

        } else {
            resetEnabled()
            VibrateUtils.vibrateCancel(ctx)
            bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        }

        onDispose {
            VibrateUtils.vibrateCancel(ctx)
            bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
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


