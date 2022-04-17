package com.hsicen.extension.extensions

/**
 * @author: hsicen
 * @date: 2022/3/4 17:09
 * @email: codinghuang@163.com
 * description: 判空处理
 */

/**
 * 判断非null执行第一个block,it为对象的非null类型;为null则执行第二个block
 */
inline fun <T, R> T?.notNullElse(crossinline block: (T) -> R): (() -> R) -> R =
  { if (this == null) it() else block(this) }


/**
 * 判断2个变量都非空执行block, 参数a, b为对象的非null类型
 */
inline fun <A, B> allNotNull(first: A?, second: B?, block: (A, B) -> Unit) {
  if (first != null && second != null) block(first, second)
}

/**
 * 判断2个变量都非空执行block, 参数a, b为对象的非null类型
 */
inline fun <A, B, C> allNotNull(first: A?, second: B?, third: C?, block: (A, B, C) -> Unit) {
  if (first != null && second != null && third != null) block(first, second, third)
}

/**
 * 判断3个变量都非空执行第一个block, 参数a, b, c为对象的非null类型；为null则执行第二个block
 */
inline fun <A, B, R> allNotNullElse(
  first: A?,
  second: B?,
  crossinline block: (A, B) -> R
): (() -> R) -> R {
  return {
    if (first != null && second != null) {
      block(first, second)
    } else {
      it()
    }
  }

}

/**
 * 判断3个变量都非空执行第一个block, 参数a, b, c为对象的非null类型；为null则执行第二个block
 */
inline fun <A, B, C, R> allNotNullElse(
  first: A?,
  second: B?,
  third: C?,
  crossinline block: (A, B, C) -> R
): (() -> R) -> R {
  return {
    if (first != null && second != null && third != null) {
      block(first, second, third)
    } else {
      it()
    }
  }
}
