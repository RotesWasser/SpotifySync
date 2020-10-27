package com.roteswasser.spotifysync.common.algorithms

class ListDiff<E>(
        val additions: List<ListInsertion<E>>,
        val removals: List<ListRemoval<E>>) {



    companion object {
        fun<E> createFrom(current: List<E>,
                       target: List<E>): ListDiff<E> {
            val lcs = computeLCS(target, current)

            val removals = computeRemovals(lcs, current)
            val additions = computeAdditions(lcs, target)

            return ListDiff(additions, removals)
        }

        private fun<E> computeRemovals(
                lcs: List<E>,
                current: List<E>
        ): List<ListRemoval<E>> {
            val deletions = mutableListOf<ListRemoval<E>>()
            var lcsHead = 0
            for (i in current.indices) {
                if (lcsHead < lcs.size && current[i] == lcs[lcsHead]) {
                    lcsHead++
                } else {
                    deletions.add(ListRemoval(i, current[i]))
                }
            }

            return deletions
        }

        private fun<E> computeAdditions(
                lcs: List<E>,
                target: List<E>
        ): List<ListInsertion<E>> {
            val additions = HashMap<Int, MutableList<E>>()
            var lcsHead = 0
            for (i in target.indices) {
                val currentTrack = target[i]

                if (lcsHead >= lcs.size || currentTrack != lcs[lcsHead]) {
                    if (additions.containsKey(lcsHead))
                        additions[lcsHead]!!.add(currentTrack)
                    else
                        additions[lcsHead] = mutableListOf(currentTrack)
                } else {
                    lcsHead++
                }
            }

            return additions.map { kv -> ListInsertion(kv.key, kv.value) }
        }
    }

    data class ListRemoval<E>(
            val position: Int,
            val element: E
    )

    data class ListInsertion<E>(
            val position: Int,
            val elements: List<E>
    )
}