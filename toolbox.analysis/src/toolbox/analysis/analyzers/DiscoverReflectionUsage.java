package toolbox.analysis.analyzers;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.CommonQueries;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.toolbox.commons.SetDefinitions;
import com.ensoftcorp.open.toolbox.commons.analysis.Analyzer;

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
	protected Q evaluateEnvelope() {
		// get all the java.lang.reflect methods
		Q containsEdges = Common.universe().edgesTaggedWithAny(XCSG.Contains).retainEdges();
		Q reflectionPackage = Common.universe().pkg("java.lang.reflect");
		Q relectionMethods = containsEdges.forward(reflectionPackage).nodesTaggedWithAny(XCSG.Method);
		relectionMethods = relectionMethods.difference(SetDefinitions.objectMethodOverrides(), Common.methods("getName"), Common.methods("getSimpleName"));
		return CommonQueries.interactions(appContext, relectionMethods, XCSG.Call);
	}

}