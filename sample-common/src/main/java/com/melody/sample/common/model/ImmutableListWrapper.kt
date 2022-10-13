package com.melody.sample.common.model

import androidx.compose.runtime.Immutable

@Immutable
class ImmutableListWrapper<T: Any>(val items: List<T>)