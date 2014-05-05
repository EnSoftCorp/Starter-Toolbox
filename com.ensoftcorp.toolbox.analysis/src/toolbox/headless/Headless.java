package toolbox.headless;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import toolbox.analysis.Script;
import toolbox.headless.scripts.EnabledScripts;
import toolbox.library.util.IndexingUtils;

public class Headless implements IApplication {

	@Override
	public Object start(IApplicationContext context) throws Exception {
		
		// get headless plugin arguments
//		String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
//		for(String arg : args){
//			System.out.println(arg);
//		}
		
		// Index the workspace
		System.out.println("Indexing workspace...");
		try {
			IndexingUtils.indexWorkspace();
			for(Script script : EnabledScripts.getEnabledScripts()){
				System.out.println("Running " + script.getName() + " analysis script...");
				
			}
		} catch (Throwable t) {
			System.out.println("Indexing Failed.");
			t.printStackTrace();
		}
		System.out.println("Analysis finished.");
		
		return EXIT_OK;
	}

	@Override
	public void stop() {}

}
