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

import kotlinx.coroutines.CoroutineScope
import java.net.URL

/**
 * @author joserobjr
 */
public abstract class ModuleLoader {
    public abstract val name: String
    public abstract val version: String
    public abstract val description: String
    public abstract val authors: Set<ContactInformation>
    public abstract val sourceCode: URL

    protected abstract fun CoroutineScope.createModules(server: Server, secrets: LoadingSecrets): List<Module>

    @InternalModuleApi
    public fun CoroutineScope.create(server: Server): Map<Module, InternalModuleAccess> {
        val secrets = LoadingSecrets(server)
        return createModules(server, secrets)
            .associateWith { module -> secrets.internalAccess.first { it.module == module } }
    }

    public class LoadingSecrets @InternalModuleApi internal constructor(server: Server) {
        init {
            check(server.lifecycle.value == ServerLifecycle.CORE_INITIALIZATION) {
                "Attempted to create a module outside of the core initialization!"
            }
        }

        @InternalModuleApi
        public val internalAccess: MutableList<InternalModuleAccess> = mutableListOf()
    }
}
