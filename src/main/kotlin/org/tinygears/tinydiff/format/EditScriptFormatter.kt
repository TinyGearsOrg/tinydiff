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

import org.tinygears.tinydiff.Patch
import org.tinygears.tinydiff.algorithm.ReplacementsHandler
import java.io.OutputStream
import java.io.PrintStream

/**
 * Returns a [PatchFormatter] for the edit script format using [System.out].
 *
 * **See Also:** [Diff Format](http://en.wikipedia.org/wiki/Diff)
 */
fun editScript(): PatchFormatter {
    return EditScriptFormatter(System.out)
}

/**
 * Returns a [PatchFormatter] for the edit script format using the given [OutputStream].
 *
 * **See Also:** [Diff Format](http://en.wikipedia.org/wiki/Diff)
 */
fun editScript(os: OutputStream): PatchFormatter {
    val ps = if (os is PrintStream) os else PrintStream(os)
    return EditScriptFormatter(ps)
}

/**
 * A [PatchFormatter] for the edit script format.
 *
 * **See Also:** [Diff Format](http://en.wikipedia.org/wiki/Diff)
 */
internal class EditScriptFormatter constructor(private val ps: PrintStream): PatchFormatter {

    override fun format(patch: Patch) {
        // first get the count of the last replacement command in the original sequence
        val counter = LineCounter()
        patch.acceptReplacementHandler(counter)
        patch.acceptReverseReplacementHandler(EditScriptReplacementsHandler(counter.lineNumber))
    }

    /**
     * A simple [ReplacementsHandler] to calculate the line number of the last command.
     */
    private class LineCounter : ReplacementsHandler<String> {
        var lineNumber = 1
            private set

        override fun handleReplacement(skipped: Int, from: List<String>, to: List<String>) {
            lineNumber += skipped + from.size
        }

        override fun handleKeep(origObj: String?, modifiedObj: String?) {}
    }

    private inner class EditScriptReplacementsHandler(private var inputLine: Int) : ReplacementsHandler<String> {
        private var firstSkip: Boolean = true

        override fun handleReplacement(skipped: Int, from: List<String>, to: List<String>) {
            // ignore the first skip, as we already calculated the line number up to the first replacement
            if (!firstSkip) {
                inputLine -= skipped
            } else {
                firstSkip = false
            }
            if (from.isEmpty()) {
                ps.print(inputLine - 1)
                ps.println('a')
                handleInsert(to)
            } else if (to.isEmpty()) {
                inputLine -= from.size
                ps.print(getLineInfo(inputLine, from.size))
                ps.println('d')
            } else {
                inputLine -= from.size
                ps.print(getLineInfo(inputLine, from.size))
                ps.println('c')
                handleInsert(to)
            }
        }

        private fun getLineInfo(startLine: Int, len: Int): String {
            return if (len == 1) {
                startLine.toString()
            } else {
                startLine.toString() + "," + (startLine + len - 1)
            }
        }

        private fun handleInsert(insert: List<String>) {
            for (i in insert.indices.reversed()) {
                val line = insert[i]
                ps.print(line)
                if (!line.endsWith("\n")) {
                    ps.println()
                }
            }
            ps.println('.')
        }

        override fun handleKeep(origObj: String?, modifiedObj: String?) {}
    }
}