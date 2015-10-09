package toolbox.analysis.analyzers;

import com.ensoftcorp.atlas.core.query.Q;
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
		//TODO: Implement
		return null;
	}
	
}
