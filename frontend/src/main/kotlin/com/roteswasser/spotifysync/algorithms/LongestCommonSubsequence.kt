package com.roteswasser.spotifysync.algorithms

enum class PredecessorState { None, Above, Left, AboveAndLeft, AppendDiagonalAboveLeft}

data class LCSCell(
        var predecessors: PredecessorState,
        var lcsLength: Int
)

/**
 * Creates an LCS table initialized with [LCSCell] initialized to
 * [PredecessorState.None] and 0.
 *
 * @return 2D Array of [LCSCell] indexed row-first
 */
fun getLCSTable(rows: Int, columns: Int) = Array(rows) { Array(columns) { LCSCell(PredecessorState.None, 0) } }

/**
 * Computes a longest common sequence. Note that there can be multiple.
 *
 * @return A longest common sequence between [first] and [second]
 */
fun <E> computeLCS(first: List<E>, second: List<E>) : List<E> {
    val rowCount = second.size + 1
    val columnCount = first.size + 1
    val lcsTable = getLCSTable(rowCount, columnCount)

    for (row in 1 until rowCount) {
        for (column in 1 until columnCount) {
            val currentCell = lcsTable[row][column]
            val aboveCell = lcsTable[row - 1][column]
            val leftCell = lcsTable[row][column - 1]

            // In the arrays for comparision
            val columnElement = first[column - 1]!!
            val rowElement = second[row - 1]!!

            if (rowElement == columnElement) {
                // Extend
                currentCell.predecessors = PredecessorState.AppendDiagonalAboveLeft
                currentCell.lcsLength = aboveCell.lcsLength + 1
            } else {
                // Same as a previously existing value, point to them
                when {
                    aboveCell.lcsLength == leftCell.lcsLength -> {
                        currentCell.predecessors = PredecessorState.AboveAndLeft
                        currentCell.lcsLength = aboveCell.lcsLength
                    }

                    aboveCell.lcsLength > leftCell.lcsLength -> {
                        currentCell.predecessors = PredecessorState.Above
                        currentCell.lcsLength = aboveCell.lcsLength
                    }

                    leftCell.lcsLength > aboveCell.lcsLength -> {
                        currentCell.predecessors = PredecessorState.Left
                        currentCell.lcsLength = leftCell.lcsLength
                    }
                }
            }

        }
    }

    val lcs: MutableList<E> = mutableListOf()

    // Build LCS via traceback
    var currentColumn = columnCount - 1
    var currentRow = rowCount - 1
    while (lcsTable[currentRow][currentColumn].lcsLength > 0) {
        when(lcsTable[currentRow][currentColumn].predecessors) {
            PredecessorState.None -> throw Exception("Arrived at none state with LCS greater than 0, this should not be possible.")
            PredecessorState.Above -> currentRow--
            PredecessorState.Left -> currentColumn--
            PredecessorState.AboveAndLeft -> currentColumn--
            PredecessorState.AppendDiagonalAboveLeft -> {
                lcs.add(0, first[currentColumn - 1])
                currentRow--
                currentColumn--
            }
        }
    }


    return lcs
}