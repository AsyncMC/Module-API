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

import com.github.michaelbull.logging.InlineLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect

/**
 * @author joserobjr
 * @since 2021-03-16
 */
public abstract class Module (
    public val moduleLoader: ModuleLoader,
    public val server: Server,
    public val name: String,
    secrets: ModuleLoader.LoadingSecrets,
): ModuleAPI {
    final override val module: Module get() = this
    private val log = InlineLogger()
    private val _lifecycle = MutableStateFlow(ModuleLifecycle.CONSTRUCTION)
    public val lifecycle: StateFlow<ModuleLifecycle> = _lifecycle.asStateFlow()

    init {
        addInternalAccess(secrets)
    }

    @OptIn(InternalModuleApi::class)
    private fun addInternalAccess(secrets: ModuleLoader.LoadingSecrets) {
        secrets.internalAccess += object : InternalModuleAccess() {
            override val module: Module get() = this@Module
            override fun CoroutineScope.changeLifecycleState(state: ModuleLifecycle) = launch {
                require(state != ModuleLifecycle.CONSTRUCTION) { "Unexpected 'construction' lifecycle" }
                if (state == ModuleLifecycle.PRE_INIT) {
                    val started = MutableStateFlow(false)
                    launch { started.value = true; watchLifeStateChangeAsync() }
                    coroutineScope { started.collect { if (it) cancel() } }
                }
                val previous = state.previous!!
                val current = _lifecycle.value
                check(current == previous) {
                    "Attempted to change the module lifecycle of ${moduleLoader.name} - $name from $current to $state directly."
                }
                _lifecycle.emit(state)

                lifecycleChanged(state)
            }
        }
    }

    protected abstract fun CoroutineScope.lifecycleChanged(newState: ModuleLifecycle)

    private fun CoroutineScope.watchLifeStateChangeAsync() = launch {
        lifecycle.collect {
            log.info { "- Module [${moduleLoader.name}][${name}] state has changed to $it -" }
        }
    }
}
