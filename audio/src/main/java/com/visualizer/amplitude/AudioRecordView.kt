package com.visualizer.amplitude

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.res.ResourcesCompat
import java.util.*
import kotlin.math.max


class AudioRecordView : View {

    enum class AlignTo(var value: Int) {
        CENTER(1),
        BOTTOM(2)
    }

    private val maxReportableAmp = 22760f //effective size,  max fft = 32760
    private val uninitialized = 0f
    var chunkAlignTo = AlignTo.CENTER

    private val chunkPaint = Paint()
    private val minorTickPaint = Paint()
    private val majorTickPaint = Paint()
    private val timestampPaint = Paint()
    private var lastUpdateTime = 0L

    private val minorTickWidth = 1.dp()
    private val majorTickWidth = 2.dp()
    private val minorTickHeight = 8.dp()
    private val majorTickHeight = 8.dp()
    private val timestampMargin = 4.dp()

    private var usageWidth = 0f
    private var chunkHeights = ArrayList<Float>()
    private var chunkWidths = ArrayList<Float>()
    private var topBottomPadding = 6.dp()

    var chunkSoftTransition = false
    var chunkColor = Color.RED
        set(value) {
            chunkPaint.color = value
            field = value
        }
    var chunkWidth = 2.dp()
        set(value) {
            chunkPaint.strokeWidth = value
            field = value
        }
    var chunkSpace = 1.dp()
    var chunkMaxHeight = uninitialized
    var chunkMinHeight = 3.dp()  // recommended size > 10 dp
    var chunkRoundedCorners = false
        set(value) {
            if (value) {
                chunkPaint.strokeCap = Paint.Cap.ROUND
            } else {
                chunkPaint.strokeCap = Paint.Cap.BUTT
            }
            field = value
        }

    var minorTickColor = Color.BLACK
        set(value) {
            minorTickPaint.color = value
            field = value
        }
    var majorTickColor = Color.RED
        set(value) {
            majorTickPaint.color = value
            field = value
        }
    var timestampColor = Color.GRAY
        set(value) {
            timestampPaint.color = value
            field = value
        }
    var timestampSize = 12.dp()
        set(value) {
            timestampPaint.textSize = value
            field = value
        }
    var timestampTypeface = 0
        set(value) {
            if (value != 0) {
                timestampPaint.typeface = ResourcesCompat.getFont(context, value)
            }
            field = value
        }

    var updateInterval = 100       //ms
    var duration = 0                //ms

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    fun recreate() {
        usageWidth = 0f
        duration = 0
        chunkWidths.clear()
        chunkHeights.clear()
        invalidate()
    }

    fun update(fft: Int) {
        handleNewFFT(fft)
        duration += updateInterval
        invalidate() // call to the onDraw function
        lastUpdateTime = System.currentTimeMillis()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawChunks(canvas)
        drawRudder(canvas)
    }

    private fun init() {
        chunkPaint.isAntiAlias = true
        minorTickPaint.isAntiAlias = true
        majorTickPaint.isAntiAlias = true
        timestampPaint.isAntiAlias = true
        chunkPaint.strokeWidth = chunkWidth
        chunkPaint.color = chunkColor
        minorTickPaint.color = minorTickColor
        majorTickPaint.color = majorTickColor
        timestampPaint.color = timestampColor
        timestampPaint.textSize = timestampSize

        minorTickPaint.strokeWidth = minorTickWidth
        majorTickPaint.strokeWidth = majorTickWidth
        timestampPaint.textAlign = Paint.Align.CENTER
    }

    private fun init(attrs: AttributeSet) {
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.AudioRecordView,
            0, 0
        ).apply {
            try {
                chunkSpace = getDimension(R.styleable.AudioRecordView_chunkSpace, chunkSpace)
                chunkMaxHeight =
                    getDimension(R.styleable.AudioRecordView_chunkMaxHeight, chunkMaxHeight)
                chunkMinHeight =
                    getDimension(R.styleable.AudioRecordView_chunkMinHeight, chunkMinHeight)
                chunkRoundedCorners =
                    getBoolean(R.styleable.AudioRecordView_chunkRoundedCorners, chunkRoundedCorners)
                chunkWidth = getDimension(R.styleable.AudioRecordView_chunkWidth, chunkWidth)
                chunkColor = getColor(R.styleable.AudioRecordView_chunkColor, chunkColor)
                chunkAlignTo =
                    when (getInt(R.styleable.AudioRecordView_chunkAlignTo, chunkAlignTo.ordinal)) {
                        AlignTo.BOTTOM.value -> AlignTo.BOTTOM
                        else -> AlignTo.CENTER
                    }
                minorTickColor =
                    getColor(R.styleable.AudioRecordView_minorTickColor, minorTickColor)
                majorTickColor =
                    getColor(R.styleable.AudioRecordView_majorTickColor, majorTickColor)
                timestampColor =
                    getColor(R.styleable.AudioRecordView_timestampColor, timestampColor)
                timestampSize =
                    getDimension(R.styleable.AudioRecordView_timestampSize, timestampSize)
                timestampTypeface =
                    getResourceId(R.styleable.AudioRecordView_timestampTypeface, timestampTypeface)

                chunkSoftTransition =
                    getBoolean(R.styleable.AudioRecordView_chunkSoftTransition, chunkSoftTransition)



                setWillNotDraw(false)
                chunkPaint.isAntiAlias = true
                minorTickPaint.isAntiAlias = true
                majorTickPaint.isAntiAlias = true
                timestampPaint.isAntiAlias = true
                minorTickPaint.strokeWidth = minorTickWidth
                majorTickPaint.strokeWidth = majorTickWidth
                timestampPaint.textAlign = Paint.Align.CENTER
            } finally {
                recycle()
            }
        }
    }

    private fun handleNewFFT(fft: Int) {
        if (fft == 0) {
            return
        }

        val chunkHorizontalScale = chunkWidth + chunkSpace
        val maxChunkCount = width / chunkHorizontalScale
        if (chunkHeights.isNotEmpty() && chunkHeights.size >= maxChunkCount) {
            chunkHeights.removeAt(0)
        } else {
            usageWidth += chunkHorizontalScale
            chunkWidths.add(chunkWidths.size, usageWidth)
        }

        if (chunkMaxHeight == uninitialized) {
            chunkMaxHeight = height - (topBottomPadding * 2)
        } else if (chunkMaxHeight > height - (topBottomPadding * 2)) {
            chunkMaxHeight = height - (topBottomPadding * 2)
        }

        val verticalDrawScale = chunkMaxHeight - chunkMinHeight
        if (verticalDrawScale == 0f) {
            return
        }

        val point = maxReportableAmp / verticalDrawScale
        if (point == 0f) {
            return
        }

        var fftPoint = fft / point

        if (chunkSoftTransition && chunkHeights.isNotEmpty()) {
            val updateTimeInterval = System.currentTimeMillis() - lastUpdateTime
            val scaleFactor = calculateScaleFactor(updateTimeInterval)
            val prevFftWithoutAdditionalSize = chunkHeights.last() - chunkMinHeight
            fftPoint = fftPoint.softTransition(prevFftWithoutAdditionalSize, 2.2f, scaleFactor)
        }

        fftPoint += chunkMinHeight

        if (fftPoint > chunkMaxHeight) {
            fftPoint = chunkMaxHeight
        } else if (fftPoint < chunkMinHeight) {
            fftPoint = chunkMinHeight
        }

        chunkHeights.add(chunkHeights.size, fftPoint)
    }

    private fun calculateScaleFactor(updateTimeInterval: Long): Float {
        return when (updateTimeInterval) {
            in 0..50 -> 1.6f
            in 50..100 -> 2.2f
            in 100..150 -> 2.8f
            in 100..150 -> 3.4f
            in 150..200 -> 4.2f
            in 200..500 -> 4.8f
            else -> 5.4f
        }
    }

    private fun drawChunks(canvas: Canvas) {
        when (chunkAlignTo) {
            AlignTo.BOTTOM -> drawAlignBottom(canvas)
            else -> drawAlignCenter(canvas)
        }
    }

    private fun drawAlignCenter(canvas: Canvas) {
        val verticalCenter = height / 2
        for (i in 0 until chunkHeights.size - 1) {
            val chunkX = chunkWidths[i]
            val startY = verticalCenter - chunkHeights[i] / 2
            val stopY = verticalCenter + chunkHeights[i] / 2

            canvas.drawLine(chunkX, startY, chunkX, stopY, chunkPaint)
        }
    }

    private fun drawAlignBottom(canvas: Canvas) {
        for (i in 0 until chunkHeights.size - 1) {
            val chunkX = chunkWidths[i]
            val startY = height.toFloat() - topBottomPadding
            val stopY = startY - chunkHeights[i]

            canvas.drawLine(chunkX, startY, chunkX, stopY, chunkPaint)
        }
    }

    private fun drawRudder(canvas: Canvas) {
        val chunkHorizontalScale = chunkWidth + chunkSpace
        val maxChunkCount = (width / chunkHorizontalScale).toInt()
        val maxDuration = maxChunkCount * updateInterval
        var offset = max(0, duration - maxDuration)
        val tickStart = height - timestampSize - 2 * timestampMargin - majorTickHeight
        val minorTickEnd = tickStart + minorTickHeight
        val majorTickEnd = tickStart + majorTickHeight
        var timeStampY = height - timestampMargin
        var time = offset
        for (i in 0 until maxChunkCount - 1) {
            if (time % 200 == 0) {
                val x = i * chunkHorizontalScale
                if (time % 1000 == 0){
                    canvas.drawLine(x, tickStart, x, majorTickEnd, majorTickPaint)
                    canvas.drawText(formatTimestamp(time), x, timeStampY, timestampPaint)
                } else {
                    canvas.drawLine(x, tickStart, x, minorTickEnd, minorTickPaint)
                }
            }
            time += updateInterval
        }
    }

    private fun formatTimestamp(timestamp : Int) : String{
        val minutes = timestamp / 1000 / 60
        val seconds = timestamp / 1000 % 60
        return java.lang.String.format(Locale.US, "%02d:%02d", minutes, seconds)

    }
}