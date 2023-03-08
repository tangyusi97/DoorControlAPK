package cn.yusi97.doorcontrol

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.*
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

private val s1_1 = DpSize(40.dp, 50.dp)
private val s1_2 = DpSize(40.dp, 150.dp)
private val s1_3 = DpSize(40.dp, 250.dp)
private val s1_4 = DpSize(40.dp, 350.dp)
private val s2_1 = DpSize(120.dp, 50.dp)
private val s2_2 = DpSize(120.dp, 150.dp)
private val s2_3 = DpSize(120.dp, 250.dp)
private val s2_4 = DpSize(120.dp, 350.dp)
private val s3_1 = DpSize(200.dp, 50.dp)
private val s3_2 = DpSize(200.dp, 150.dp)
private val s3_3 = DpSize(200.dp, 250.dp)
private val s3_4 = DpSize(200.dp, 350.dp)
private val s4_1 = DpSize(280.dp, 50.dp)
private val s4_2 = DpSize(280.dp, 150.dp)
private val s4_3 = DpSize(280.dp, 250.dp)
private val s4_4 = DpSize(280.dp, 350.dp)

class ControlPanelWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            s1_1, s1_2, s1_3, s1_4, s2_1, s2_2, s2_3, s2_4,
            s3_1, s3_2, s3_3, s3_4, s4_1, s4_2, s4_3, s4_4
        )
    )

    @Composable
    override fun Content() {
        val width = LocalSize.current.width
        val height = LocalSize.current.height
        val paddingVertical = when (height) {
            s1_1.height -> 8.dp
            s1_2.height -> 16.dp
            s1_3.height -> 24.dp
            s1_4.height -> 32.dp
            else -> 40.dp
        }
        val paddingBorder = when {
            width == s1_1.width || height == s1_1.height -> 1.dp
            width == s2_2.width || height == s2_2.height -> 2.dp
            width == s3_3.width || height == s3_3.height -> 3.dp
            width == s4_4.width || height == s4_4.height -> 4.dp
            else -> 5.dp
        }
        val fontSize = when {
            width == s1_1.width || height == s1_1.height -> 16f
            width == s2_2.width || height == s2_2.height -> 24f
            width == s3_3.width || height == s3_3.height -> 32f
            width == s4_4.width || height == s4_4.height -> 40f
            else -> 48f
        }
        Box(
            modifier = GlanceModifier.fillMaxSize().appWidgetBackground()
                .padding(0.dp, paddingVertical)
        ) {
            Box(
                modifier = GlanceModifier
                    .background(ColorProvider(R.color.widget_background))
                    .cornerRadius(16.dp)
            ) {
                Box(
                    modifier = GlanceModifier.padding(paddingBorder * 2)
                ) {
                    Column(
                        modifier = GlanceModifier.fillMaxSize()
                            .cornerRadius(16.dp - paddingBorder * 2),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = GlanceModifier.fillMaxSize().defaultWeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ReactButton(
                                LocalContext.current.getString(R.string.open),
                                fontSize,
                                DoorActionCmd.open,
                                GlanceModifier.fillMaxSize().defaultWeight()
                                    .padding(0.dp, 0.dp, paddingBorder, paddingBorder)
                            )
                            ReactButton(
                                LocalContext.current.getString(R.string.close),
                                fontSize,
                                DoorActionCmd.close,
                                GlanceModifier.fillMaxSize().defaultWeight()
                                    .padding(paddingBorder, 0.dp, 0.dp, paddingBorder)
                            )
                        }
                        Row(
                            modifier = GlanceModifier.fillMaxSize().defaultWeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ReactButton(
                                LocalContext.current.getString(R.string.stop),
                                fontSize,
                                DoorActionCmd.stop,
                                GlanceModifier.fillMaxSize().defaultWeight()
                                    .padding(0.dp, paddingBorder, paddingBorder, 0.dp)
                            )
                            ReactButton(
                                "〇",
                                fontSize,
                                DoorActionCmd.openAndClose,
                                GlanceModifier.fillMaxSize().defaultWeight()
                                    .padding(paddingBorder, paddingBorder, 0.dp, 0.dp)
                            )
                        }
                    }
                }

            }
        }
    }

    @OptIn(ExperimentalUnitApi::class)
    @Composable
    fun ReactButton(text: String, fontSize: Float, data: ByteArray, modifier: GlanceModifier) {

        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = GlanceModifier.fillMaxSize()
                    .background(ColorProvider(R.color.widget_button))
            ) {}
            Text(
                text = text,
                maxLines = 1,
                style = TextStyle(
                    textAlign = TextAlign.Center,
                    color = ColorProvider(R.color.widget_text),
                    fontSize = TextUnit(fontSize, TextUnitType.Sp)
                )
            )
            Box(
                modifier = GlanceModifier.fillMaxSize().clickable(
                    actionRunCallback<ControlAction>(
                        actionParametersOf(dataKey to data)
                    )
                )
            ) {}
        }
    }
}


class IconWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(setOf(s1_1, s1_2, s2_1))

    @Composable
    override fun Content() {
        when (LocalSize.current) {
            s1_1 -> OpenAndClose()
            s1_2 -> OpenCloseStopColumn()
            s2_1 -> OpenCloseStopRow()
        }
    }

    @Composable
    fun OpenAndClose() {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = GlanceModifier.width(64.dp).height(64.dp).padding(4.dp)) {
                Image(
                    modifier = GlanceModifier.fillMaxSize().cornerRadius(12.dp).clickable(
                        actionRunCallback<ControlAction>(
                            actionParametersOf(dataKey to DoorActionCmd.openAndClose)
                        )
                    ),
                    provider = ImageProvider(R.mipmap.my_launcher_round),
                    contentDescription = "icon"
                )
            }
            Title(text = LocalContext.current.getString(R.string.app_name))
        }

    }

    @OptIn(ExperimentalUnitApi::class)
    @Composable
    fun Title(text: String) {
        Text(
            text = text,
            modifier = GlanceModifier.padding(2.dp)
                .clickable(actionStartActivity<MainActivity>()),
            style = TextStyle(
                fontSize = TextUnit(12f, TextUnitType.Sp),
                color = ColorProvider(R.color.widget_text)
            )
        )
    }

    @Composable
    fun OpenCloseStopRow() {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = GlanceModifier.height(60.dp).padding(6.dp, 0.dp)) {
                Box(
                    modifier = GlanceModifier.background(ColorProvider(R.color.widget_background))
                        .cornerRadius(14.dp)
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxSize().padding(2.dp),
                    ) {
                        val modifier = GlanceModifier.fillMaxSize().defaultWeight()
                        ControlButton(text = "开", modifier, data = DoorActionCmd.open)
                        ControlButton(text = "停", modifier, data = DoorActionCmd.stop)
                        ControlButton(text = "关", modifier, data = DoorActionCmd.close)
                    }
                }
            }
            Box(modifier = GlanceModifier.padding(0.dp, 0.dp, 0.dp, 6.dp)) {
                Title(text = LocalContext.current.getString(R.string.app_name))
            }
        }
    }

    @Composable
    fun OpenCloseStopColumn() {
        Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Box(
                modifier = GlanceModifier.width(64.dp).fillMaxHeight()
                    .padding(0.dp, 6.dp, 0.dp, 30.dp)
            ) {
                Box(
                    modifier = GlanceModifier.background(ColorProvider(R.color.widget_background))
                        .cornerRadius(14.dp)
                ) {
                    Column(
                        modifier = GlanceModifier.fillMaxSize().padding(2.dp),
                    ) {
                        val modifier = GlanceModifier.fillMaxSize().defaultWeight()
                        ControlButton(text = "开", modifier, data = DoorActionCmd.open)
                        ControlButton(text = "停", modifier, data = DoorActionCmd.stop)
                        ControlButton(text = "关", modifier, data = DoorActionCmd.close)
                    }
                }
            }
            Box(modifier = GlanceModifier.padding(0.dp, 10.dp)) {
                Title(text = LocalContext.current.getString(R.string.app_name))
            }
        }
    }

    @Composable
    fun ControlButton(text: String, modifier: GlanceModifier, data: ByteArray) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = GlanceModifier.width(44.dp).height(44.dp).padding(1.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = GlanceModifier.fillMaxSize().cornerRadius(10.dp)
                        .background(ColorProvider(R.color.widget_button))
                ) {}
                Text(
                    text = text,
                    maxLines = 1,
                    style = TextStyle(
                        textAlign = TextAlign.Center,
                        color = ColorProvider(R.color.widget_text),
                    )
                )
                Box(
                    modifier = GlanceModifier.fillMaxSize().clickable(
                        actionRunCallback<ControlAction>(
                            actionParametersOf(dataKey to data)
                        )
                    )
                ) {}
            }
        }
    }
}

private val dataKey = ActionParameters.Key<ByteArray>("data")

class ControlAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val bleAdvertiser = MyBleAdvertiser(context)
        parameters[dataKey]?.let { bleAdvertiser.advertiseCommand(it, 500) }
    }
}

class ControlPanelWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ControlPanelWidget()
}

class IconWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = IconWidget()
}
