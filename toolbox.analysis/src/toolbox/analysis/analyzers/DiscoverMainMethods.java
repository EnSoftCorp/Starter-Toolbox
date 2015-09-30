package toolbox.analysis.analyzers;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.toolbox.commons.analysis.Analyzer;

public class DiscoverMainMethods extends Analyzer {
	
	@Override 
	public String getName(){
		return "Discover Main Methods";
	}
	
	@Override
	public String getDescription() {
		return "Finds valid Java main methods.";
	}

	@Override
	public String[] getAssumptions() {
		return new String[]{"Main methods are methods.",
						    "Main methods are case-sensitively named \"main\"",
						    "Main methods are public.", 
						    "Main methods are static.", 
						    "Main methods return void.", 
						    "Main methods take a single one dimensional String array parameter", 
						    "Main methods may be final.", 
						    "Main methods may have restricted floating point calculations.", 
						    "Main methods may be synchronized."};
	}

	@Override
	protected Q evaluateEnvelope() {
		// Step 1) select nodes from the index that are marked as public, static, methods
		Q mainMethods = appContext.nodesTaggedWithAll(XCSG.publicVisibility, XCSG.ClassMethod);
		
		// Step 2) select nodes from the public static methods that are named "main"
		mainMethods = mainMethods.selectNode(XCSG.name, "main");
		
		// Step 3) filter out methods that are not void return types
		Q returnsEdges = context.edgesTaggedWithAny(XCSG.Returns).retainEdges();
		Q voidMethods = returnsEdges.predecessors(Common.universe().nodesTaggedWithAny(XCSG.Void));
		mainMethods = mainMethods.intersection(voidMethods);
		
		// Step 4) filter out methods that do not take exactly one parameter
		Q paramEdges = appContext.edgesTaggedWithAny(XCSG.Parameter).retainEdges();
		Q mainMethodParams = paramEdges.successors(mainMethods);
		// methods with no parameters will not have a PARAM edge (and won't be reachable from parameters)
		mainMethods = paramEdges.predecessors(mainMethodParams);
		// methods with 2 or more parameters will have at least one parameter node with attribute 
		// PARAMETER_INDEX == 1 (index 0 is the first parameter)
		Q mainMethodSecondParams = mainMethodParams.selectNode(XCSG.parameterIndex, 1);
		Q methodsWithTwoOrMoreParams = mainMethodSecondParams.predecessors(mainMethodSecondParams);
		mainMethods = mainMethods.difference(methodsWithTwoOrMoreParams);

		// TODO: update for XCSG
//		// Step 5) filter out methods that do not take a one dimensional String array
//		Q elementTypeEdges = context.edgesTaggedWithAny(Edge.ELEMENTTYPE).retainEdges();
//		Q stringArraysTypes = elementTypeEdges.predecessors(Common.typeSelect("java.lang","String"));
//		// array types have a DIMENSION attribute
//		Q oneDimensionStringArrayType = stringArraysTypes.selectNode(Node.DIMENSION, 1);
//		Q mainMethodFirstParams = CommonQueries.methodParameter(mainMethods, 0);
//		Q typeOfEdges = context.edgesTaggedWithAny(Edge.TYPEOF).retainEdges();
//		Q oneDimensionStringArrayParameters = typeOfEdges.predecessors(oneDimensionStringArrayType);
//		Q validMainMethodFirstParams = mainMethodFirstParams.intersection(oneDimensionStringArrayParameters);
//		mainMethods = paramEdges.predecessors(validMainMethodFirstParams);
		
		return mainMethods;
	}
	
}
