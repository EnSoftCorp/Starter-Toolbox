package toolbox.analysis.analyzers;

import java.util.HashMap;
import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.CommonQueries;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.Analyzer;
import com.ensoftcorp.open.commons.analysis.SetDefinitions;
import com.ensoftcorp.open.commons.analysis.StandardQueries;

public class DiscoverReflectionUsage extends Analyzer {

	@Override
	public String getDescription() {
		return "Finds usage of Java Reflection.";
	}

	@Override
	public String[] getAssumptions() {
		return new String[]{"All uses of Reflection are through java.lang.reflect package."};
	}
	
	@Override
	public Map<String, Result> getResults(Q context) {
		HashMap<String,Result> results = new HashMap<String,Result>();
		
		// get all the "interesting" java.lang.reflect methods
		Q containsEdges = Common.universe().edgesTaggedWithAny(XCSG.Contains).retainEdges();
		Q reflectionPackage = Common.universe().pkg("java.lang.reflect");
		Q relectionMethods = containsEdges.forward(reflectionPackage).nodesTaggedWithAny(XCSG.Method);
		relectionMethods = relectionMethods.difference(SetDefinitions.objectMethodOverrides(), Common.methods("getName"), Common.methods("getSimpleName"));
		
		for(Node relectionMethod : relectionMethods.eval().nodes()){
			results.put(Analyzer.getUUID(), new Result((StandardQueries.getQualifiedFunctionName(relectionMethod)), 
					CommonQueries.interactions(context, Common.toQ(relectionMethod), XCSG.Call)));
		}
		
		return results;
	}

}