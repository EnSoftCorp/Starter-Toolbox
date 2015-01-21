package toolbox.analysis.analyzers;

import com.ensoftcorp.atlas.core.query.Attr.Edge;
import com.ensoftcorp.atlas.core.query.Attr.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.java.core.script.Common;
import com.ensoftcorp.atlas.java.core.script.CommonQueries;
import com.ensoftcorp.open.toolbox.commons.analysis.Analyzer;

/**
 * Discovers Java main methods.
 * Tested on examples from http://rationalpi.wordpress.com/2007/01/29/main-methods-in-java/
 * @author Ben Holland
 */
public class DiscoverMainMethods extends Analyzer {

	@Override 
	public String getName(){
		return "Discover Main Methods";
	}
	
	@Override
	public String getDescription() {
		return "Finds methods named \"main\" that are public static void methods that take a String array.";
	}

	@Override
	public String[] getAssumptions() {
		return new String[]{"All main methods are named \"main\".", 
							"All main methods are public.",
							"All main methods are static.",
							"All main methods return void.",
							"All main methods take a single parameter of a String array."};
	}

	@Override
	protected Q evaluateEnvelope() {
		// Step 1) select nodes from the index that are marked as public, static, methods
		Q mainMethods = context.nodesTaggedWithAll(Node.IS_PUBLIC, Node.IS_STATIC, Node.METHOD);
		
		// Step 2) select nodes from the public static methods that are named "main"
		mainMethods = mainMethods.selectNode(Node.NAME, "main");

		// Step 3) filter out methods that are not void return types
		mainMethods = mainMethods.intersection(Common.stepFrom(Common.edges(Edge.RETURNS), Common.types("void")));
		
		// Step 4) filter out methods that do not take exactly one parameter
		Q paramEdgesInContext = context.edgesTaggedWithAny(Edge.PARAM).retainEdges();
		// methods with no parameteres will not have a PARAM edge
		Q methodsWithNoParams = mainMethods.difference(Common.stepFrom(paramEdgesInContext, Common.stepTo(paramEdgesInContext, mainMethods)));
		// methods with 2 or more params will have at least one edge with PARAMETER_INDEX == 1 (index 0 is the first parameter)
		Q methodsWithTwoOrMoreParams = Common.stepFrom(paramEdgesInContext, Common.stepTo(paramEdgesInContext, mainMethods).selectNode(Node.PARAMETER_INDEX, 1));
		mainMethods = mainMethods.difference(methodsWithNoParams, methodsWithTwoOrMoreParams);
		
		// Step 5) filter out methods that do not take a String array
		// get the 1-dimensional String array type
		Q stringArrays = Common.stepFrom(Common.edges(Edge.ELEMENTTYPE), Common.typeSelect("java.lang","String"));
		Q oneDimensionStringArray = stringArrays.selectNode(Node.DIMENSION, 1);
		Q mainMethodParams = CommonQueries.methodParameter(mainMethods, 0);
		Q validMethodParams = mainMethodParams.intersection(Common.stepFrom(Common.edges(Edge.TYPEOF), oneDimensionStringArray));
		mainMethods = Common.stepFrom(paramEdgesInContext, validMethodParams);

		return mainMethods;
	}
	
}
