package helper

data class Transfer(val from: Int, val to: Int, val amount: Float)

class Calculator(deltas_array: FloatArray) {
    private val deltas = deltas_array
    private val n = deltas.size
    private val powN = 1 shl n
    private val eps = 1e-3

    private fun findMasks(): BooleanArray {
        val masks = BooleanArray(powN)
        for (i in 0 until powN) {
            var sum = 0f
            for (j in 0 until n) {
                if (i and (1 shl j) != 0)
                    sum += deltas[j]
            }
            if (-eps < sum && sum < eps)
                masks[i] = true
        }
        return masks
    }

    private fun getMaxPrevMask(masks: BooleanArray): IntArray {
        val dp = IntArray(powN)
        val maxPrevMask = IntArray(powN)
        for (m in 0 until powN) {
            var s = m
            while (s != 0) {
                if (masks[s]) {
                    val f = m xor s
                    if (dp[m] < dp[f] + 1) {
                        dp[m] = dp[f] + 1
                        maxPrevMask[m] = s
                    }
                }
                s = (s - 1) % m
            }
        }
        return maxPrevMask
    }

    fun bitMaskCalculate(): Array<Transfer> { //O(3^n) Only n <= 18 is suitable
        val masks = findMasks()
        val previousMask = getMaxPrevMask(masks)
        val transfers = MutableList(0) {Transfer(0, 0, 0f)}
        val remains = deltas.toTypedArray()
        var i = powN - 1

        while(i != 0) {
            val positive = MutableList(0){0}
            val negative = MutableList(0){0}

            for (j in 0 until n) {
                if (i and (1 shl j) != 0) {
                    if (remains[j] > eps)
                        positive.add(j)
                    else if (remains[j] < -eps)
                        negative.add(j)
                }
            }

            while(positive.size != 0) {
                val a = positive.removeLast(); val b = negative.removeLast()
                transfers.add(Transfer(b, a, remains[a]))
                remains[b] += remains[a]
                remains[a] = 0f
                if (remains[b] > eps)
                    positive.add(b)
                else if (remains[b] < -eps)
                    negative.add(b)
            }

            i = i xor previousMask[i]
        }
        println("calculated")
        return transfers.toTypedArray()
    }

    fun greedyCalculate(): Array<Transfer> { //O(n*log(n))
        val remains = deltas.toTypedArray()
        remains.sort()
        val transfers = Array(n - 1) {Transfer(0, 0, 0f)}
        for (i in 0 until n - 1) {
            transfers[i] = Transfer(i, i + 1, -remains[i])
            remains[i + 1] += remains[i]
        }
        return transfers
    }

    fun getTransfers(): Array<Transfer> {
        println("called")
        if (n <= 18)
            return bitMaskCalculate()
        return greedyCalculate()
    }
}