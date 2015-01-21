package toolbox.headless.analyzers;

import java.util.ArrayList;

import toolbox.analysis.analyzers.DiscoverMainMethods;

import com.ensoftcorp.open.toolbox.commons.analysis.Analyzer;

public class EnabledAnalyzers {

	public static ArrayList<Analyzer> getEnabledAnalyzers(){
		ArrayList<Analyzer> enabledScripts = new ArrayList<Analyzer>();
		enabledScripts.add(new DiscoverMainMethods());
		return enabledScripts;
	}
	
}
