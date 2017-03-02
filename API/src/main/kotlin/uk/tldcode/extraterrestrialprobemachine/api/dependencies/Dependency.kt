package uk.tldcode.extraterrestrialprobemachine.api.dependencies

import org.jgrapht.alg.CycleDetector
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.traverse.TopologicalOrderIterator
import java.util.*

object Dependency {

    operator fun invoke(map: Map<String, List<String>>): Stack<String> {
        val cycleDetector: CycleDetector<String, DefaultEdge>
        val graph = DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java)
        map.keys.forEach { graph.addVertex(it) }
        map.forEach { key, value -> value.forEach { graph.addEdge(key, it) } }
        cycleDetector = CycleDetector<String, DefaultEdge>(graph)
        if (cycleDetector.detectCycles()) {
            var iterator: Iterator<String>
            val cycleVertices: MutableSet<String> = cycleDetector.findCycles()
            var subCycle: Set<String>
            var cycle: String
            while (!cycleVertices.isEmpty()) {
                iterator = cycleVertices.iterator()
                cycle = iterator.next()
                subCycle = cycleDetector.findCyclesContainingVertex(cycle)
                for (sub in subCycle) {
                    cycleVertices.remove(sub)
                    graph.removeVertex(sub)
                }
            }
        }
        val orderIterator = TopologicalOrderIterator <String, DefaultEdge>(graph)
        val stack: Stack<String> = Stack()
        while (orderIterator.hasNext()) {
            val v = orderIterator.next()
            stack.push(v)
        }
        return stack

    }
}