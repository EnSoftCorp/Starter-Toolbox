package toolbox.headless.analyzers;

import java.util.ArrayList;

import com.ensoftcorp.open.commons.analysis.Analyzer;
import com.ensoftcorp.open.java.commons.analyzers.DiscoverMainMethods;

import toolbox.analysis.analyzers.DiscoverClassLoaderUsage;
import toolbox.analysis.analyzers.DiscoverNativeCodeUsage;
import toolbox.analysis.analyzers.DiscoverProcessUsage;
import toolbox.analysis.analyzers.DiscoverReflectionUsage;

public class EnabledAnalyzers {

	public static ArrayList<Analyzer> getEnabledAnalyzers(){
		ArrayList<Analyzer> enabledScripts = new ArrayList<Analyzer>();
		enabledScripts.add(new DiscoverMainMethods());
		enabledScripts.add(new DiscoverClassLoaderUsage());
		enabledScripts.add(new DiscoverNativeCodeUsage());
		enabledScripts.add(new DiscoverProcessUsage());
		enabledScripts.add(new DiscoverReflectionUsage());
		return enabledScripts;
	}
	
}
