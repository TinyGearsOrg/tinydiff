tinydiff
====

This is a small library for creating diff-like comparisons of files / streams written in `kotlin`.
It provides an implementation of the comparison algorithm described in
*An O(ND) Difference Algorithm and its Variations* by Eugene W. Myers.

## Supported output formats

* classic diff
* edit script
* unified format
* side-by-side output

## Example

Here is a quick example:

```kotlin
val fileA = "original.txt"
val fileB = "modified.txt"
val patch = TinyDiff.diff(fileA, fileB)

// print a side-by-side diff to the console
sideBySide(80).format(patch);

// apply the patch to the original file
patch.apply()
```
