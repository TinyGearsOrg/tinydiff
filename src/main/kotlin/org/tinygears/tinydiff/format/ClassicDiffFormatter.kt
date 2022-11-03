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
import java.io.OutputStream
import java.io.PrintStream

/**
 * Returns a [PatchFormatter] for the classic diff format printing to [System.out].
 *
 * **See Also:** [Diff Format](http://en.wikipedia.org/wiki/Diff)
 */
fun classicDiff(): PatchFormatter {
    return ClassicDiffFormatter(System.out)
}

/**
 * Returns a [PatchFormatter] for the classic diff format using the given [OutputStream].
 *
 * **See Also:** [Diff Format](http://en.wikipedia.org/wiki/Diff)
 */
fun classicDiff(os: OutputStream): PatchFormatter {
    val ps = if (os is PrintStream) os else PrintStream(os)
    return ClassicDiffFormatter(ps)
}

/**
 * A [PatchFormatter] for the classic diff format.
 *
 * **See Also:** [Diff Format](http://en.wikipedia.org/wiki/Diff)
 */
private class ClassicDiffFormatter constructor(private val ps: PrintStream): PatchFormatter {

    private val replacementHandler = ClassDiffReplacementsHandler()

    override fun format(patch: Patch) {
        patch.acceptReplacementHandler(replacementHandler)
    }

    companion object {
        private const val NO_NEWLINE = "\\ No newline at end of file"
    }

    private inner class ClassDiffReplacementsHandler: OutputReplacementsHandler() {
        override fun handleReplacement(from: List<String>, to: List<String>) {
            if (from.isEmpty()) {
                ps.print(inputLineNumber - 1)
                ps.print('a')
                ps.println(getLineInfo(outputLineNumber, to.size))
                handleInsert(to)
            } else if (to.isEmpty()) {
                ps.print(getLineInfo(inputLineNumber, from.size))
                ps.print('d')
                ps.println(outputLineNumber - 1)
                handleDelete(from)
            } else {
                ps.print(getLineInfo(inputLineNumber, from.size))
                ps.print('c')
                ps.println(getLineInfo(outputLineNumber, to.size))
                handleDelete(from)
                ps.println("---")
                handleInsert(to)
            }
        }

        private fun handleInsert(insert: List<String>) {
            for (line in insert) {
                ps.print("> ")
                ps.print(line)
                if (!line.endsWith("\n")) {
                    ps.println()
                    ps.println(NO_NEWLINE)
                }
            }
        }

        private fun handleDelete(delete: List<String>) {
            for (line in delete) {
                ps.print("< ")
                ps.print(line)
                if (!line.endsWith("\n")) {
                    ps.println()
                    ps.println(NO_NEWLINE)
                }
            }
        }

        private fun getLineInfo(startLine: Int, len: Int): String {
            return if (len == 1) {
                startLine.toString()
            } else {
                "%1\$d,%2\$d".format(startLine, startLine + len - 1)
            }
        }
    }
}