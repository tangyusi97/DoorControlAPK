package cn.yusi97.doorcontrol

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

val Number.vh: Dp
    get() {
        val display = Resources.getSystem().displayMetrics
        val height = display.heightPixels / display.scaledDensity
        return (this.toInt() * height / 1000).dp
    }

val Number.th: TextUnit
    get() {
        val display = Resources.getSystem().displayMetrics
        val height = display.heightPixels / display.scaledDensity
        return (this.toInt() * height / 800).sp
    }

data class Hardware(val myBleAdvertiser: MyBleAdvertiser, val myVibrator: MyVibrator)

@Suppress("deprecation")
class MyVibrator(context: Context) {

    private val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        context.getSystemService(VIBRATOR_SERVICE) as Vibrator
    }

    fun vibrate(milliseconds: Long) {
        vib.vibrate(milliseconds)
    }

    /**
     * 以pattern[]方式震动
     * @param repeat -1 不重复  0一直震动
     */
    fun vibrate(pattern: LongArray, repeat: Int) {
        vib.vibrate(pattern, repeat)
    }

    fun cancel() {
        try {
            vib.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

enum class MyBleAdvertiserEvent {
    ADVERTISE_PERMISSION, OPEN_BLE
}

class MyBleAdvertiser(context: Context) {
    private val ctx = context.applicationContext

    private val bluetoothAdapter: BluetoothAdapter? =
        ctx.getSystemService(BluetoothManager::class.java)?.adapter

    private var bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null

    private val advertiseSettings: AdvertiseSettings = AdvertiseSettings.Builder()
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
        .setTimeout(0)
        .setConnectable(false)
        .build()

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Toast.makeText(ctx, ctx.getString(R.string.advertise_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private var advertiseDenied = false
    private var openBluetoothDenied = false
    private var eventHandler: (MyBleAdvertiserEvent, Boolean) -> Unit = { _, _ -> }
    private var requestPermissionLauncher: ActivityResultLauncher<Array<String>>? = null
    private var openBluetoothLauncher: ActivityResultLauncher<Intent>? = null

    init {
        setUp()
    }

    constructor(
        activity: ComponentActivity,
        eventHandler: (event: MyBleAdvertiserEvent, value: Boolean) -> Unit
    ) : this(activity) {
        this.eventHandler = eventHandler
        setRequestPermissionLauncher(activity)
        setOpenBluetoothLauncher(activity)
    }

    fun setUp() {
        // 检测蓝牙支持
        if (bluetoothAdapter == null) {
            eventHandler(MyBleAdvertiserEvent.OPEN_BLE, false)
            Toast.makeText(ctx, ctx.getString(R.string.no_bluetooth), Toast.LENGTH_SHORT).show()
            return
        }
        // 检查广播权限
        if (!checkAdvertisePermission(!advertiseDenied)) {
            eventHandler(MyBleAdvertiserEvent.ADVERTISE_PERMISSION, false)
            return
        }
        eventHandler(MyBleAdvertiserEvent.ADVERTISE_PERMISSION, true)
        // 打开蓝牙开关
        if (!bluetoothAdapter.isEnabled) {
            eventHandler(MyBleAdvertiserEvent.OPEN_BLE, false)
            if (!openBluetoothDenied && checkOpenBluetoothPermission()) {
                val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                openBluetoothLauncher?.launch(enableBluetoothIntent)
            }
            return
        }
        eventHandler(MyBleAdvertiserEvent.OPEN_BLE, true)
        // 初始化广播器
        bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser
    }

    fun advertiseCommand(cmdData: ByteArray) {
        if (bluetoothAdapter?.isEnabled != true) {
            eventHandler(MyBleAdvertiserEvent.OPEN_BLE, false)
            Toast.makeText(ctx, ctx.getString(R.string.no_bluetooth), Toast.LENGTH_SHORT).show()
            return
        }
        val data = AdvertiseData.Builder().addManufacturerData(0xFFF0, cmdData).build()
        if (!checkAdvertisePermission(false)) {
            eventHandler(MyBleAdvertiserEvent.ADVERTISE_PERMISSION, false)
            Toast.makeText(ctx, ctx.getString(R.string.no_permission), Toast.LENGTH_SHORT).show()
        }
        bluetoothLeAdvertiser?.startAdvertising(advertiseSettings, data, advertiseCallback)
    }

    fun advertiseCancel() {
        if (!checkAdvertisePermission(false)) {
            eventHandler(MyBleAdvertiserEvent.ADVERTISE_PERMISSION, false)
        }
        bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
    }

    private fun checkAdvertisePermission(request: Boolean? = false): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    ctx, Manifest.permission.BLUETOOTH_ADVERTISE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                return true
            } else if (request == true) {
                requestPermissionLauncher?.launch(
                    arrayOf(Manifest.permission.BLUETOOTH_ADVERTISE)
                )
            }
            return false
        }
        if (ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                ctx, Manifest.permission.BLUETOOTH_ADMIN
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        } else if (request == true) {
            requestPermissionLauncher?.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
                )
            )
        }
        return false
    }

    private fun checkOpenBluetoothPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    ctx, Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                return true
            }
            requestPermissionLauncher?.launch(
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT)
            )
            return false
        }
        return true
    }

    private fun setRequestPermissionLauncher(activity: ComponentActivity) {
        requestPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            it.forEach { (permission, isGranted) ->
                when (permission) {
                    Manifest.permission.BLUETOOTH_ADVERTISE,
                    Manifest.permission.BLUETOOTH -> {
                        if (!isGranted) {
                            advertiseDenied = true
                            Toast.makeText(
                                ctx,
                                ctx.getString(R.string.no_permission),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADMIN -> {
                        //
                    }
                }
            }
        }
    }

    private fun setOpenBluetoothLauncher(activity: ComponentActivity) {
        openBluetoothLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_CANCELED) {
                openBluetoothDenied = true
                Toast.makeText(ctx, ctx.getString(R.string.no_bluetooth), Toast.LENGTH_SHORT).show()
            }
        }
    }
}

object DoorActionCmd {
    val open = byteArrayOf(
        0x6D.toByte(),
        0xB6.toByte(),
        0x43.toByte(),
        0x4F.toByte(),
        0x9E.toByte(),
        0x0F.toByte(),
        0x87.toByte(),
        0x91.toByte(),
        0x23.toByte(),
        0x6F.toByte(),
        0xCB.toByte(),
        0xCF.toByte(),
        0x65.toByte(),
        0xDA.toByte(),
        0x51.toByte(),
        0x3B.toByte()
    )
    val close = byteArrayOf(
        0x6D.toByte(),
        0xB6.toByte(),
        0x43.toByte(),
        0x4F.toByte(),
        0x9E.toByte(),
        0x0F.toByte(),
        0x87.toByte(),
        0x91.toByte(),
        0x23.toByte(),
        0x6F.toByte(),
        0xCB.toByte(),
        0xCF.toByte(),
        0x65.toByte(),
        0x7A.toByte(),
        0x5B.toByte(),
        0x9E.toByte()
    )
    val stop = byteArrayOf(
        0x6D.toByte(),
        0xB6.toByte(),
        0x43.toByte(),
        0x4F.toByte(),
        0x9E.toByte(),
        0x0F.toByte(),
        0x87.toByte(),
        0x91.toByte(),
        0x23.toByte(),
        0x6F.toByte(),
        0xCB.toByte(),
        0xCF.toByte(),
        0x65.toByte(),
        0xBA.toByte(),
        0x57.toByte(),
        0x58.toByte()
    )
    val openAndClose = byteArrayOf(
        0x6D.toByte(),
        0xB6.toByte(),
        0x43.toByte(),
        0x4F.toByte(),
        0x9E.toByte(),
        0x0F.toByte(),
        0x87.toByte(),
        0x91.toByte(),
        0x23.toByte(),
        0x6F.toByte(),
        0xCB.toByte(),
        0xCF.toByte(),
        0x65.toByte(),
        0x87.toByte(),
        0x5E.toByte(),
        0xA4.toByte()
    )
}