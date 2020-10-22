package com.roteswasser.spotifysync

import com.roteswasser.spotifysync.algorithms.computeLCS
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class LongestCommonSubsequenceTest {
    @Test
    fun `LCS example from Wikipedia is computed correctly`() {
        // See: https://en.wikipedia.org/wiki/Longest_common_subsequence_problem#Solution_for_two_sequences
        val lcs = computeLCS(listOf("A", "G", "C", "A", "T"), listOf("G", "A", "C"))

        Assertions.assertTrue(lcs in listOf(listOf("A", "C"), listOf("G", "C"), listOf("G", "A")))
    }
}