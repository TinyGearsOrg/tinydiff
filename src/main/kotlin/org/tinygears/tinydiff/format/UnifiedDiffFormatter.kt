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
import java.io.File
import java.io.OutputStream
import java.io.PrintStream
import java.text.SimpleDateFormat
import java.util.*

/** The default number of context lines. */
private const val DEFAULT_CONTEXT_LINES = 3

/**
 * Returns a [PatchFormatter] for the unified diff format printing to [System.out].
 *
 * **See Also:** [Diff Format](http://en.wikipedia.org/wiki/Diff)
 */
fun unifiedDiff(context: Int = DEFAULT_CONTEXT_LINES): PatchFormatter {
    return UnifiedDiffFormatter(System.out, context)
}

/**
 * Returns a [PatchFormatter] for the unified diff format using the given [OutputStream].
 *
 * **See Also:** [Diff Format](http://en.wikipedia.org/wiki/Diff)
 */
fun unifiedDiff(os: OutputStream, context: Int = DEFAULT_CONTEXT_LINES): PatchFormatter {
    val ps = if (os is PrintStream) os else PrintStream(os)
    return UnifiedDiffFormatter(ps, context)
}

/**
 * A [PatchFormatter] for the unified diff format.
 *
 * **See Also:** [Diff Format](http://en.wikipedia.org/wiki/Diff)
 */
internal class UnifiedDiffFormatter constructor(private val ps:      PrintStream,
                                                private val context: Int): PatchFormatter {

    override fun format(patch: Patch) {
        if (patch.originalFileName != null) {
            formatFileName(patch.originalFileName, "---", ps)
        }
        if (patch.modifiedFileName != null) {
            formatFileName(patch.modifiedFileName, "+++", ps)
        }
        val handler = MyReplacementsHandler()
        patch.acceptReplacementHandler(handler)
        handler.finishChunk()
    }

    private fun formatFileName(fileName: String, prefix: String, ps: PrintStream) {
        ps.print(prefix)
        ps.print(' ')
        ps.print(fileName)
        ps.print('\t')
        val file = File(fileName)
        if (file.exists()) {
            val modificationDate = Date(file.lastModified())
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z")
            ps.print(dateFormat.format(modificationDate))
        }
        ps.println()
    }

    companion object {
        private const val NO_NEWLINE = "\\ No newline at end of file"
    }

    private inner class MyReplacementsHandler: OutputReplacementsHandler() {
        /** A circular queue containing the last N lines. */
        private val contextQueue: Deque<String> = LinkedList()

        /** The buffer containing the current chunk. */
        private val chunkBuffer: MutableList<String> = LinkedList()

        /** The starting line number of the current chunk for the first file. */
        private var lineA = 1

        /** The starting line number of the current chunk for the second file. */
        private var lineB = 1

        /** The length of the current chunk for the first file. */
        private var lengthA = 0

        /** The length of the current chunk for the second file. */
        private var lengthB = 0

        /** Indicates the number of context lines already added to the current chunk. */
        private var addedContext = -1

        public override fun handleReplacement(from: List<String>, to: List<String>) {
            if (from.isEmpty()) {
                resetContext()
                handleInsert(to, true)
                lengthB += to.size
            } else if (to.isEmpty()) {
                resetContext()
                handleDelete(from, true)
                lengthA += from.size
            } else {
                resetContext()
                handleDelete(from, true)
                handleInsert(to, false)
                lengthA += from.size
                lengthB += to.size
            }
        }

        private fun addContextToCurrentChunk() {
            while (!contextQueue.isEmpty()) {
                chunkBuffer.add(" ${contextQueue.remove()}")
            }
        }

        private fun handleInsert(insert: List<String>, withContext: Boolean) {
            if (withContext) {
                addContextToCurrentChunk()
            }
            for (line in insert) {
                chunkBuffer.add("+$line")
            }
            addedContext = 0
        }

        private fun handleDelete(delete: List<String>, withContext: Boolean) {
            if (withContext) {
                addContextToCurrentChunk()
            }
            for (line in delete) {
                chunkBuffer.add("-$line")
            }
            addedContext = 0
        }

        override fun handleKeep(origObj: String?, newObj: String?) {
            if (origObj == null) {
                return
            }
            if (addedContext == -1) {
                contextQueue.add(origObj)
                if (contextQueue.size > context) {
                    contextQueue.removeFirst()
                }
            } else {
                chunkBuffer.add(" $origObj")
                lengthA++
                lengthB++
                if (++addedContext == context) {
                    finishChunk()
                }
            }
        }

        private fun resetContext() {
            if (addedContext == -1) {
                lineA   = inputLineNumber - contextQueue.size
                lineB   = outputLineNumber - contextQueue.size
                lengthB = contextQueue.size
                lengthA = lengthB
            }
        }

        /**
         * Finished the current buffered chunk.
         *
         * This method must be called at the end of the visit to close the last pending chunk.
         */
        fun finishChunk() {
            if (chunkBuffer.isNotEmpty()) {
                ps.print("@@ -")
                ps.print(lineA)
                ps.print(',')
                ps.print(lengthA)
                ps.print(" +")
                ps.print(lineB)
                ps.print(',')
                ps.print(lengthB)
                ps.println(" @@")
                for (s in chunkBuffer) {
                    ps.print(s)
                    if (!s.contains("\n")) {
                        ps.println()
                        ps.println(NO_NEWLINE)
                    }
                }
                chunkBuffer.clear()
                addedContext = -1
            }
        }
    }
}