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
 * This interface is devoted to handle synchronized replacement sequences.
 *
 * @see ReplacementsFinder
 */
internal interface ReplacementsHandler<T> {
    /**
     * Handle two synchronized sequences.
     *
     * This method is called by a [ReplacementsFinder]
     * instance when it has synchronized two sub-sequences of object arrays
     * being compared, and at least one of the sequences is non-empty. Since the
     * sequences are synchronized, the objects before the two sub-sequences are
     * equal (if they exist). This property also holds for the objects after
     * the two sub-sequences.
     *
     * The replacement is defined as replacing the `from`
     * sub-sequence into the `to` sub-sequence.
     *
     * @param skipped  number of tokens skipped since the last call (i.e. number of
     * tokens that were in both sequences), this number should be strictly positive
     * except on the very first call where it can be zero (if the first object of
     * the two sequences are different)
     * @param from  sub-sequence of objects coming from the first sequence
     * @param to  sub-sequence of objects coming from the second sequence
     */
    fun handleReplacement(skipped: Int, from: List<T>, to: List<T>)

    /**
     * Method called when a keep command is encountered.
     *
     * This is a convenience method as some output formatters need context information.
     * The number of keep objects between two calls of [handleReplacement]
     * is equivalent to the parameters `skipped`.
     *
     * NOTE: in case an [EditScript] has been loaded from a diff file,
     * the keep information from the original file may be lost, thus [obj]
     * may be `null` in such cases.
     *
     * @param origObj     object to keep (this object comes from the first sequence)
     * @param modifiedObj object to keep (this object comes from the second sequence)
     */
    fun handleKeep(origObj: T?, modifiedObj: T?)
}