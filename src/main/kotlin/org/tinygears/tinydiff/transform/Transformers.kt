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
package org.tinygears.tinydiff.transform

import org.tinygears.tinydiff.algorithm.DiffAlgorithm
import java.util.*

fun collapseWhitespace(): Transformer<String> {
    return CollapseWhitespaceTransformer
}

fun removeWhitespace(): Transformer<String> {
    return RemoveWhitespaceTransformer
}

fun removeTrailingWhitespace(): Transformer<String> {
    return RemoveTrailingWhitespaceTransformer
}

fun lowercase(): Transformer<String> {
    return LowerCaseTransformer
}

internal fun <T> transform(obj: T, vararg transformers: Transformer<T>): T {
    var transformed: T = obj
    for (t in transformers) {
        transformed = t.transform(transformed)
    }
    return transformed
}

/**
 * A [Transformer] is used to normalize the input data before passing
 * it to the [DiffAlgorithm].
 *
 * @param <T> the input/output type
 */
interface Transformer<T> {
    /**
     * Transforms the input data.
     *
     * @param input the input data to transform
     * @return the transformed input data
     */
    fun transform(input: T): T
}

/**
 * Collapses any sequence of white-space characters to a single space.
 */
internal object CollapseWhitespaceTransformer: Transformer<String> {
    override fun transform(input: String): String {
        return input.replace("\\s+".toRegex(), " ")
    }
}

/**
 * Transforms the input to lower-case.
 */
internal object LowerCaseTransformer: Transformer<String> {
    override fun transform(input: String): String {
        return input.lowercase(Locale.getDefault())
    }
}

/**
 * Removes any trailing whitespace from the input strings.
 */
internal object RemoveTrailingWhitespaceTransformer: Transformer<String> {
    override fun transform(input: String): String {
        return input.replace("\\s+$".toRegex(), "")
    }
}

/**
 * Removes any whitespace from the input strings.
 */
internal object RemoveWhitespaceTransformer: Transformer<String> {
    override fun transform(input: String): String {
        return input.replace("\\s+".toRegex(), "")
    }
}