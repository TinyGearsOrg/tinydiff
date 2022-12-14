/*
 * Copyright 2012 Thomas Neidhart.
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
package org.tinygears.tinydiff.util

import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.inputStream

internal fun readSequence(fileName: String): List<String> {
    return readSequence(Paths.get(fileName))
}

internal fun readSequence(path: Path): List<String> {
    path.inputStream().use { `is` -> return readSequence(`is`) }
}

internal fun readSequence(`is`: InputStream): List<String> {
    Scanner(`is`).use { scanner ->
        val list = mutableListOf<String>()
        while (scanner.hasNextLine()) {
            var line = scanner.findWithinHorizon(".*\r?\n", 0)
            if (line == null) {
                line = scanner.nextLine()
            }
            list.add(line)
        }
        return list
    }
}
