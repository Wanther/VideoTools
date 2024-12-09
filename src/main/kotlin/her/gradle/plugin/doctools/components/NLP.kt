package her.gradle.plugin.doctools.components

import com.hankcs.hanlp.HanLP
import com.hankcs.hanlp.seg.common.Term
import com.hankcs.hanlp.corpus.tag.Nature

class NLP {
    companion object {

        fun seg(content: String): List<Term> {
            return HanLP.segment(content)
        }

        fun similar(v1: String, v2: String): Float {
            val seg1 = HanLP.segment(v1)
            val seg2 = HanLP.segment(v2)

            val ignoreNatures = arrayOf(Nature.w, Nature.uj, Nature.m) 

            val a = seg1.filterNot { it.nature in ignoreNatures }.map{ it.word }
            val b = seg2.filterNot { it.nature in ignoreNatures }.map{ it.word }

            val result = cos(a, b)

            //if (result > 0.8) {
            //    println(a)
            //    println(b)
            //}

            return result
        }

        fun cos(a: Collection<String>, b: Collection<String>): Float {
            val union = mutableSetOf<String>()
            
            union.addAll(a)
            union.addAll(b)

            val u = union.toTypedArray()

            val va = IntArray(u.size)
            val vb = IntArray(u.size)

            u.forEachIndexed { i, eu ->
                a.forEach { ea ->
                    if (ea == eu) {
                        va[i]++
                    }
                }
                b.forEach { eb ->
                    if (eb == eu) {
                        vb[i]++
                    }
                }
            }

            return cos(va, vb)

        }

        fun cos(va: IntArray, vb: IntArray): Float {
            if (va.size != vb.size) {
                throw RuntimeException("va.size != vb.size")
            }

            var x = 0
            var ma = 0
            var mb = 0

            (0 until va.size).forEach { i ->
                x += va[i] * vb[i]
                ma += va[i] * va[i]
                mb += vb[i] * vb[i]
            }

            return (x / Math.sqrt(ma.toDouble()) / Math.sqrt(mb.toDouble())).toFloat()
        }
    }
}
