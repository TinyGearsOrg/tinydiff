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

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.tinygears.tinydiff.format.unifiedDiff
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.BasicFileAttributes
import java.util.function.BiPredicate
import kotlin.io.path.name
import kotlin.io.path.outputStream
import kotlin.io.path.readText
import kotlin.streams.toList

class IntegrationTest {

    @ParameterizedTest
    @MethodSource("integrationTests")
    fun integrationTest(path: Path) {
        val testCase = path.name

        val patch = TinyDiff.diff(path, "a.txt", "b.txt")

        val fileResult = path.resolve("output.txt")
        fileResult.outputStream().use { os ->
            patch.apply(path, os)
        }

        assertEquals(testCase, path.resolve("b.txt"), fileResult)

        val actualPatch = path.resolve("actual.patch")
        actualPatch.outputStream().use { os ->
            unifiedDiff(os).format(patch)
        }

        val expectedPatch = path.resolve("expected.patch")
        assertEquals(testCase, expectedPatch, actualPatch)
    }

    private fun assertEquals(testCase: String, fileA: Path, fileB: Path) {
        val contentA = fileA.readText()
        val contentB = fileB.readText()

        assertEquals(contentB, contentA, "$testCase: contents of file $fileA $fileB are not equal")
    }

    companion object {
        private val TEST_DIR = BiPredicate { path: Path, attr: BasicFileAttributes -> attr.isDirectory && path.name.matches("test\\d+".toRegex()) }

        @JvmStatic
        fun integrationTests(): List<Arguments> {
            val markerFile  = IntegrationTest::class.java.getResource("/integration/marker.txt")!!.file
            val baseTestDir = Paths.get(markerFile).parent

            val testDirectories = Files.find(baseTestDir, Int.MAX_VALUE, TEST_DIR)

            return testDirectories.use { it.map { path -> arguments(path) }.toList() }
        }
    }

}