package com.hsicen.extension.extensions

/**
 * 作者：hsicen  8/3/21 11:15
 * 邮箱：codinghuang@163.com
 * 作用：
 * 描述：区间扩展
 */

/**
 * for( i in 0 toward 10) [0,10]
 * for( i in 10 toward 0) [10,0]
 * @receiver Int
 * @param to Int
 * @return IntProgression
 */
infix fun Int.toward(to: Int): IntProgression {
    val step = if (this > to) -1 else 1
    return IntProgression.fromClosedRange(this, to, step)
}
