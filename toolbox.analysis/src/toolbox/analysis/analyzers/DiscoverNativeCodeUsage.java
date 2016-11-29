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

public class DiscoverNativeCodeUsage extends Analyzer {

	@Override
	public String getDescription() {
		return "Finds calls to native code.";
	}

	@Override
	public String[] getAssumptions() {
		return new String[]{"All native calls are using JNI and are flagged with the keyword native."};
	}

	@Override
	public Map<String, Result> getResults(Q context) {
		HashMap<String,Result> results = new HashMap<String,Result>();
		
		Q nativeMethods = Common.universe().nodesTaggedWithAny(XCSG.Java.nativeMethod);
		
		for(Node nativeMethod : nativeMethods.eval().nodes()){
			results.put(Analyzer.getUUID(), new Result((StandardQueries.getQualifiedFunctionName(nativeMethod)), 
					CommonQueries.interactions(context, Common.toQ(nativeMethod), XCSG.Call)));
		}
		
		return results;
	}

}