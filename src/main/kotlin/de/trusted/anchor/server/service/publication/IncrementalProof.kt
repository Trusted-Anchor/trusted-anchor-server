package de.trusted.anchor.server.service.publication

import org.bouncycastle.operator.DigestCalculator
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.*


/**
 * (N0), (N1, I0,1), (N2), (N3, I2.1, I0.2)
 */
class IncrementalProof(
    val outputStream: OutputStream,
    val factory: Function1<Unit, DigestCalculator>
) {

    // private val outputStream: OutputStream = BufferedOutputStream(FileOutputStream(proofName + ".txt"));
    private var id: Int = 0
    private var treeHeight = 0
    private val nodes: LinkedList<ByteArray> = LinkedList()

    fun add(hash: ByteArray) {
        writeNode(hash)
        nodes.add(hash)
        if (id % 2 == 0) {
            var incrementalProof = computeBasendOnHead()
            writeIncremental(incrementalProof)
            nodes.add(incrementalProof)

            // beweise nachträglich erzeugen bei neuer spitze
            if (isPowerOfTwo(id) && id > 2) {
                val power = kotlin.math.log2(id.toDouble()).toInt() - 1
                for (i in 1..power) {
                    writeIncrementalProof()
                }

                treeHeight = id
                return
            }

            // beweise für subbaum
            val subtree = id - treeHeight
            val power = kotlin.math.log2(subtree.toDouble()).toInt() - 1
            println("id: " + id + " treeheight: " + treeHeight + " subtree: " + subtree + " power: " + power + " list: " + nodes.size)
            if (isPowerOfTwo(subtree)) {
                println("subtreegen " + power)
                for (i in 1..power) {
                    writeIncrementalProof()
                }
            }
        }
    }

    private fun writeIncrementalProof() {
        var incrementalProof1 = computeBasendOnHead()
        writeIncremental(incrementalProof1)
        nodes.add(incrementalProof1)
    }

    fun isPowerOfTwo(x: Int): Boolean {
        return x and (x - 1) == 0
    }

    private fun computeBasendOnHead(): ByteArray {
        val digestCalculator = factory.invoke(Unit)
        var last = nodes.removeLast()
        var beforeLast = nodes.removeLast()
        digestCalculator.outputStream.write(beforeLast)
        digestCalculator.outputStream.write(last)
        return digestCalculator.digest
    }

    private fun writeNode(hash: ByteArray) {
        val s = "N" + id++ + ": " + String(hash, StandardCharsets.UTF_8) + '\n'
        // print(s)
        outputStream.write(
            s.toByteArray(
                StandardCharsets.UTF_8
            )
        )
    }

    private fun writeIncremental(hash: ByteArray) {
        var s = "I" + id + ": " + String(
            hash,
            StandardCharsets.UTF_8
        ) + '\n'

        //print(s)
        outputStream.write(
            s.toByteArray(StandardCharsets.UTF_8)
        )
    }
}

/**
1 = 0
2 = 1
3 = 0
4 = 2

5 = 0
6 = 1
7 = 0
8 = 3

9 = 0
10 = 1
11 = 0
12 = 2

13 = 0
14 = 1
15 = 0
16 = 4

17 = 0
18 = 1
19 = 0
20 = 2

21 = 0
22 = 1
23 = 0
24 = 3

25 = 0
26 = 1
27 = 0
28 = 2

29 = 0
30 = 1
31 = 0
32 = 5

 */