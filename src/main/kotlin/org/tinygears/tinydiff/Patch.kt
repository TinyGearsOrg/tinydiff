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
package org.tinygears.tinydiff

import org.tinygears.tinydiff.algorithm.CommandVisitor
import org.tinygears.tinydiff.algorithm.EditScript
import org.tinygears.tinydiff.algorithm.ReplacementsFinder
import org.tinygears.tinydiff.algorithm.ReplacementsHandler
import org.tinygears.tinydiff.format.OutputReplacementsHandler
import org.tinygears.tinydiff.util.readSequence
import java.io.*
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Pattern
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

class Patch internal constructor(        val originalFileName: String?,
                                         val updatedFileName:  String?,
                                 private val editScript:       EditScript<String>) {

    internal fun accept(visitor: CommandVisitor<String>) {
        editScript.accept(visitor)
    }

    internal fun acceptReverse(visitor: CommandVisitor<String>) {
        editScript.acceptReverse(visitor)
    }

    internal fun acceptReplacementHandler(handler: ReplacementsHandler<String>) {
        editScript.accept(ReplacementsFinder(handler))
    }

    internal fun acceptReverseReplacementHandler(handler: ReplacementsHandler<String>) {
        editScript.acceptReverse(ReplacementsFinder(handler))
    }

    fun apply() {
        check(originalFileName != null)
        Paths.get(originalFileName).outputStream().use { os -> apply(os) }
    }

    fun apply(baseDir: Path) {
        check(originalFileName != null)

        val originalFilePath = baseDir.resolve(originalFileName)
        originalFilePath.outputStream().use { os -> apply(baseDir, os) }
    }

    fun apply(baseDir: Path, os: OutputStream) {
        check(originalFileName != null)

        val originalFilePath = baseDir.resolve(originalFileName)
        val inputSequence = originalFilePath.inputStream().use { `is` ->
            readSequence(`is`)
        }

        apply(inputSequence, os)
    }

    fun apply(`is`: InputStream, os: OutputStream) {
        val inputSequence = readSequence(`is`)
        apply(inputSequence, os)
    }

    fun apply(os: OutputStream) {
        check(originalFileName != null)
        val inputSequence = readSequence(originalFileName)
        apply(inputSequence, os)
    }

    private fun apply(inputSequence: List<String>, os: OutputStream) {
        acceptReplacementHandler(PatchReplacementHandler(inputSequence, PrintStream(os)))
    }

    companion object {
        private val UNIFIED_FORMAT_PATTERN = Pattern.compile("^@@ \\-(\\d+)\\,(\\d+) \\+(\\d+)\\,(\\d+) @@$")

        fun loadFrom(fileName: String): Patch {
            BufferedReader(FileReader(fileName)).use { reader ->
                var lineNumber = 1
                val script = EditScript.empty<String>()
                var line: String?

                var origFileName: String? = null
                var newFileName: String?  = null

                var pendingCommand = 'X'
                var pendingLine: String? = null

                val handlePendingCommand: (Boolean) -> Unit = { addNewLine ->
                    if (pendingLine != null) {
                        if (addNewLine) {
                            pendingLine += '\n'
                        }

                        when (pendingCommand) {
                            'I' -> script.appendInsert(pendingLine!!)
                            'D' -> script.appendDelete(pendingLine!!)
                            'K' -> script.appendKeep(pendingLine!!)
                            else -> error("unexpected command $pendingCommand")
                        }

                        pendingCommand = 'X'
                        pendingLine = null
                    }
                }

                while (reader.readLine().also { line = it } != null) {
                    val currentLine = line!!

                    if (currentLine.startsWith("+++")) {
                        newFileName = currentLine.substring(4).split("\\s+".toRegex())[0]
                    } else if (currentLine.startsWith("---")) {
                        origFileName = currentLine.substring(4).split("\\s+".toRegex())[0]
                    } else if (currentLine.startsWith("\\")) {
                        handlePendingCommand(false)
                    } else if (currentLine.startsWith("+")) {
                        handlePendingCommand(true)
                        pendingCommand = 'I'
                        pendingLine = currentLine.substring(1)
                    } else if (currentLine.startsWith("-")) {
                        handlePendingCommand(true)
                        pendingCommand = 'D'
                        pendingLine = currentLine.substring(1)
                        lineNumber++
                    } else if (currentLine.startsWith("@@")) {
                        handlePendingCommand(true)
                        val m = UNIFIED_FORMAT_PATTERN.matcher(currentLine)
                        if (m.matches()) {
                            val origStart = Integer.valueOf(m.group(1))
                            while (lineNumber < origStart) {
                                script.appendKeep(null)
                                lineNumber++
                            }
                        }
                    } else {
                        handlePendingCommand(true)

                        pendingCommand = 'K'
                        pendingLine = currentLine.substring(1)

                        lineNumber++
                    }
                }
                return Patch(origFileName, newFileName, script)
            }
        }
    }
}

internal class PatchReplacementHandler constructor(private val inputSequence: List<String>,
                                                   private val ps:            PrintStream): OutputReplacementsHandler() {

    private var currentLine = 0

    public override fun handleReplacement(from: List<String>, to: List<String>) {
        handleDelete(from)
        handleInsert(to)
    }

    override fun handleKeep(origObj: String?, modifiedObj: String?) {
        val inputObj = inputSequence[currentLine]

        if (origObj != null && inputObj != origObj) {
            error("Hunk FAILED at line ${currentLine + 1}.")
        }

        ps.print(inputObj)
        currentLine++
    }

    private fun handleInsert(insert: List<String>) {
        for (line in insert) {
            ps.print(line)
        }
    }

    private fun handleDelete(delete: List<String>) {
        currentLine += delete.size
    }
}
