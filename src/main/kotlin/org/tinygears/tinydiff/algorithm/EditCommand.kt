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
 * Abstract base class for all commands used to transform an objects sequence
 * into another one.
 *
 * When two objects sequences are compared through the [DiffAlgorithm.getEditScript]
 * method, the result is provided has a [script][EditScript] containing the commands
 * that progressively transform the first sequence into the second one.
 *
 * There are only three types of commands, all of which are subclasses of this
 * abstract class. Each command is associated with one object belonging to at
 * least one of the sequences. These commands are [ InsertCommand][InsertCommand]
 * which correspond to an object of the second sequence being inserted into the
 * first sequence, [DeleteCommand] which correspond to an object of the first sequence
 * being removed and [KeepCommand] which correspond to an object of the first
 * sequence which `equals` an object in the second sequence. It is guaranteed that
 * comparison is always performed this way (i.e. the `equals` method of the object
 * from the first sequence is used and the object passed as an argument comes from
 * the second sequence) ; this can be important if subclassing is used for some
 * elements in the first sequence and the `equals` method is specialized.
 *
 * @see DiffAlgorithm
 * @see EditScript
 */
internal interface EditCommand<T> {
    /**
     * Accept a visitor.
     *
     * This method is invoked for each command belonging to an [EditScript],
     * in order to implement the visitor design pattern.
     *
     * @param visitor  the visitor to be accepted
     */
    fun accept(visitor: CommandVisitor<T>)
}

/**
 * Command representing the deletion of one object of the first sequence.
 */
internal data class DeleteCommand<T> constructor(val obj: T): EditCommand<T> {
    /**
     * Accept a [CommandVisitor].
     * @param visitor  the visitor to be accepted
     */
    override fun accept(visitor: CommandVisitor<T>) {
        visitor.visitDeleteCommand(obj)
    }

    override fun toString(): String {
        return "Delete[$obj]"
    }
}

/**
 * Command representing the insertion of one object of the second sequence.
 */
internal data class InsertCommand<T> constructor(val obj: T): EditCommand<T> {
    /**
     * Accept a [CommandVisitor].
     * @param visitor  the visitor to be accepted
     */
    override fun accept(visitor: CommandVisitor<T>) {
        visitor.visitInsertCommand(obj)
    }

    override fun toString(): String {
        return "Insert[$obj]"
    }
}

/**
 * Command representing the keeping of one object present in both sequences.
 */
internal data class KeepCommand<T> constructor(val origObj: T?, val modifiedObj: T?): EditCommand<T> {

    internal constructor(origObj: T?): this(origObj, origObj)

    /**
     * Accept a [CommandVisitor].
     * @param visitor  the visitor to be accepted
     */
    override fun accept(visitor: CommandVisitor<T>) {
        visitor.visitKeepCommand(origObj, modifiedObj)
    }

    override fun toString(): String {
        return "Keep[$origObj,$modifiedObj]"
    }
}
