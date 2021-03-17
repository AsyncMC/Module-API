/*
 *  AsyncMC - A fully async, non blocking, thread safe and open source Minecraft server implementation
 *  Copyright (C) 2021  José Roberto de Araújo Júnior <joserobjr@gamemods.com.br>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published
 *  by the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.asyncmc.module.api

/**
 * @author joserobjr
 */
public enum class ServerLifecycle {
    CORE_INITIALIZATION,

    MODULE_PRE_INIT,
    MODULE_INIT,
    MODULE_POST_INIT,

    ;
    private companion object { private val values = values() }
    public val previous: ServerLifecycle? get() = values.getOrNull(ordinal - 1)
    public val next: ServerLifecycle? get() = values.getOrNull(ordinal + 1)
}
