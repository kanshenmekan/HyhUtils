package com.huyuhui.utils.demo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.viewbinding.ViewBinding
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.ParameterizedType

/**
 * 计算渐变颜色值 ARGB
 *
 * @param fraction   变化比率 0~1
 * @param startValue 初始色值
 * @param endValue   结束色值
 * @return 颜色数值
 */
fun evaluate(fraction: Float, startValue: Int, endValue: Int): Int {
    if (fraction >= 1) return endValue
    val startA = startValue shr 24 and 0xff
    val startR = startValue shr 16 and 0xff
    val startG = startValue shr 8 and 0xff
    val startB = startValue and 0xff
    val endA = endValue shr 24 and 0xff
    val endR = endValue shr 16 and 0xff
    val endG = endValue shr 8 and 0xff
    val endB = endValue and 0xff
    return (startA + (fraction * (endA - startA)).toInt() shl 24
            or (startR + (fraction * (endR - startR)).toInt() shl 16)
            or (startG + (fraction * (endG - startG)).toInt() shl 8)
            or startB + (fraction * (endB - startB)).toInt())
}

fun evaluate(
    @FloatRange(from = 0.0, to = 1.0) fraction: Float,
    @ColorInt vararg colors: Int,
): Int {
    val per = 1.0 / colors.size
    val start = (fraction / per).toInt()
    if (start >= colors.size - 1) {
        return colors.last()
    }
    val fractionUse = (fraction % per) / per
    return evaluate(fractionUse.toFloat(), colors[start], colors[start + 1])
}

@Suppress("UNCHECKED_CAST")
@JvmName("inflateWithGeneric")
fun <VB : ViewBinding> BaseActivity<VB>.inflateBindingWithGeneric(): VB =
    withGenericBindingClass(this) { clazz ->
        clazz.getMethod("inflate", LayoutInflater::class.java).invoke(null, layoutInflater) as VB
    }.also {
//        if (binding is ViewDataBinding) {
//            binding.lifecycleOwner = this
//        }
    }
@Suppress("UNCHECKED_CAST")
@JvmName("inflateWithGeneric")
fun <VB : ViewBinding> BaseFragment<VB>.inflateBindingWithGeneric(
    parent: ViewGroup?,
    attachToParent: Boolean
): VB =
    withGenericBindingClass<VB>(this) { clazz ->
        clazz.getMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.java
        )
            .invoke(null, layoutInflater, parent, attachToParent) as VB
    }.also {
//        if (binding is ViewDataBinding) {
//            binding.lifecycleOwner = viewLifecycleOwner
//        }
    }

@Suppress("UNCHECKED_CAST")
private fun <VB : ViewBinding> withGenericBindingClass(any: Any, block: (Class<VB>) -> VB): VB {
    var genericSuperclass = any.javaClass.genericSuperclass
    var superclass = any.javaClass.superclass
    while (superclass != null) {
        if (genericSuperclass is ParameterizedType) {
            try {
                return block.invoke(genericSuperclass.actualTypeArguments[0] as Class<VB>)
            } catch (_: NoSuchMethodException) {
            } catch (_: ClassCastException) {
            } catch (e: InvocationTargetException) {
                throw e.targetException
            }
        }
        genericSuperclass = superclass.genericSuperclass
        superclass = superclass.superclass
    }
    throw IllegalArgumentException("There is no generic of ViewBinding.")
}