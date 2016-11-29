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

public class DiscoverClassLoaderUsage extends Analyzer {

	@Override
	public String getDescription() {
		return "Finds calls to Class Loaders.";
	}

	@Override
	public String[] getAssumptions() {
		return new String[]{"All uses of Reflection are through java.lang.reflect package."};
	}

	@Override
	public Map<String, Result> getResults(Q context) {
		HashMap<String,Result> results = new HashMap<String,Result>();
		
		// get all the java.lang.reflect methods
		Q containsEdges = Common.universe().edgesTaggedWithAny(XCSG.Contains).retainEdges();
		Q supertypeEdges = Common.universe().edgesTaggedWithAny(XCSG.Supertype).retainEdges();
		Q loaders = supertypeEdges.reverse(Common.typeSelect("java.lang", "ClassLoader"));
		Q loaderMethods = containsEdges.forwardStep(loaders).nodesTaggedWithAny(XCSG.Method).difference(SetDefinitions.objectMethodOverrides());
		
		for(Node loaderMethod : loaderMethods.eval().nodes()){
			results.put(Analyzer.getUUID(), new Result((StandardQueries.getQualifiedFunctionName(loaderMethod)), 
					CommonQueries.interactions(context, Common.toQ(loaderMethod), XCSG.Call)));
		}
		
		return results;
	}
}