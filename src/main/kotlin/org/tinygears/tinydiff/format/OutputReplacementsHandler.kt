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
package org.tinygears.tinydiff.format

import org.tinygears.tinydiff.algorithm.ReplacementsHandler

/**
 * A default [ReplacementsHandler] to simplify patch formatting.
 * Keeps track of the current line numbers.
 */
abstract class OutputReplacementsHandler: ReplacementsHandler<String> {
    protected var inputLineNumber  = 1
        private set

    protected var outputLineNumber = 1
        private set

    override fun handleReplacement(skipped: Int, from: List<String>, to: List<String>) {
        inputLineNumber  += skipped
        outputLineNumber += skipped
        handleReplacement(from, to)
        inputLineNumber  += from.size
        outputLineNumber += to.size
    }

    override fun handleKeep(origObj: String?, modifiedObj: String?) {}

    protected abstract fun handleReplacement(from: List<String>, to: List<String>)
}