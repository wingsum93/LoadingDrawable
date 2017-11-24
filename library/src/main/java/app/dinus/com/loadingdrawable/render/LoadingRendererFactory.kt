package app.dinus.com.loadingdrawable.render

import android.content.Context
import android.util.SparseArray

import java.lang.reflect.Constructor

import app.dinus.com.loadingdrawable.render.animal.FishLoadingRenderer
import app.dinus.com.loadingdrawable.render.animal.GhostsEyeLoadingRenderer
import app.dinus.com.loadingdrawable.render.circle.jump.CollisionLoadingRenderer
import app.dinus.com.loadingdrawable.render.circle.jump.DanceLoadingRenderer
import app.dinus.com.loadingdrawable.render.circle.jump.GuardLoadingRenderer
import app.dinus.com.loadingdrawable.render.circle.jump.SwapLoadingRenderer
import app.dinus.com.loadingdrawable.render.circle.rotate.GearLoadingRenderer
import app.dinus.com.loadingdrawable.render.circle.rotate.LevelLoadingRenderer
import app.dinus.com.loadingdrawable.render.circle.rotate.MaterialLoadingRenderer
import app.dinus.com.loadingdrawable.render.circle.rotate.WhorlLoadingRenderer
import app.dinus.com.loadingdrawable.render.goods.BalloonLoadingRenderer
import app.dinus.com.loadingdrawable.render.goods.WaterBottleLoadingRenderer
import app.dinus.com.loadingdrawable.render.scenery.DayNightLoadingRenderer
import app.dinus.com.loadingdrawable.render.scenery.ElectricFanLoadingRenderer
import app.dinus.com.loadingdrawable.render.shapechange.CircleBroodLoadingRenderer
import app.dinus.com.loadingdrawable.render.shapechange.CoolWaitLoadingRenderer

object LoadingRendererFactory {
    private val LOADING_RENDERERS = SparseArray<Class<out LoadingRenderer>>()

    init {
        //circle rotate
        LOADING_RENDERERS.put(0, MaterialLoadingRenderer::class.java)
        LOADING_RENDERERS.put(1, LevelLoadingRenderer::class.java)
        LOADING_RENDERERS.put(2, WhorlLoadingRenderer::class.java)
        LOADING_RENDERERS.put(3, GearLoadingRenderer::class.java)
        //circle jump
        LOADING_RENDERERS.put(4, SwapLoadingRenderer::class.java)
        LOADING_RENDERERS.put(5, GuardLoadingRenderer::class.java)
        LOADING_RENDERERS.put(6, DanceLoadingRenderer::class.java)
        LOADING_RENDERERS.put(7, CollisionLoadingRenderer::class.java)
        //scenery
        LOADING_RENDERERS.put(8, DayNightLoadingRenderer::class.java)
        LOADING_RENDERERS.put(9, ElectricFanLoadingRenderer::class.java)
        //animal
        LOADING_RENDERERS.put(10, FishLoadingRenderer::class.java)
        LOADING_RENDERERS.put(11, GhostsEyeLoadingRenderer::class.java)
        //goods
        LOADING_RENDERERS.put(12, BalloonLoadingRenderer::class.java)
        LOADING_RENDERERS.put(13, WaterBottleLoadingRenderer::class.java)
        //shape change
        LOADING_RENDERERS.put(14, CircleBroodLoadingRenderer::class.java)
        LOADING_RENDERERS.put(15, CoolWaitLoadingRenderer::class.java)
    }

    @Throws(Exception::class)
    fun createLoadingRenderer(context: Context, loadingRendererId: Int): LoadingRenderer {
        val loadingRendererClazz = LOADING_RENDERERS.get(loadingRendererId)
        val constructors = loadingRendererClazz.declaredConstructors
        for (constructor in constructors) {
            val parameterTypes = constructor.parameterTypes
            if (parameterTypes != null
                    && parameterTypes.size == 1
                    && parameterTypes[0] == Context::class.java) {
                constructor.isAccessible = true
                return constructor.newInstance(context) as LoadingRenderer
            }
        }

        throw InstantiationException()
    }
}
