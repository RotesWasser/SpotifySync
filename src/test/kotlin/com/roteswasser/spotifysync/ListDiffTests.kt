package com.roteswasser.spotifysync

import com.roteswasser.spotifysync.algorithms.ListDiff
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class ListDiffTests {
    @Test
    fun `No difference leads to no changes`() {
        val targetList = listOf("A", "B", "C")
        val currentList = listOf("A", "B", "C")

        val diff = ListDiff.createFrom(currentList, targetList)

        Assertions.assertTrue(diff.additions.isEmpty())
        Assertions.assertTrue(diff.removals.isEmpty())
    }

    @Test
    fun `Having an Element more in the Target leads to a correct addition of it`() {
        val targetList = listOf("A", "B", "C")
        val currentList = listOf("A", "B")

        val diff = ListDiff.createFrom(currentList, targetList)

        Assertions.assertTrue(diff.additions.size == 1)
        Assertions.assertTrue(diff.additions[0].position == 2)

        Assertions.assertTrue(diff.additions[0].elements.size == 1)
        Assertions.assertTrue(diff.additions[0].elements[0] == "C")

        Assertions.assertTrue(diff.removals.isEmpty())
    }

    @Test
    fun `Subsequent elements are coalesced into one addition operation`() {
        val targetList = listOf("A", "B", "C", "D")
        val currentList = listOf("A", "B")

        val diff = ListDiff.createFrom(currentList, targetList)

        Assertions.assertTrue(diff.additions.size == 1)

        Assertions.assertTrue(diff.additions[0].position == 2)
        Assertions.assertTrue(diff.additions[0].elements.size == 2)
        Assertions.assertTrue(diff.additions[0].elements[0] == "C")
        Assertions.assertTrue(diff.additions[0].elements[1] == "D")

        Assertions.assertTrue(diff.removals.isEmpty())
    }

    @Test
    fun `Objects are removed correctly`() {
        val targetList = listOf("A", "B")
        val currentList = listOf("A", "B", "C")

        val diff = ListDiff.createFrom(currentList, targetList)

        Assertions.assertTrue(diff.additions.isEmpty())

        Assertions.assertTrue(diff.removals.size == 1)
        Assertions.assertTrue(diff.removals[0].position == 2)
        Assertions.assertTrue(diff.removals[0].element == "C")
    }
}