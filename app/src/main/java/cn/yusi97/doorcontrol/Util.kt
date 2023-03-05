package cn.yusi97.doorcontrol

import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.res.Resources
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

object VibrateUtils {

    //震动milliseconds毫秒
    fun vibrate(context: Context, milliseconds: Long) {
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        vib.vibrate(milliseconds)
    }

    /**
     * 以pattern[]方式震动
     * @param repeat -1 不重复  0一直震动
     */
    fun vibrate(context: Context, pattern: LongArray, repeat: Int) {
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        vib.vibrate(pattern, repeat)
    }

    //取消震动
    fun vibrateCancel(context: Context) {
        val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            context.getSystemService(VIBRATOR_SERVICE) as Vibrator
        }
        try {
            vib.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
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