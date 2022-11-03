/*
 * Copyright 2013 Thomas Neidhart.
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

//import org.netomi.jdiffutils.transform.Transformer
//
///**
// *
// *
// * @param <T>
//</T> */
//class OptimizedDiffAlgorithm<T> : DiffAlgorithm<T> {
//    override fun getEditScript(
//        origSequence: List<T>, newSequence: List<T>,
//        transformer: Transformer<T>
//    ): EditScript<T> {
//        val map: MutableMap<T, EquivalenceClass<T>> =
//            HashMap()
//        val transformedA: MutableList<T> = ArrayList(origSequence.size)
//        for (a in origSequence) {
//            val transformed = if (transformer == null) a else transformer.transform(a)
//            transformedA.add(transformed)
//            var c = map[transformed]
//            if (c == null) {
//                c = EquivalenceClass(transformed)
//                map[transformed] = c
//            }
//            c.incA()
//        }
//        val transformedB: MutableList<T> = ArrayList(newSequence.size)
//        for (b in newSequence) {
//            val transformed = if (transformer == null) b else transformer.transform(b)
//            transformedB.add(transformed)
//            var c = map[transformed]
//            if (c == null) {
//                c = EquivalenceClass(transformed)
//                map[transformed] = c
//            }
//            c.incB()
//        }
//        val reverseMap: Array<EquivalenceClass<T>> =
//            arrayOfNulls<EquivalenceClass<*>>(map.size)
//        var index = 0
//        var countA = 0
//        var countB = 0
//        for ((_, c) in map) {
//            if (c.getCountA() > 0 && c.getCountB() > 0) {
//                c.setIndex(index)
//                reverseMap[index] = c
//                countA += c.getCountA()
//                countB += c.getCountB()
//                index++
//            }
//        }
//
//        // TODO: find better heuristic when to omit the transformation
//        //        as the plain myers algorithm will be faster, i.e. because
//        //        the number of estimated changes is very small
//        if (countA + countB > origSequence.size) {
//            return MyersDiffAlgorithm<T>().getEditScript(origSequence, newSequence, transformer)
//        }
//        val listA: MutableList<Int> = ArrayList(countA)
//        val listB: MutableList<Int> = ArrayList(countB)
//        val indexA: MutableList<Int> = ArrayList(countA)
//        val indexB: MutableList<Int> = ArrayList(countB)
//        var lineNo = 0
//        for (s in transformedA) {
//            val c = map[s]!!
//            if (c.getIndex() >= 0) {
//                listA.add(c.getIndex())
//                indexA.add(lineNo)
//            }
//            lineNo++
//        }
//        lineNo = 0
//        for (s in transformedB) {
//            val c = map[s]!!
//            if (c.getIndex() >= 0) {
//                listB.add(c.getIndex())
//                indexB.add(lineNo)
//            }
//            lineNo++
//        }
//        val diff: DiffAlgorithm<Int> = MyersDiffAlgorithm()
//        val script = diff.getEditScript(listA, listB, null)
//        val visitor =
//            ReverseTransformVisitor(origSequence, newSequence, reverseMap, indexA, indexB)
//        script.visit(visitor)
//        return visitor.getScript()
//    }
//
//    private class ReverseTransformVisitor<T>(
//        var listA: List<T>, var listB: List<T>, var map: Array<EquivalenceClass<*>>,
//        var indexA: List<Int>, var indexB: List<Int>
//    ) : CommandVisitor<Int?> {
//        var lineNoA = 0
//        var lineNoB = 0
//        var lastA = 0
//        var lastB = 0
//        var script: EditScript<T>
//
//        init {
//            script = EditScript()
//        }
//
//        override fun visitInsertCommand(`object`: Int) {
//            val next = indexB[lineNoB]
//            while (next >= lastB) {
//                script.appendInsert(listB[lastB++])
//            }
//            lineNoB++
//        }
//
//        override fun visitKeepCommand(`object`: Int?) {
//            val nextA = indexA[lineNoA]
//            val nextB = indexB[lineNoB]
//            while (nextA > lastA) {
//                script.appendDelete(listA[lastA++])
//            }
//            while (nextB > lastB) {
//                script.appendInsert(listB[lastB++])
//            }
//            val c: EquivalenceClass<T> = map[`object`!!.toInt()]
//            script.appendKeep(c.getLine())
//            lastA++
//            lastB++
//            lineNoA++
//            lineNoB++
//        }
//
//        override fun visitDeleteCommand(`object`: Int) {
//            val next = indexA[lineNoA]
//            while (next >= lastA) {
//                script.appendDelete(listA[lastA++])
//            }
//            lineNoA++
//        }
//
//        fun startVisit() {}
//        fun finishVisit() {
//            while (lastA < listA.size) {
//                script.appendDelete(listA[lastA++])
//            }
//            while (lastB < listB.size) {
//                script.appendInsert(listB[lastB++])
//            }
//        }
//    }
//
//    /**
//     * A class to represent equivalence classes of input data.
//     *
//     *
//     * An equivalence class corresponds to a set of input data that
//     * is supposed to be equal based on some criteria, e.g. after
//     * transformation of the input data to lower case.
//     *
//     * @param <T>
//    </T> */
//    private class EquivalenceClass<T>(val line: T) {
//        var index: Int
//        var countA: Int
//            private set
//        var countB: Int
//            private set
//
//        init {
//            index = -1
//            countA = 0
//            countB = 0
//        }
//
//        fun incA() {
//            countA++
//        }
//
//        fun incB() {
//            countB++
//        }
//    }
//}