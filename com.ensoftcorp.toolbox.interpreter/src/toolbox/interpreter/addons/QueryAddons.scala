package toolbox.interpreter.addons

import com.ensoftcorp.atlas.java.core.query.Q
import com.ensoftcorp.atlas.java.core.db.set.AtlasHashSet
import com.ensoftcorp.atlas.java.core.db.graph.operation.InducedGraph
import com.ensoftcorp.atlas.java.core.db.graph.GraphElement
import com.ensoftcorp.atlas.java.core.script.Common._
import scala.collection.JavaConversions._
import com.ensoftcorp.atlas.java.core.db.graph.GraphElement
import com.ensoftcorp.atlas.java.core.db.graph.EdgeGraph
import com.ensoftcorp.atlas.java.core.highlight.Highlighter
import com.ensoftcorp.atlas.java.core.script.Common
import com.ensoftcorp.atlas.java.core.db.set.DifferenceSet
import com.ensoftcorp.atlas.java.interpreter.lib

/**
 * Adds some methods to Q that are available on the interpreter (only)
 */
class QueryAddons(query: Q) {

  /**
   * Finds paths between from and to of length at most limit.
   */
  def between(from: Q, to: Q, limit: Int): Q = {
    var context = forwardSteps(from, limit)

    context.between(from, to)
  }

  /**
   * Shows the graph difference between this query and the other.
   */
  def diff(other: Q) = {
    var h = new Highlighter
    h.highlight(query difference other, java.awt.Color.RED)
    var edgeDiff = new DifferenceSet(query.eval.edges, other.eval.edges)
    h.highlightEdges(toQ(new InducedGraph(query.eval.nodes, edgeDiff)), java.awt.Color.RED)
    show(h)
  }

  /**
   * Removes ONLY EDGES from the second Q from the first Q. Does not remove
   * nodes from the first Q.
   */
  def differenceEdges(second: Q): Q = {
    var edgeDiff = new DifferenceSet(query.eval.edges, second.eval.edges)
    toQ(new InducedGraph(query.eval.nodes, edgeDiff))
  }

  /**
   * Finds the paths of shortest length between from and to
   */
  def betweenShortestPath(from: Q, to: Q): Q = {
    // Return immediately if no path
    if (query.between(from, to).eval.nodes.isEmpty) return nothing

    var context = from

    for (l <- 1 to 1000) {
      // Allow the context to expand forward one step
      var nextContext = context union query.forwardStep(context)

      // Discover new nodes we haven't seen before
      var oldNodes = context.retainNodes
      var newNodes = nextContext.retainNodes
      var newlySeen = newNodes difference oldNodes

      // Add in the new nodes only (no cycles back to old nodes)
      context = nextContext.between(oldNodes, newlySeen)

      // If a path is found now, no need to expand context further. Return it!
      var goaltest = context.between(from, to)
      if (!goaltest.eval.nodes.isEmpty) return goaltest
    }

    return nothing
  }

  /**
   * Wraps Q method with method that works for any object.
   */
  def nodesTaggedWithAny(itags: Object*): Q = {
    query.nodesTaggedWithAny(itags.map(x => x.toString): _*)
  }

  /**
   * Wraps Q method with method that works for any object.
   */
  def nodesTaggedWithAll(itags: Object*): Q = {
    query.nodesTaggedWithAll(itags.map(x => x.toString): _*)
  }

  /**
   * Wraps Q method with method that works for any object.
   */
  def edgesTaggedWithAny(itags: Object*): Q = {
    query.edgesTaggedWithAny(itags.map(x => x.toString): _*)
  }

  /**
   * Wraps Q method with method that works for any object.
   */
  def edgesTaggedWithAll(itags: Object*): Q = {
    query.edgesTaggedWithAll(itags.map(x => x.toString): _*)
  }

  /**
   * Selects *only* edges matching the given attribute from the Q. All nodes are
   * discarded except those which are edge endpoints.
   */
  def selectOnlyEdges(key: java.lang.String, values: Object*): Q = {
    toQ(new EdgeGraph(query.selectEdge(key, values: _*).eval.edges))
  }

  /**
   * Selects *only* edges matching the given attribute from the Q. All nodes are
   * discarded except those which are edge endpoints.
   */
  def selectOnlyEdges(key: java.lang.String): Q = {
    toQ(new EdgeGraph(query.selectEdge(key).eval.edges))
  }

  /**
   * Wraps the Graph isomorphism check.
   */
  def isomorphic(other: Q): Boolean = {
    query.eval.isomorphic(other.eval)
  }

  /**
   * Do repeated reverse steps
   */
  def reverseSteps(in: Q, steps: Integer): Q = {

    var stepped = in

    for (i <- 0 until steps) {
      stepped = query.reverseStep(stepped)
    }

    stepped
  }

  /**
   * Do repeated forward steps
   */
  def forwardSteps(in: Q, steps: Integer): Q = {

    var stepped = in

    for (i <- 0 until steps) {
      stepped = query.forwardStep(stepped)
    }

    stepped
  }

  /**
   * Do repeated stepTo
   */
  def stepsTo(in: Q, steps: Integer): Q = {
    var stepped = in

    for (i <- 0 until steps) {
      stepped = stepTo(in, query)
    }

    stepped
  }

  /**
   * Do repeated stepTo
   */
  def stepsFrom(in: Q, steps: Integer): Q = {
    var stepped = in

    for (i <- 0 until steps) {
      stepped = stepFrom(in, query)
    }

    stepped
  }

  /**
   * Select nodes
   */
  def filter(selector: GraphElement => Boolean): Q = {

    var nodes = new AtlasHashSet[GraphElement]()

    var graph = query.eval()

    for (element <- graph.nodes().iterator().toList) {
      if (selector(element)) {
        nodes.add(element)
      }
    }

    toQ(new InducedGraph(nodes, graph.edges()))
  }

  /**
   * Forward walk.
   * Equivalent to
   * <code>
   *  along.forward(query)
   * </code>
   * Reverses order of arguments to facilitate chaining.
   */
  def fwd(along: Q): Q = {
    along.forward(query)
  }

  /**
   * Forward walk.
   * Equivalent to
   * <code>
   *  along.forward(query)
   * </code>
   * Reverses order of arguments to facilitate chaining.
   */
  def fwd(alongEdgeTypes: String*): Q = {
    edges(alongEdgeTypes: _*).forward(query)
  }

  /**
   * Reverse walk.
   * Equivalent to
   * <code>
   *  along.reverse(query)
   * </code>
   * Reverses order of arguments to facilitate chaining.
   */
  def rev(along: Q): Q = {
    along.reverse(query)
  }

  /**
   * show expression, returning expression (to facilitate chaining)
   */
  def show: Q = {
    // NOTE: this is not recursive; this form just takes all defaults, permitting omission of parenthesis
    show()
  }

  /**
   * show expression, returning expression (to facilitate chaining)
   */
  def show(highlighter: Highlighter = new Highlighter, extend: Boolean = true, title: String = null): Q = {
    import com.ensoftcorp.atlas.java.interpreter.lib
    lib.Common.show(q = query, highlighter = highlighter, extend = extend, title = title);
    printSize
    query
  }

  /**
   * Print size of expression, returning expression (to facilitate chaining)
   */
  def printSize(): Q = {
    val g = query.eval;
    val out = g.nodes.size + " nodes " + g.edges.size + " edges";
    println(out)
    query
  }

  /**
   * Print qualified names of nodes
   */
  def printNames(): String = {
    val list = new java.util.ArrayList[String]();
    val buf = new StringBuilder
    val g = query.eval;
    val itr = g.nodes.iterator
    while (itr.hasNext) {
      val ge = itr.next
      list.add(getQualifiedName(ge))
    }

    // lexical sort, which is better than nothing, but unfortunately sorts by return type instead of package
    java.util.Collections.sort(list)

    for (name <- list) {
      buf.append(name)
      buf.append("\n")
    }

    buf.toString
  }
}

object QueryAddons {

  import com.ensoftcorp.atlas.java.core.db.graph.GraphElement
  import com.ensoftcorp.atlas.java.core.script.Common._
  import scala.language.implicitConversions
  
  
  /**
   * Implicitly add some functionality to queries
   */
  implicit def query2addonquery(query: Q) = new QueryAddons(query)

}

class GraphElementAddons(element: GraphElement) {

  import com.ensoftcorp.atlas.java.core.script.Common

  def toQ() = {
    Common.toQ(Common.toGraph(element))
  }
}

object GraphElementAddons {

  import scala.language.implicitConversions
  
  /**
   * Implicitly convert graph elements back to queries
   */
  implicit def graphelementwithaddons(element: GraphElement): GraphElementAddons = new GraphElementAddons(element)
}