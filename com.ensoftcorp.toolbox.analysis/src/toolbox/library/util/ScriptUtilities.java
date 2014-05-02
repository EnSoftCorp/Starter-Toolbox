package toolbox.library.util;

import static com.ensoftcorp.atlas.java.core.script.Common.edges;
import static com.ensoftcorp.atlas.java.core.script.Common.getQualifiedName;
import static com.ensoftcorp.atlas.java.core.script.Common.index;
import static com.ensoftcorp.atlas.java.core.script.Common.stepFrom;
import static com.ensoftcorp.atlas.java.core.script.Common.stepTo;
import static com.ensoftcorp.atlas.java.core.script.Common.toQ;
import static com.ensoftcorp.atlas.java.core.script.Common.typeSelect;
import static com.ensoftcorp.atlas.java.core.script.CommonQueries.callStep;
import static com.ensoftcorp.atlas.java.core.script.CommonQueries.declarations;
import static com.ensoftcorp.atlas.java.core.script.CommonQueries.edgeSize;
import static com.ensoftcorp.atlas.java.core.script.CommonQueries.nodeSize;
import static com.ensoftcorp.atlas.java.core.script.CommonQueries.TraversalDirection.FORWARD;
import static com.ensoftcorp.atlas.java.core.script.CommonQueries.TraversalDirection.REVERSE;

import com.ensoftcorp.atlas.java.core.db.graph.Graph;
import com.ensoftcorp.atlas.java.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.java.core.db.graph.GraphElement.EdgeDirection;
import com.ensoftcorp.atlas.java.core.db.graph.NodeGraph;
import com.ensoftcorp.atlas.java.core.db.graph.operation.BetweenGraph;
import com.ensoftcorp.atlas.java.core.db.graph.operation.InducedGraph;
import com.ensoftcorp.atlas.java.core.db.set.AtlasHashSet;
import com.ensoftcorp.atlas.java.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.java.core.db.set.SingletonAtlasSet;
import com.ensoftcorp.atlas.java.core.query.Attr.Edge;
import com.ensoftcorp.atlas.java.core.query.Attr.Node;
import com.ensoftcorp.atlas.java.core.query.Q;

/**
 * Common queries which are useful for writing larger scripts, and for using on
 * the interpreter.
 */
public final class ScriptUtilities {
	private ScriptUtilities() {}

	/**
	 * Types which represent arrays of other types
	 * 
	 * NOTE: These nodes are NOT declared by anything. They are outside of any
	 * project.
	 */
	public static Q arrayTypes() {
		return index().nodesTaggedWithAny(Node.ARRAY_TYPE).retainNodes();
	}

	/**
	 * Types which represent language primitive types
	 * 
	 * NOTE: These nodes are NOT declared by anything. They are outside of any
	 * project.
	 */
	public static Q primitiveTypes() {
		return index().nodesTaggedWithAny(Node.PRIMITIVE_TYPE).retainNodes();
	}

	/**
	 * Summary invoke nodes, representing invocations on methods.
	 * 
	 * NOTE: These nodes are NOT declared by anything. They are outside of any
	 * project.
	 */
	public static Q invokeNodes() {
		return index().nodesTaggedWithAny(Node.INVOKE).retainNodes();
	}

	/**
	 * Everything declared under any of the known API projects, if they are in
	 * the index.
	 */
	public static Q apis() {
		return declarations(index().nodesTaggedWithAny(Node.LIBRARY), FORWARD).difference(arrayTypes(),
				primitiveTypes(), invokeNodes());
	}

	/**
	 * Methods defined in java.lang.Object, and all methods which override them
	 */
	public static Q objectMethodOverrides() {
		return edges(Edge.OVERRIDES).reverse(
				declarations(typeSelect("java.lang", "Object"), FORWARD).nodesTaggedWithAny(Node.METHOD));
	}

	/**
	 * Everything in the universe which is part of the app (not part of the
	 * apis, or any "floating" nodes).
	 */
	public static Q app() {
		return index().difference(apis(), invokeNodes(), arrayTypes(), primitiveTypes());
	}

	/**
	 * All method nodes declared by the APIs.
	 */
	public static Q apiMethods() {
		return apis().nodesTaggedWithAny(Node.METHOD);
	}

	/**
	 * All variable nodes declared by the APIs.
	 */
	public static Q apiVariables() {
		return apis().nodesTaggedWithAny(Node.VARIABLE);
	}

	/**
	 * All data flow nodes declared by the APIs.
	 */
	public static Q apiDFN() {
		return apis().nodesTaggedWithAny(Node.DATA_FLOW);
	}

	/**
	 * All edges for which both endpoints lay within the APIs.
	 */
	public static Q apiEdges() {
		return apis().induce(index());
	}

	/**
	 * Everything declared under the given methods, but NOT declared under
	 * additional methods or types. Retreives declarations of only this method.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q localDeclarations(Q origin) {
		return localDeclarations(index(), origin);
	}

	/**
	 * Everything declared under the given methods, but NOT declared under
	 * additional methods or types. Retreives declarations of only this method.
	 * 
	 * Operates in the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q localDeclarations(Q context, Q origin) {
		Q dec = context.edgesTaggedWithAny(Edge.DECLARES);
		dec = dec.differenceEdges(dec.reverseStep(dec.nodesTaggedWithAny(Node.TYPE)));
		return dec.forward(origin);
	}

	/**
	 * Returns the direct callers of the given methods.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q callers(Q origin) {
		return callers(index(), origin);
	}

	/**
	 * Returns the direct callers of the given methods.
	 * 
	 * Operates in the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q callers(Q context, Q origin) {
		return callStep(context, origin, REVERSE).retainEdges().roots();
	}

	/**
	 * Returns those control flow blocks which directly call the given methods.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q callsites(Q origin) {
		return callsites(index(), origin).nodesTaggedWithAny(Node.CONTROL_FLOW);
	}

	/**
	 * Returns those control flow blocks which directly call the given methods.
	 * 
	 * Operates in the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q callsites(Q context, Q origin) {
		return callers(context, origin).nodesTaggedWithAny(Node.CONTROL_FLOW);
	}

	/**
	 * Returns the subset of the given methods which are called.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q called(Q origin) {
		return called(index(), origin);
	}

	/**
	 * Returns the subset of the given methods which are called.
	 * 
	 * Operates in the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q called(Q context, Q origin) {
		return callStep(context, origin, REVERSE).retainEdges().leaves();
	}

	/**
	 * Returns the given methods which were called by the given callers.
	 * 
	 * Operates in the index context.
	 * 
	 * @param callers
	 * @param called
	 * @return
	 */
	public static Q calledBy(Q callers, Q called) {
		return calledBy(index(), callers, called);
	}

	/**
	 * Returns the given methods which were called by the given callers.
	 * 
	 * Operates in the given context.
	 * 
	 * @param context
	 * @param callers
	 * @param called
	 * @return
	 */
	public static Q calledBy(Q context, Q callers, Q called) {
		return context.edgesTaggedWithAny(Edge.CALL).betweenStep(callers, called).retainEdges().leaves();
	}

	/**
	 * Returns a comma separated string representing the nodes in the query.
	 * 
	 * @param query
	 * @return
	 */
	public static String stringify(Q query) {
		return stringify(query, false);
	}

	/**
	 * Returns a comma separated string representing the nodes in the query.
	 * 
	 * The node names are optionally fully qualified.
	 * 
	 * @param query
	 * @param qualified
	 * @return
	 */
	public static String stringify(Q query, boolean qualified) {
		StringBuilder sb = new StringBuilder();
		String prefix = "";
		for (GraphElement node : query.eval().nodes()) {
			sb.append(prefix);
			if (qualified) {
				sb.append(getQualifiedName(node).trim());
			} else {
				sb.append(node.attr().get(Node.NAME).toString().trim());
			}
			prefix = ",";
		}
		return sb.toString().trim();
	}

	/**
	 * Returns the first declaring node of the given Q which is tagged with one
	 * of the given types.
	 * 
	 * Operates in the index context.
	 * 
	 * @param declared
	 * @param declaratorTypes
	 * @return
	 */
	public static Q firstDeclarator(Q declared, String... declaratorTypes) {
		return firstDeclarator(index(), declared, declaratorTypes);
	}

	/**
	 * Returns the first declaring node of the given Q which is tagged with one
	 * of the given types.
	 * 
	 * Operates in the given context.
	 * 
	 * @param context
	 * @param declared
	 * @param declaratorTypes
	 * @return
	 */
	public static Q firstDeclarator(Q context, Q declared, String... declaratorTypes) {
		Q subContext = declarations(context, declared, REVERSE);
		subContext = subContext.differenceEdges(subContext.reverseStep(subContext.nodesTaggedWithAny(declaratorTypes)));
		return subContext.reverse(declared).nodesTaggedWithAny(declaratorTypes);
	}

	/**
	 * Given two query expressions, intersects the given node and edge kinds to
	 * produce a new expression.
	 * 
	 * @param first
	 * @param second
	 * @param nodeTags
	 * @param edgeTags
	 * @return
	 */
	public static Q advancedIntersection(Q first, Q second, String[] nodeTags, String[] edgeTags) {
		Q plainIntersection = first.intersection(second);

		return plainIntersection.nodesTaggedWithAny(nodeTags).induce(plainIntersection.edgesTaggedWithAny(edgeTags));
	}

	/**
	 * Returns the nodes which directly read from nodes in origin.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q readersOf(Q origin) {
		return readersOf(index(), origin);
	}

	/**
	 * Returns the nodes which directly read from nodes in origin.
	 * 
	 * Operates in the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q readersOf(Q context, Q origin) {
		return stepTo(context.edgesTaggedWithAny(Edge.DF_LOCAL, Edge.DF_INTERPROCEDURAL), origin);
	}

	/**
	 * Returns the nodes which directly write to nodes in origin.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q writersOf(Q origin) {
		return writersOf(index(), origin);
	}

	/**
	 * Returns the nodes which directly write to nodes in origin.
	 * 
	 * Operates in the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q writersOf(Q context, Q origin) {
		return stepFrom(context.edgesTaggedWithAny(Edge.DF_LOCAL, Edge.DF_INTERPROCEDURAL), origin);
	}

	/**
	 * Returns the nodes from which nodes in the origin read.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q readBy(Q origin) {
		return readBy(index(), origin);
	}

	/**
	 * Returns the nodes from which nodes in the origin read.
	 * 
	 * Operates in the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q readBy(Q context, Q origin) {
		return writersOf(context, origin);
	}

	/**
	 * Returns the nodes to which nodes in origin write.
	 * 
	 * Operates in the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q writtenBy(Q origin) {
		return writtenBy(index(), origin);
	}

	/**
	 * Returns the nodes to which nodes in origin write.
	 * 
	 * Operates in the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q writtenBy(Q context, Q origin) {
		return readersOf(context, origin);
	}

	/**
	 * Returns that part of the control flow graph which is part of a loop body.
	 * 
	 * Operates within the index context.
	 * 
	 * @return
	 */
	public static Q loops() {
		return loops(index());
	}

	/**
	 * Returns that part of the control flow graph which is part of a loop body.
	 * 
	 * Operates within the given context.
	 * 
	 * @param context
	 * @return
	 */
	public static Q loops(Q context) {
		Graph cfContext = context.edgesTaggedWithAny(Edge.CONTROL_FLOW).eval();

		AtlasSet<GraphElement> loopNodes = new AtlasHashSet<GraphElement>();
		AtlasSet<GraphElement> loopEdges = new AtlasHashSet<GraphElement>();

		for (GraphElement loop : context.nodesTaggedWithAny(Node.IS_MASTER_LOOP_NODE).eval().nodes()) {
			AtlasSet<GraphElement> loopSet = new SingletonAtlasSet<GraphElement>(loop);
			Graph loopGraph = new BetweenGraph(cfContext, loopSet, loopSet);
			loopNodes.addAll(loopGraph.nodes());
			loopEdges.addAll(loopGraph.edges());
		}

		return toQ(new InducedGraph(loopNodes, loopEdges));
	}

	/**
	 * Returns the control flow graph between conditional nodes and the given
	 * origin.
	 * 
	 * Operates within the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q conditionsAbove(Q origin) {
		return conditionsAbove(index(), origin);
	}

	/**
	 * Returns the control flow graph between conditional nodes and the given
	 * origin.
	 * 
	 * Operates within the given context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q conditionsAbove(Q context, Q origin) {
		Q conditionNodes = context.nodesTaggedWithAny(Node.IS_CONDITION);

		return context.edgesTaggedWithAny(Edge.CONTROL_FLOW).between(conditionNodes, origin);
	}

	/**
	 * Given a Q containing methods or data flow nodes, returns a Q of things
	 * which write to or call things in the Q.
	 * 
	 * Operates within the index context.
	 * 
	 * @param origin
	 * @return
	 */
	public static Q mutators(Q origin) {
		return mutators(index(), origin);
	}

	/**
	 * Returns those nodes in the context which have self edges.
	 * 
	 * @param context
	 * @return
	 */
	public static Q nodesWithSelfEdges(Q context) {
		AtlasSet<GraphElement> res = new AtlasHashSet<GraphElement>();

		for (GraphElement edge : context.eval().edges()) {
			GraphElement to = edge.getNode(EdgeDirection.TO);
			GraphElement from = edge.getNode(EdgeDirection.FROM);
			if (to == from)
				res.add(to);
		}

		return toQ(new NodeGraph(res));
	}

	/**
	 * Given a Q containing methods or data flow nodes, returns a Q of things
	 * which write to or call things in the Q.
	 * 
	 * Operates within the index context.
	 * 
	 * @param context
	 * @param origin
	 * @return
	 */
	public static Q mutators(Q context, Q origin) {
		return writersOf(context, origin).union(callers(context, origin));
	}

	/**
	 * Returns those elements in the origin which were called by or written by
	 * elements in the mutators set.
	 * 
	 * Operates within the index context.
	 * 
	 * @param mutators
	 * @param origin
	 * @return
	 */
	public static Q mutatedBy(Q mutators, Q origin) {
		return mutatedBy(index(), mutators, origin);
	}

	/**
	 * Returns those elements in the origin which were called by or written by
	 * elements in the mutators set.
	 * 
	 * Operates within the given context.
	 * 
	 * @param context
	 * @param mutators
	 * @param origin
	 * @return
	 */
	public static Q mutatedBy(Q context, Q mutators, Q origin) {
		return writtenBy(context, origin).union(calledBy(context, origin, mutators)).intersection(origin);
	}

	/**
	 * Calculates Graph Density of a Q, considering all nodes and edges
	 * 
	 * @param graph
	 *            A Q representing the graph to calculate density for
	 * @param directed
	 *            A boolean to indicate if the edges should be treated as
	 *            directed or undirected edges
	 * @return graph density calculated as: An undirected graph has no loops and
	 *         can have at most |N| * (|N| - 1) / 2 edges, so the density of an
	 *         undirected graph is 2 * |E| / (|N| * (|N| - 1)).
	 * 
	 *         A directed graph has no loops and can have at most |N| * (|N| -
	 *         1) edges, so the density of a directed graph is |E| / (|N| * (|N|
	 *         - 1)).
	 * 
	 *         Note: N is the number of nodes and E is the number of edges in
	 *         the graph. Note: A value of 0 would be a sparse graph and a value
	 *         of 1 is a dense graph. Note: Because of the way the way the Atlas
	 *         schema is constructed the above assumptions are likely to be
	 *         violated based on the nodes/edges contained in the graph.
	 *         Therefore the result of this calculation will likely not be
	 *         between 0 and 1 as expected, and should be taken with a grain of
	 *         salt.
	 * 
	 *         Reference: http://webwhompers.com/graph-theory.html
	 */
	public static double getDensity(Q graph, boolean directed) {
		double N = new Double(nodeSize(graph));
		double E = new Double(edgeSize(graph));

		// no nodes means we don't even have a graph
		// check this first
		if (N <= 0) {
			return -1;
		}

		// can't have any edges with just one node
		if (N == 1) {
			return -1;
		}

		// no edges is the sparsest you can get
		if ((E <= 0)) {
			return 0;
		}

		if (directed) {
			return E / (N * (N - 1));
		} else {
			return 2 * E / (N * (N - 1));
		}
	}

	/**
	 * Returns the Graph Density of a Q, considering specified nodes and edges
	 * 
	 * @param graph
	 * @param directed
	 * @param nodeTypes
	 *            An array of node type tags, or null for all nodes
	 * @param edgeTypes
	 *            An array of edge type tags, or null for all edges
	 * @return See getDensity method for details
	 */
	public static double getDensity(Q graph, boolean directed, String[] nodeTypes, String[] edgeTypes) {
		Q nodes = nodeTypes != null ? graph.nodesTaggedWithAny(nodeTypes).retainNodes() : graph.retainNodes();
		Q edges = edgeTypes != null ? graph.edgesTaggedWithAll(edgeTypes).retainEdges() : graph.retainEdges();
		graph = nodes.union(edges);
		return getDensity(graph, directed);
	}
}