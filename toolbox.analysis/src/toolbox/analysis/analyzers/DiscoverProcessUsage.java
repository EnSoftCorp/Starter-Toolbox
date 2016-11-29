package toolbox.analysis.analyzers;

import java.util.HashMap;
import java.util.Map;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.CommonQueries;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.Analyzer;
import com.ensoftcorp.open.commons.analysis.StandardQueries;

public class DiscoverProcessUsage extends Analyzer {

	@Override
	public String getDescription() {
		return "Finds calls to methods that would allow the application to run a shell command.";
	}

	@Override
	public String[] getAssumptions() {
		return new String[]{"The only way to run shell commands is by directly invoking the Runtime.exec method."};
	}

	@Override
	public Map<String, Result> getResults(Q context) {
		HashMap<String,Result> results = new HashMap<String,Result>();
		
		Q runtimeType = Common.typeSelect("java.lang", "Runtime");
		Q declaresEdges = Common.universe().edgesTaggedWithAny(XCSG.Contains).retainEdges();
		Q runtimeMethods = declaresEdges.forwardStep(runtimeType).nodesTaggedWithAny(XCSG.Method);
		Q execMethods = runtimeMethods.intersection(Common.methods("exec"));
		
		for(Node execMethod : execMethods.eval().nodes()){
			results.put(Analyzer.getUUID(), new Result((StandardQueries.getQualifiedFunctionName(execMethod)), 
					CommonQueries.interactions(context, Common.toQ(execMethod), XCSG.Call)));
		}
		
		return results;
	}
	
}