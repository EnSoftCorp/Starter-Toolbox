package toolbox.headless;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

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
		try {
			IndexingUtils.indexWorkspace();
			System.out.println("Success!");
		} catch (Throwable t) {
			System.out.println("Failed.");
			t.printStackTrace();
		}
		
		return EXIT_OK;
	}

	@Override
	public void stop() {}

}
