package toolbox.analysis.analyzers;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.atlas.core.script.CommonQueries;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.toolbox.commons.SetDefinitions;
import com.ensoftcorp.open.toolbox.commons.analysis.Analyzer;

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
	protected Q evaluateEnvelope() {
		// get all the java.lang.reflect methods
		Q containsEdges = Common.universe().edgesTaggedWithAny(XCSG.Contains).retainEdges();
		Q supertypeEdges = Common.universe().edgesTaggedWithAny(XCSG.Supertype).retainEdges();
		Q loaders = supertypeEdges.reverse(Common.typeSelect("java.lang", "ClassLoader"));
		Q loaderMethods = containsEdges.forwardStep(loaders).nodesTaggedWithAny(XCSG.Method).difference(SetDefinitions.objectMethodOverrides());
		return CommonQueries.interactions(appContext, loaderMethods, XCSG.Call);
	}

}