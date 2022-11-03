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

/** The default column width. */
private const val DEFAULT_TOTAL_WIDTH = 130

/**
 * Returns a [PatchFormatter] for a side-by-side view similar as produces
 * by the gnu diff command with switch '-y' printing to [System.out].
 */
fun sideBySide(totalWidth: Int = DEFAULT_TOTAL_WIDTH): PatchFormatter {
    return SideBySideFormatter(System.out, totalWidth)
}

/**
 * Returns a [PatchFormatter] for a side-by-side view similar as produces
 * by the gnu diff command with switch '-y' printing to the given [OutputStream].
 */
fun sideBySide(os: OutputStream, totalWidth: Int = DEFAULT_TOTAL_WIDTH): PatchFormatter {
    val ps = if (os is PrintStream) os else PrintStream(os)
    return SideBySideFormatter(ps, totalWidth)
}

/**
 * A side-by-side formatter similar as produced by the gnu diff command with switch '-y'.
 */
internal class SideBySideFormatter constructor(private val ps:         PrintStream,
                                               private val totalWidth: Int): PatchFormatter {

    override fun format(patch: Patch) {
        patch.acceptReplacementHandler(SideBySideReplacementsHandler())
    }

    private inner class SideBySideReplacementsHandler: OutputReplacementsHandler() {
        private val columnWidth: Int    = (totalWidth - 3) / 2
        private val separatorWidth: Int = totalWidth - 2 * columnWidth
        private val spaces: CharArray   = CharArray(columnWidth) { ' ' }

        public override fun handleReplacement(from: List<String>, to: List<String>) {
            if (from.isEmpty()) {
                handleInsert(to)
            } else if (to.isEmpty()) {
                handleDelete(from)
            } else {
                var i = 0
                var j = 0
                while (i < from.size && j < to.size) {
                    val a = from[i]
                    val b = to[i]

                    val aWithoutCRLF = removeCRLF(a)
                    ps.print(rightTrim(aWithoutCRLF))
                    ps.print(getSpaces(columnWidth - getLength(aWithoutCRLF)))

                    val bWithoutCRLF = removeCRLF(b)

                    val aHasLF = aWithoutCRLF.length != a.length
                    val bHasLF = bWithoutCRLF.length != b.length

                    val separator = if (!aHasLF && bHasLF) {
                        '\\'
                    } else if (aHasLF && !bHasLF) {
                        '/'
                    } else {
                        '|'
                    }

                    printSeparator(ps, separator)

                    ps.println(rightTrim(bWithoutCRLF))
                    i++
                    j++
                }
                handleDelete(from.subList(i, from.size))
                handleInsert(to.subList(j, to.size))
            }
        }

        override fun handleKeep(origObj: String?, newObj: String?) {
            if (origObj == null || newObj == null) {
                return
            }
            val origObjWithoutCRLF = removeCRLF(origObj)
            val trimmedOrigObj     = rightTrim(origObjWithoutCRLF)
            ps.print(trimmedOrigObj)
            ps.print(getSpaces(columnWidth - getLength(trimmedOrigObj)))
            printSeparator(ps, ' ')

            val newObjWithoutCRLF = removeCRLF(newObj)
            val trimmedNewObj     = rightTrim(newObjWithoutCRLF)

            ps.println(trimmedNewObj)
        }

        private fun handleInsert(insert: List<String>) {
            for (line in insert) {
                val lineWithoutCRLF = removeCRLF(line)
                ps.print(spaces)
                printSeparator(ps, '>')
                ps.println(rightTrim(lineWithoutCRLF))
            }
        }

        private fun handleDelete(delete: List<String>) {
            for (line in delete) {
                val lineWithoutCRLF = removeCRLF(line)
                val trimmedLine = rightTrim(lineWithoutCRLF)
                ps.print(trimmedLine)
                ps.print(getSpaces(columnWidth - getLength(trimmedLine)))
                printSeparator(ps, '<')
                ps.println()
            }
        }

        private fun removeCRLF(input: String): String {
            return input.removeSuffix("\n").removeSuffix("\r")
        }

        private fun rightTrim(input: String): String {
            // FIXME: this is inefficient and can lead to an infinite loop in case of tabs
            var str = input.substring(0, columnWidth.coerceAtMost(input.length))
            while (getLength(str) > columnWidth) {
                str = str.substring(0, str.length - 1)
            }
            return str
        }

        private fun getLength(str: String): Int {
            var len = str.length
            var idx = 0
            var totalSlack = 0
            while (str.indexOf('\t', idx).also { idx = it } != -1) {
                val slack = 7 - (idx + totalSlack) % 8
                totalSlack += slack
                len += slack
                idx++
            }
            return len
        }

        private fun getSpaces(len: Int): String {
            return if (len <= 0) {
                ""
            } else {
                String(spaces, 0, len)
            }
        }

        private fun printSeparator(ps: PrintStream, separator: Char) {
            ps.print(' ')
            ps.print(separator)
            ps.print(getSpaces(separatorWidth - 2))
        }
    }
}