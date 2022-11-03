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

import org.tinygears.tinydiff.transform.Transformer

/**
 * A visitor interface to walk through [edit scripts][EditScript].
 */
internal interface CommandVisitor<T> {
    /**
     * Method called before the first command.
     */
    fun visitStart()

    /**
     * Method called after the last command.
     */
    fun visitEnd()

    /**
     * Method called when a delete command is encountered.
     * @param obj object to delete (this object comes from the first sequence)
     */
    fun visitDeleteCommand(obj: T)

    /**
     * Method called when an insert command is encountered.
     * @param obj object to insert (this object comes from the second sequence)
     */
    fun visitInsertCommand(obj: T)

    /**
     * Method called when a keep command is encountered.
     *
     * NOTE: in case an [EditScript] has been loaded from a patch file,
     * the keep information from the original file may be lost, thus [origObj]
     * and [modifiedObj] may be `null` in such cases.
     *
     * NOTE: [origObj] and [modifiedObj] only differ if a
     * [Transformer] was used while
     * generating the [EditScript].
     *
     * @param origObj      object to keep (this object comes from the first sequence)
     * @param modifiedObj  object to keep (this object comes from the second sequence)
     */
    fun visitKeepCommand(origObj: T?, modifiedObj: T?)
}