package com.hsicen.extension.extensions

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * 作者：hsicen  5/23/21 22:19
 * 邮箱：codinghuang@163.com
 * 作用：
 * 描述：ViewBinding扩展
 */

/**
 * 扩展  Activity  inflate方式
 * @receiver ComponentActivity
 * @param bindingInflater inflate 方法
 * @param setContent 是否主动调用 setContentView
 * @return ViewBinding
 */
inline fun <T : ViewBinding> ComponentActivity.viewBinding(
  crossinline bindingInflater: (LayoutInflater) -> T,
  setContent: Boolean = false
) = lazy(LazyThreadSafetyMode.NONE) {
  bindingInflater.invoke(layoutInflater).apply {
    if (setContent) setContentView(root)
  }
}

/**
 * Dialog  inflate方式
 * @receiver Dialog
 * @param bindingInflater inflate 方法
 * @param setContent 是否主动调用 setContentView
 * @return ViewBinding
 */
inline fun <T : ViewBinding> Dialog.viewBinding(
  crossinline bindingInflater: (LayoutInflater) -> T,
  setContent: Boolean = false
) = lazy(LazyThreadSafetyMode.NONE) {
  bindingInflater.invoke(layoutInflater).apply {
    if (setContent) setContentView(root)
  }
}

/**
 * Fragment bind方式，绑定已经 inflate 的 View
 * @receiver Fragment
 * @param viewBindingFactory bind(View) 方法
 * @return ViewBinding
 */
fun <T : ViewBinding> Fragment.viewBinding(viewBindingFactory: (View) -> T) =
  FragmentViewBindingByBind(this, viewBindingFactory)

/**
 * Fragment inflate方式，inflate 后要在 Fragment 中返回 View
 * @receiver Fragment
 * @param viewBindingFactory inflate 方法
 * @return ViewBinding
 */
fun <T : ViewBinding> Fragment.viewBinding(viewBindingFactory: (LayoutInflater) -> T) =
  FragmentViewBindingByInflate(this, viewBindingFactory)

/**
 * 基类 Activity，不需要手动 setContentView
 * @param T  xml 生产的 ViewBinding
 * @property binding ViewBinding 实例
 */
abstract class BindingActivity<T : ViewBinding> : AppCompatActivity() {
  protected lateinit var binding: T

  @Suppress("UNCHECKED_CAST")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val type = javaClass.genericSuperclass
    if (type is ParameterizedType) {
      val clazz = type.actualTypeArguments[0] as Class<T>
      val method = clazz.getMethod("inflate", LayoutInflater::class.java)
      binding = method.invoke(null, layoutInflater) as T
      setContentView(binding.root)
    }
  }
}

/**
 * 基类 Fragment，已经在 Fragment 的 onCreateView 中 inflate View 并返回
 * @param T xml 生产的 ViewBinding
 * @property _binding 内部的 ViewBinding 实例
 * @property binding 外部使用的 ViewBinding 实例
 */
abstract class BindingFragment<T : ViewBinding> : Fragment() {
  private var _binding: T? = null
  protected val binding get() = _binding!!

  @Suppress("UNCHECKED_CAST")
  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val type = javaClass.genericSuperclass
    val clazz = (type as ParameterizedType).actualTypeArguments[0] as Class<T>
    val method = clazz.getMethod(
      "inflate",
      LayoutInflater::class.java,
      ViewGroup::class.java,
      Boolean::class.java
    )
    _binding = method.invoke(null, layoutInflater, container, false) as T

    this.viewLifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
      override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) _binding = null
      }
    })
    return binding.root
  }
}

class FragmentViewBindingByBind<T : ViewBinding>(
  val fragment: Fragment,
  val bindingFactory: (View) -> T
) : ReadOnlyProperty<Fragment, T> {
  private var binding: T? = null

  override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
    if (binding == null) {
      binding = bindingFactory(thisRef.requireView())

      thisRef.viewLifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
          if (event == Lifecycle.Event.ON_DESTROY) binding = null
        }
      })
    }

    return binding!!
  }
}

class FragmentViewBindingByInflate<T : ViewBinding>(
  val fragment: Fragment,
  val bindingFactory: (LayoutInflater) -> T
) : ReadOnlyProperty<Fragment, T> {
  private var binding: T? = null

  override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
    if (binding == null) {
      binding = bindingFactory(thisRef.layoutInflater)

      thisRef.viewLifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
          if (event == Lifecycle.Event.ON_DESTROY) binding = null
        }
      })
    }

    return binding!!
  }
}