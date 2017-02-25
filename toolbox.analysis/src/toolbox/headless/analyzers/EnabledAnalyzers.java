package toolbox.headless.analyzers;

import java.util.ArrayList;

import com.ensoftcorp.open.commons.analyzers.Analyzer;
import com.ensoftcorp.open.java.commons.analyzers.ClassLoaderUsage;
import com.ensoftcorp.open.java.commons.analyzers.JavaProgramEntryPoints;
import com.ensoftcorp.open.java.commons.analyzers.NativeCodeUsage;
import com.ensoftcorp.open.java.commons.analyzers.ProcessUsage;
import com.ensoftcorp.open.java.commons.analyzers.ReflectionUsage;

public class EnabledAnalyzers {

	public static ArrayList<Analyzer> getEnabledAnalyzers(){
		ArrayList<Analyzer> enabledScripts = new ArrayList<Analyzer>();
		enabledScripts.add(new JavaProgramEntryPoints());
		enabledScripts.add(new ClassLoaderUsage());
		enabledScripts.add(new NativeCodeUsage());
		enabledScripts.add(new ProcessUsage());
		enabledScripts.add(new ReflectionUsage());
		return enabledScripts;
	}
	
}
