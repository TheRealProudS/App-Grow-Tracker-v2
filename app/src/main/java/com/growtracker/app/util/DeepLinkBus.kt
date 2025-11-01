package com.growtracker.app.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object DeepLinkBus {
    private val _plantIds = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val plantIds: SharedFlow<String> = _plantIds

    fun emitPlantId(id: String) {
        _plantIds.tryEmit(id)
    }
}
