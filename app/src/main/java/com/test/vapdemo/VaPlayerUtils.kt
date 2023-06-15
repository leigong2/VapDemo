package com.test.vapdemo

import android.content.Context
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import com.tencent.qgame.animplayer.AnimConfig
import com.tencent.qgame.animplayer.AnimPlayer
import com.tencent.qgame.animplayer.AnimView
import com.tencent.qgame.animplayer.inter.IAnimListener
import com.tencent.qgame.animplayer.util.IScaleType
import com.tencent.qgame.animplayer.util.ScaleType
import com.test.vapdemo.FieldUtils.getFieldValue
import java.io.File

class VaPlayerUtils {
    companion object {
        /**
         * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
         */
        @JvmStatic
        fun dp2px(context: Context, dpValue: Float): Int {
            val scale: Float = context.getResources().getDisplayMetrics().density
            return (dpValue * scale + 0.5f).toInt()
        }

        @JvmStatic
        @JvmOverloads
        fun startPlay(
            viewGroup: ViewGroup,
            assetsPath: String,
            loop: Boolean = false,
            scaleType: ScaleType = ScaleType.FIT_CENTER,
            autoDismiss: Boolean = true,
        ) {
            val removeViews = ArrayList<View>()
            for(i in 0 until  viewGroup.childCount) {
                val it = viewGroup.getChildAt(i)
                if (it is AnimView) {
                    removeViews.add(it)
                }
            }
            removeViews.forEach {
                (it as AnimView).stopPlay()
                viewGroup.removeView(it)
            }
            var animView: AnimView = AnimView(viewGroup.context)
            viewGroup.addView(animView)
            animView.setTag(assetsPath)
            if (loop) {
                animView.setLoop(Int.MAX_VALUE)
            } else {
                animView.setLoop(1)
            }
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
            //#22945161 SIGSEGV(SEGV_MAPERR)
            if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.M && !TextUtils.isEmpty(
                    assetsPath
                )
            ) {
                /*zune: 6.0手机播放assets文件夹里面的mp4文件，会遇到这个闪退，这里处理方式是直接不让播放了*/
                listener?.onVideoComplete()
                return
            }
            /*zune: 这些播放需要全屏，自定义了setScaleType，重新给他设置了宽高*/
            if (scaleType == ScaleType.FIT_XY) {
                resetFitXYScaleType(animView)
            } else {
                resetCenterInside(animView)
            }
            animView.autoDismiss = autoDismiss
            if (assetsPath != null) {
                animView.startPlay(animView.context.assets, assetsPath)
            } else if (file != null) {
                animView.startPlay(file)
            }
            if (listener == null) {
                animView.setAnimListener(object : IAnimListenerImp(animView) {
                    override fun onVideoComplete(animView: AnimView) {
                        val playLoop = (animView.getFieldValue("player") as? AnimPlayer)?.playLoop
                        /*zune: 循环次数播放完成之前，不移除; 设置不自动移除的话，也不移除*/
                        if ((playLoop ?: 0) > 0 || !animView.autoDismiss) {
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
        }

        @JvmStatic
        fun stopPlay(viewGroup: ViewGroup) {
            for(i in 0 until  viewGroup.childCount) {
                val it = viewGroup.getChildAt(i)
                if (it is AnimView && it.isRunning()) {
                    it.stopPlay()
                }
            }
        }

        /**
         * 自适应的播放容器
         */
        private fun resetCenterInside(animView: AnimView) {
            var resetWidth = false
            var resetHeight = false
            /*zune: 因为wrap_content被包裹的时候，布局不会变，不会执行getLayoutParam方法，所以先提前给他设置为1像素*/
            if ((animView.parent as? FrameLayout)?.layoutParams?.width == WRAP_CONTENT) {
                (animView.parent as? FrameLayout)?.layoutParams?.width = 1
                resetWidth = true
            }
            if ((animView.parent as? FrameLayout)?.layoutParams?.height == WRAP_CONTENT) {
                (animView.parent as? FrameLayout)?.layoutParams?.height = 1
                resetHeight = true
            }
            animView.setScaleType(ScaleType.FIT_CENTER)
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
                    this.videoWidth = dp2px(animView.context, videoWidth / 3f)
                    this.videoHeight = dp2px(animView.context, videoHeight / 3f)
                    val width = (animView.parent as? FrameLayout)?.layoutParams?.width ?: 0
                    val height = (animView.parent as? FrameLayout)?.layoutParams?.height ?: 0
                    if (resetWidth) {
                        (animView.parent as? FrameLayout)?.layoutParams?.width = WRAP_CONTENT
                    }
                    if (resetHeight) {
                        (animView.parent as? FrameLayout)?.layoutParams?.height = WRAP_CONTENT
                    }
                    return FrameLayout.LayoutParams(
                        if (width > 1) width else this.videoWidth,
                        if (height > 1) height else this.videoHeight
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
            animView.setScaleType(ScaleType.FIT_XY)
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
                    castParams.gravity = Gravity.CENTER
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