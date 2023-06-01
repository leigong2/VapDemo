package com.test.vapdemo

import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.tencent.qgame.animplayer.AnimConfig
import com.tencent.qgame.animplayer.AnimPlayer
import com.tencent.qgame.animplayer.AnimView
import com.tencent.qgame.animplayer.inter.IAnimListener
import com.test.vapdemo.FieldUtils.getFieldValue
import com.tencent.qgame.animplayer.util.IScaleType
import com.tencent.qgame.animplayer.util.ScaleType
import java.io.File

class VaPlayerUtils {
    companion object {
        @JvmStatic
        @JvmOverloads
        fun startPlay(
            viewGroup: ViewGroup,
            assetsPath: String,
            loop: Boolean = false,
            scaleType: ScaleType = ScaleType.FIT_CENTER,
            autoDismiss: Boolean = true,
        ) {
            for (i in 0 until viewGroup.childCount) {
                val it = viewGroup.getChildAt(i)
                if (it is AnimView) {
                    /*zune: 里面有正在播放的动画，则复用*/
                    startPlay(it, assetsPath, null, scaleType)
                    return
                }
            }
            val animView = AnimView(viewGroup.context)
            if (loop) {
                animView.setLoop(Int.MAX_VALUE)
            } else {
                animView.setLoop(1)
            }
            viewGroup.addView(animView)
            startPlay(
                animView = animView,
                assetsPath = assetsPath,
                listener = null,
                scaleType = scaleType,
                autoDismiss = autoDismiss
            )
        }

        /**
         * @param animView 播放容器
         * @param assetsPath assetsPath路径
         * @param listener 播放监听
         * @param scaleType FIT_XY全屏，FIT_CENTER居中
         * @param file 播放文件，优先取assets
         * @param autoDismiss  false停留在最后一帧的画面
         */
        @JvmStatic
        @JvmOverloads
        fun startPlay(
            animView: AnimView,
            assetsPath: String? = null,
            listener: IAnimListener? = null,
            scaleType: ScaleType = ScaleType.FIT_CENTER,
            file: File? = null,
            autoDismiss: Boolean = true,
        ) {
            /*zune: 这些播放需要全屏，自定义了setScaleType，重新给他设置了宽高*/
            if (scaleType == ScaleType.FIT_XY) {
                resetFitXYScaleType(animView)
            } else {
                resetCenterInside(animView)
            }
            animView.autoDismiss = autoDismiss
            if (listener == null) {
                animView.setAnimListener(object : IAnimListenerImp(animView) {
                    override fun onVideoComplete(animView: AnimView) {
                        val playLoop = (animView.getFieldValue("player") as? AnimPlayer)?.playLoop
                        /*zune: 循环次数播放完成之前，不移除*/
                        if ((playLoop ?: 0) > 0) {
                            return
                        }
                        if (animView.parent is ViewGroup) {
                            (animView.parent as ViewGroup).removeView(animView)
                        }
                    }
                })
            } else {
                animView.setAnimListener(listener)
            }
            if (assetsPath != null) {
                animView.startPlay(animView.context.assets, assetsPath)
            } else if (file != null) {
                animView.startPlay(file)
            }
        }

        /**
         * 自适应的播放容器
         */
        private fun resetCenterInside(animView: AnimView) {
            var resetWidth = false
            var resetHeight = false
            if ((animView.parent as FrameLayout).layoutParams.width == WRAP_CONTENT) {
                (animView.parent as FrameLayout).layoutParams.width = 1
                resetWidth = true
            }
            if ((animView.parent as FrameLayout).layoutParams.height == WRAP_CONTENT) {
                (animView.parent as FrameLayout).layoutParams.height = 1
                resetHeight = true
            }
            animView.setScaleType(object : IScaleType {
                var videoWidth = 0
                var videoHeight = 0
                override fun getLayoutParam(
                    layoutWidth: Int,
                    layoutHeight: Int,
                    videoWidth: Int,
                    videoHeight: Int,
                    layoutParams: FrameLayout.LayoutParams
                ): FrameLayout.LayoutParams {
                    this.videoWidth = videoWidth
                    this.videoHeight = videoHeight
                    val width = (animView.parent as FrameLayout).layoutParams.width
                    val height = (animView.parent as FrameLayout).layoutParams.height
                    if (resetWidth) {
                        (animView.parent as FrameLayout).layoutParams.width = WRAP_CONTENT
                    }
                    if (resetHeight) {
                        (animView.parent as FrameLayout).layoutParams.height = WRAP_CONTENT
                    }
                    return FrameLayout.LayoutParams(
                        if (width > 1) width else videoWidth,
                        if (height > 1) height else videoHeight
                    ).apply {
                        gravity = Gravity.CENTER
                    }
                }

                override fun getRealSize(): Pair<Int, Int> {
                    return Pair(videoWidth, videoHeight)
                }
            })
        }

        /**
         * 全屏的播放容器
         */
        private fun resetFitXYScaleType(animView: AnimView) {
            animView.setScaleType(object : IScaleType {
                var videoWidth = 0
                var videoHeight = 0
                override fun getLayoutParam(
                    layoutWidth: Int,
                    layoutHeight: Int,
                    videoWidth: Int,
                    videoHeight: Int,
                    layoutParams: FrameLayout.LayoutParams
                ): FrameLayout.LayoutParams {
                    this.videoWidth = videoWidth
                    this.videoHeight = videoHeight
                    val castParams = FrameLayout.LayoutParams(
                        layoutWidth,
                        (layoutWidth.toFloat() * (videoHeight.toFloat() / videoWidth)).toInt()
                            .coerceAtMost(layoutHeight)
                    )
                    castParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                    return castParams
                }

                override fun getRealSize(): Pair<Int, Int> {
                    return Pair(videoWidth, videoHeight)
                }
            })
        }
    }

    abstract class IAnimListenerImp constructor(val animView: AnimView) : IAnimListener {

        override fun onFailed(errorType: Int, errorMsg: String?) {
            animView.apply {
                HandlerGetter.mainHandler.post { onVideoComplete(animView) }
            }
        }

        override fun onVideoComplete() {
            animView.apply {
                HandlerGetter.mainHandler.post { onVideoComplete(animView) }
            }
        }

        override fun onVideoDestroy() {
        }

        override fun onVideoRender(frameIndex: Int, config: AnimConfig?) {
        }

        override fun onVideoStart() {
        }

        override fun onVideoConfigReady(config: AnimConfig): Boolean {
            return true
        }

        open abstract fun onVideoComplete(animView: AnimView);
    }
}