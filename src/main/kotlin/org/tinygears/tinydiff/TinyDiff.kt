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

import org.tinygears.tinydiff.algorithm.MyersDiffAlgorithm
import org.tinygears.tinydiff.transform.Transformer
import org.tinygears.tinydiff.util.readSequence
import java.nio.file.Path
import java.nio.file.Paths

object TinyDiff {
    fun diff(baseDir: Path, origFile: String, modifiedFile: String, vararg transformers: Transformer<String>): Patch {
        val origFilePath     = baseDir.resolve(origFile)
        val modifiedFilePath = baseDir.resolve(modifiedFile)

        val origList = readSequence(origFilePath)
        val newList  = readSequence(modifiedFilePath)

        val algo   = MyersDiffAlgorithm<String>()
        val script = algo.getEditScript(origList, newList, *transformers)

        return Patch(origFile, modifiedFile, script)
    }

    fun diff(origFileName: String, modifiedFileName: String, vararg transformers: Transformer<String>): Patch {
        return diff(Paths.get(origFileName), Paths.get(modifiedFileName), *transformers)
    }

    fun diff(origFilePath: Path, modifiedFilePath: Path, vararg transformers: Transformer<String>): Patch {
        val origList = readSequence(origFilePath)
        val newList  = readSequence(modifiedFilePath)

        val algo   = MyersDiffAlgorithm<String>()
        val script = algo.getEditScript(origList, newList, *transformers)

        return Patch(origFilePath.toString(), modifiedFilePath.toString(), script)
    }
}