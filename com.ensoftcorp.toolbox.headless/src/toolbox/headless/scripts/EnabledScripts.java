package toolbox.headless.scripts;

import java.util.ArrayList;

import toolbox.analysis.Script;
import toolbox.analysis.scripts.DiscoverMainMethods;

public class EnabledScripts {

	public static ArrayList<Script> getEnabledScripts(){
		ArrayList<Script> enabledScripts = new ArrayList<Script>();
		enabledScripts.add(new DiscoverMainMethods());
		return enabledScripts;
	}
	
}
