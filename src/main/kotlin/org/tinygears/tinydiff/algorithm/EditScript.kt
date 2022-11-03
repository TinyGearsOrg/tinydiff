/*
 * Copyright (c) 2022 Thomas Neidhart.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tinygears.tinydiff.algorithm

/**
 * This class gathers all the [commands][EditCommand] needed to transform
 * one sequence into another sequence.
 */
internal class EditScript<T> private constructor(private val commands: MutableList<EditCommand<T>> = mutableListOf()) {
    /**
     * Get the length of the Longest Common Subsequence (LCS). The length of the
     * longest common subsequence is the number of [keep][KeepCommand] in the script.
     *
     * @return length of the longest common subsequence
     */
    var lcsLength: Int = 0
        private set

    /**
     * Get the number of effective modifications. The number of effective
     * modification is the number of [delete][DeleteCommand] and
     * [insert][InsertCommand] commands in the script.
     *
     * @return number of effective modifications
     */
    var modifications: Int = 0
        private set

    /**
     * Returns the total number of commands contained in this [EditScript].
     * @return the total number of commands
     */
    val size: Int
        get() = commands.size

    /**
     * Add a keep command to the script.
     * @param obj  object to be kept, may be null
     */
    fun appendKeep(obj: T?) {
        appendKeep(obj, obj)
    }

    /**
     * Add a keep command to the script.
     * @param origObj  object to be kept, may be null
     * @param newObj   object to be kept, may be null
     */
    fun appendKeep(origObj: T?, newObj: T?) {
        commands.add(KeepCommand(origObj, newObj))
        ++lcsLength
    }

    /**
     * Add an insert command to the script.
     * @param obj  object to be inserted
     */
    fun appendInsert(obj: T) {
        commands.add(InsertCommand(obj))
        ++modifications
    }

    /**
     * Add a delete command to the script.
     * @param obj  object to be deleted
     */
    fun appendDelete(obj: T) {
        commands.add(DeleteCommand(obj))
        ++modifications
    }

    /**
     * Accepts the given [CommandVisitor] and visits all
     * [edit commands][EditCommand] of this script.
     * @param visitor  the visitor that will visit all commands
     */
    fun accept(visitor: CommandVisitor<T>) {
        visitor.visitStart()
        for (command in commands) {
            command.accept(visitor)
        }
        visitor.visitEnd()
    }

    /**
     * Accepts the given [CommandVisitor] and visits all
     * [edit commands][EditCommand] of this script in reverse order.
     * @param visitor  the visitor that will visit all commands
     */
    fun acceptReverse(visitor: CommandVisitor<T>) {
        visitor.visitStart()
        for (i in commands.indices.reversed()) {
            val command = commands[i]
            command.accept(visitor)
        }
        visitor.visitEnd()
    }

    override fun toString(): String {
        return "EditScript[%d commands]".format(size)
    }

    companion object {
        fun <T> empty(): EditScript<T> {
            return EditScript()
        }
    }
}
