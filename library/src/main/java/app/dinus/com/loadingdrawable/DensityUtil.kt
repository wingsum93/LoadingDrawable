package app.dinus.com.loadingdrawable

import android.content.Context

object DensityUtil {

    fun dip2px(context: Context, dpValue: Float): Float {
        val scale = context.resources.displayMetrics.density
        return dpValue * scale
    }
}