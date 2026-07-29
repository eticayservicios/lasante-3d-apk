package com.lasante.tvkiosk.data.remote

import com.lasante.tvkiosk.data.SearchItem
import com.lasante.tvkiosk.data.SearchResult

fun SearchResultDto.toDomain() = SearchResult(
    query = query,
    count = count,
    items = items.map { it.toDomain() }
)

fun SearchItemDto.toDomain() = SearchItem(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    tipo = tipo
)
