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
import org.tinygears.tinydiff.transform.transform
import java.util.*

/**
 * This class allows to compare two objects sequences.
 *
 * The two sequences can hold any object type, as only the `equals`
 * method is used to compare the elements of the sequences. It is
 * guaranteed that the comparisons will always be done as `o1.equals(o2)`
 * where `o1` belongs to the first sequence and `o2` belongs to the
 * second sequence. This can be important if subclassing is used for some
 * elements in the first sequence and the `equals` method is specialized.
 *
 * Comparison can be seen from two points of view: either as giving the smallest
 * modification allowing to transform the first sequence into the second one, or
 * as giving the longest sequence which is a subsequence of both initial
 * sequences. The `equals` method is used to compare objects, so any
 * object can be put into sequences. Modifications include deleting, inserting
 * or keeping one object, starting from the beginning of the first sequence.
 *
 * This class implements the comparison algorithm, which is the very efficient
 * algorithm from Eugene W. Myers
 *
 * [An O(ND) Difference Algorithm and Its Variations](http://www.cis.upenn.edu/~bcpierce/courses/dd/papers/diff.ps).
 *
 * This algorithm produces the shortest possible [edit script][EditScript] containing
 * all the [commands][EditCommand] needed to transform the first sequence into the
 * second one.
 *
 * @see EditScript
 * @see EditCommand
 * @see CommandVisitor
 */
internal class MyersDiffAlgorithm<T> : DiffAlgorithm<T> {
    /**
     * Get the [EditScript] object.
     * @return the edit script resulting from the comparison of the two sequences
     */
    override fun getEditScript(origSequence: List<T>, modifiedSequence: List<T>, vararg transformers: Transformer<T>): EditScript<T> {
        val context =
            if (transformers.isEmpty()) {
                Context(origSequence, modifiedSequence)
            } else {
                val origSequenceTransformed     = origSequence    .map { transform(it, *transformers) }
                val modifiedSequenceTransformed = modifiedSequence.map { transform(it, *transformers) }

                Context(origSequenceTransformed, modifiedSequenceTransformed, origSequence, modifiedSequence)
            }

        return context.buildScript()
    }

    private inner class Context constructor(val sequenceA:         List<T>,
                                            val sequenceB:         List<T>,
                                            val originalSequenceA: List<T> = sequenceA,
                                            val originalSequenceB: List<T> = sequenceB) {

        private val editScript: EditScript<T> = EditScript.empty()

        private val vDown: IntArray
        private val vUp:   IntArray

        init {
            require(sequenceA.size == originalSequenceA.size)
            require(sequenceB.size == originalSequenceB.size)

            val size = sequenceA.size + sequenceB.size + 2
            vDown = IntArray(size)
            vUp   = IntArray(size)
        }

        /**
         * Build an edit script.
         */
        fun buildScript(): EditScript<T> {
            buildScript(0, sequenceA.size, 0, sequenceB.size)
            return editScript
        }

        /**
         * Build a snake.
         *
         * @param start the value of the start of the snake
         * @param diag  the value of the diagonal of the snake
         * @param end1  the value of the end of the first sequence to be compared
         * @param end2  the value of the end of the second sequence to be compared
         * @return the snake built
         */
        private fun buildSnake(start: Int, diag: Int, end1: Int, end2: Int): Snake {
            var end = start

            while (end - diag < end2 &&
                   end        < end1 &&
                   sequenceA[end] == sequenceB[end - diag]) {
                ++end
            }

            return Snake(start, end, diag)
        }

        /**
         * Get the middle snake corresponding to two subsequences of the main sequences.
         *
         * The snake is found using the MYERS Algorithm (this algorithm has
         * also been implemented in the GNU diff program). This algorithm is
         * explained in Eugene Myers article:
         *
         * [An O(ND) Difference Algorithm and Its Variations](http://www.cs.arizona.edu/people/gene/PAPERS/diff.ps).
         *
         * @param start1 the beginning of the first sequence to be compared
         * @param end1   the end of the first sequence to be compared
         * @param start2 the beginning of the second sequence to be compared
         * @param end2   the end of the second sequence to be compared
         * @return the middle snake
         */
        private fun getMiddleSnake(start1: Int, end1: Int, start2: Int, end2: Int): Snake? {
            // Myers Algorithm
            // Initialisations
            val m = end1 - start1
            val n = end2 - start2
            if (m == 0 || n == 0) {
                return null
            }

            val delta = m - n
            val sum = n + m
            val offset = (if (sum % 2 == 0) sum else sum + 1) / 2
            vDown[1 + offset] = start1
            vUp[1 + offset] = end1 + 1
            for (d in 0..offset) {
                // Down
                var k = -d
                while (k <= d) {
                    // First step
                    val i = k + offset
                    if (k == -d || k != d && vDown[i - 1] < vDown[i + 1]) {
                        vDown[i] = vDown[i + 1]
                    } else {
                        vDown[i] = vDown[i - 1] + 1
                    }
                    var x = vDown[i]
                    var y = x - start1 + start2 - k
                    while (x < end1 && y < end2 && sequenceA[x] == sequenceB[y]) {
                        vDown[i] = ++x
                        ++y
                    }

                    // Second step
                    if (delta % 2 != 0 && delta - d + 1 <= k && k <= delta + d - 1) {
                        if (vUp[i - delta] <= vDown[i]) {
                            return buildSnake(vUp[i - delta], k + start1 - start2, end1, end2)
                        }
                    }
                    k += 2
                }

                // Up
                k = delta - d
                while (k <= delta + d) {
                    // First step
                    val i = k + offset - delta
                    if (k == delta - d
                        || k != delta + d && vUp[i + 1] <= vUp[i - 1]
                    ) {
                        vUp[i] = vUp[i + 1] - 1
                    } else {
                        vUp[i] = vUp[i - 1]
                    }
                    var x = vUp[i] - 1
                    var y = x - start1 + start2 - k
                    while (x >= start1 && y >= start2 && sequenceA[x] == sequenceB[y]) {
                        vUp[i] = x--
                        y--
                    }

                    // Second step
                    if (delta % 2 == 0 && -d <= k + delta && k + delta <= d) {
                        if (vUp[i] <= vDown[i + delta]) {
                            return buildSnake(vUp[i], k + start1 - start2, end1, end2)
                        }
                    }
                    k += 2
                }
            }

            return null
        }

        /**
         * Build an edit script.
         *
         * @param start1 the beginning of the first sequence to be compared
         * @param end1   the end of the first sequence to be compared
         * @param start2 the beginning of the second sequence to be compared
         * @param end2   the end of the second sequence to be compared
         */
        @Suppress("NAME_SHADOWING")
        private fun buildScript(start1: Int, end1: Int, start2: Int, end2: Int) {
            var start1 = start1
            var end1   = end1
            var start2 = start2
            var end2   = end2

            // short-cut: for elements at the start/end of the two sequences that are equal
            //            we can already add keep commands and omit them during the main algorithm
            //            to speed up things.
            while (start1 < end1 && start2 < end2 && sequenceA[start1] == sequenceB[start2]) {
                editScript.appendKeep(originalSequenceA[start1], originalSequenceB[start2])
                ++start1
                ++start2
            }

            val commands = LinkedList<Pair<T, T>>()
            while (end1 > start1 && end2 > start2 && sequenceA[end1 - 1] == sequenceB[end2 - 1]) {
                // collect the equal elements at the end in reverse order.
                commands.addFirst(Pair(originalSequenceA[end1 - 1], originalSequenceB[end2 - 1]))
                --end1
                --end2
            }

            // one of the sequences is a sub-sequence of the other one.
            if (start1 == end1) {
                while (start2 < end2) {
                    editScript.appendInsert(originalSequenceB[start2++])
                }
            } else if (start2 == end2) {
                while (start1 < end1) {
                    editScript.appendDelete(originalSequenceA[start1++])
                }
            } else {
                // for the remaining part, calculate the middle snake
                val middle = getMiddleSnake(start1, end1, start2, end2)
                if (middle == null ||
                    middle.start == end1   && middle.diag == end1 - end2 ||
                    middle.end   == start1 && middle.diag == start1 - start2) {
                    var i = start1
                    var j = start2
                    while (i < end1 || j < end2) {
                        if (i < end1 && j < end2 && sequenceA[i] == sequenceB[j]) {
                            editScript.appendKeep(originalSequenceA[i], originalSequenceB[j])
                            ++i
                            ++j
                        } else {
                            if (end1 - i > end2 - j) {
                                editScript.appendDelete(originalSequenceA[i])
                                ++i
                            } else {
                                editScript.appendInsert(originalSequenceB[j])
                                ++j
                            }
                        }
                    }
                } else {
                    buildScript(start1, middle.start, start2, middle.start - middle.diag)

                    for (i in middle.start until middle.end) {
                        editScript.appendKeep(originalSequenceA[i], originalSequenceB[i - middle.diag])
                    }

                    buildScript(middle.end, end1, middle.end - middle.diag, end2)
                }
            }

            // do not forget to add the collected keep commands at the end.
            for ((origObj, newObj) in commands) {
                editScript.appendKeep(origObj, newObj)
            }
        }
    }

    /**
     * This class is a simple placeholder to hold the end part of a path.
     */
    private class Snake constructor(val start: Int, val end: Int, val diag: Int)
}