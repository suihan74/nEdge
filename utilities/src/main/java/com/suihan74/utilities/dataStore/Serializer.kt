package com.suihan74.utilities.dataStore

import kotlin.reflect.KClass

/** そのままでは保存できないデータを保存できる型に変換する */
interface Serializer<SrcT, DestT> {
    /** 保存可能な型に変換する */
    fun serialize(value: SrcT) : DestT

    /** 保存された型から利用時のデータ型に戻す */
    fun deserialize(value: DestT) : SrcT
}

@Target(AnnotationTarget.PROPERTY)
annotation class Serialize(val kClass: KClass<*>)
