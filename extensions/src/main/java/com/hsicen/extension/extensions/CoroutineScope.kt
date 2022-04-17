package com.hsicen.extension.extensions

import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * @author: hsicen
 * @date: 4/17/22 14:18
 * @email: codinghuang@163.com
 * description: CoroutineScope 扩展.
 */

val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
  throwable.printStackTrace()
}

fun CoroutineScope.launchSafely(
  context: CoroutineContext = EmptyCoroutineContext,
  start: CoroutineStart = CoroutineStart.DEFAULT,
  block: suspend CoroutineScope.() -> Unit
): Job {
  return launch(context + exceptionHandler, start) {
    try {
      block()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}
