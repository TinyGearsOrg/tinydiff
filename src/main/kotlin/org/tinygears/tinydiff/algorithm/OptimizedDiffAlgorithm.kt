/*
 * Copyright 2022 Thomas Neidhart.
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

/**
 * An optimized version of a [DiffAlgorithm].
 *
 * The runtime complexity of the Myers Diff algorithm is O(ND) which means if there are a great
 * number of differences, it becomes quadratic. The optimized version tries to circumvent that
 * by only generating the optimal edit script for elements of the sequences that are present in
 * both. The elements that are only present in the original or modified sequence will then be
 * inserted or deleted separately to speed up the computation.
 */
internal class OptimizedDiffAlgorithm<T>: DiffAlgorithm<T> {
    override fun getEditScript(origSequence: List<T>, modifiedSequence: List<T>, vararg transformers: Transformer<T>): EditScript<T> {

        val inputToEquivalenceClassMap: MutableMap<T, EquivalenceClass<T>> = mutableMapOf()

        val origSequenceTransformed     = origSequence.map { transform(it, *transformers) }.toList()
        val modifiedSequenceTransformed = modifiedSequence.map { transform(it, *transformers) }.toList()

        val mapEquivalenceClass: (List<T>, (EquivalenceClass<T>) -> Unit) -> Unit = { sequence, func ->
            for (obj in sequence) {
                func(inputToEquivalenceClassMap.computeIfAbsent(obj) { key -> EquivalenceClass(key) })
            }
        }

        mapEquivalenceClass.invoke(origSequenceTransformed)     { c -> c.countA++ }
        mapEquivalenceClass.invoke(modifiedSequenceTransformed) { c -> c.countB++ }

        val reverseMapping: Array<EquivalenceClass<T>?> = arrayOfNulls(inputToEquivalenceClassMap.size)

        var index  = 0
        var countA = 0
        var countB = 0

        for ((_, equivalenceClass) in inputToEquivalenceClassMap) {
            if (equivalenceClass.isPresentInBothSequences) {
                equivalenceClass.index = index
                reverseMapping[index++] = equivalenceClass
                countA += equivalenceClass.countA
                countB += equivalenceClass.countB
            }
        }

        val origSequenceEquivalence     = ArrayList<Int>(countA)
        val modifiedSequenceEquivalence = ArrayList<Int>(countB)

        val indexToLineOrig     = ArrayList<Int>(countA)
        val indexToLineModified = ArrayList<Int>(countB)

        val createReverseMapping: (List<T>, MutableList<Int>, MutableList<Int>) -> Unit = { input, output, mapping ->
            for ((lineNumber, obj) in input.withIndex()) {
                val equivalenceClass = inputToEquivalenceClassMap[obj]!!
                if (equivalenceClass.index >= 0) {
                    output.add(equivalenceClass.index)
                    mapping.add(lineNumber)
                }
            }
        }

        createReverseMapping(origSequenceTransformed, origSequenceEquivalence, indexToLineOrig)
        createReverseMapping(modifiedSequenceTransformed, modifiedSequenceEquivalence, indexToLineModified)

        val script  = MyersDiffAlgorithm<Int>().getEditScript(origSequenceEquivalence, modifiedSequenceEquivalence)
        val visitor = ReverseTransformVisitor(origSequence, modifiedSequence, reverseMapping, indexToLineOrig, indexToLineModified)
        script.accept(visitor)
        return visitor.script
    }
}

/**
 * A class to represent equivalence classes of input data.
 *
 * An equivalence class corresponds to a set of input data that
 * is supposed to be equal based on some criteria, e.g. after
 * transformation of the input data to lower case.
 */
private class EquivalenceClass<T>(val line: T) {
    var index: Int = -1

    var countA: Int = 0
    var countB: Int = 0

    val isPresentInBothSequences: Boolean
        get() = countA > 0 && countB > 0

    override fun toString(): String {
        return "EquivalenceClass[$line]"
    }
}

private class ReverseTransformVisitor<T>(var listA:  List<T>,
                                         var listB:  List<T>,
                                         var map:    Array<EquivalenceClass<T>?>,
                                         var indexA: List<Int>,
                                         var indexB: List<Int>) : CommandVisitor<Int> {
    var lineNoA = 0
    var lineNoB = 0

    var lastA = 0
    var lastB = 0

    var script: EditScript<T> = EditScript.empty()

    override fun visitInsertCommand(obj: Int) {
        val next = indexB[lineNoB]
        while (next >= lastB) {
            script.appendInsert(listB[lastB++])
        }
        lineNoB++
    }

    override fun visitKeepCommand(origObj: Int?, modifiedObj: Int?) {
        val nextA = indexA[lineNoA]
        val nextB = indexB[lineNoB]
        while (nextA > lastA) {
            script.appendDelete(listA[lastA++])
        }
        while (nextB > lastB) {
            script.appendInsert(listB[lastB++])
        }

        val objA = listA[indexA[lineNoA]]
        val objB = listB[indexB[lineNoB]]

        script.appendKeep(objA, objB)

        lastA++
        lastB++
        lineNoA++
        lineNoB++
    }

    override fun visitDeleteCommand(obj: Int) {
        val next = indexA[lineNoA]
        while (next >= lastA) {
            script.appendDelete(listA[lastA++])
        }
        lineNoA++
    }

    override fun visitStart() {}

    override fun visitEnd() {
        while (lastA < listA.size) {
            script.appendDelete(listA[lastA++])
        }
        while (lastB < listB.size) {
            script.appendInsert(listB[lastB++])
        }
    }
}
