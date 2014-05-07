package toolbox.headless;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import toolbox.analysis.Script;
import toolbox.headless.scripts.EnabledScripts;
import toolbox.headless.scripts.serializers.Serializer;
import toolbox.library.util.IndexingUtils;

public class Headless implements IApplication {

	// constants for XML elements
	public static final String APP = "app";
	public static final String DETAILS = "details";
	public static final String DETAIL = "detail";
	public static final String WORKSPACE = "workspace";
	public static final String SCRIPTS = "scripts";
	public static final String SCRIPT = "script";
	
	// constants for XML attributes
	public static final String NAME = "name";
	public static final String DATE = "date";
	public static final String ERROR = "error";
	public static final String TIME = "time";
	public static final String PROJECT = "project";
	
	@Override
	public Object start(IApplicationContext context) throws Exception {
		// build an XML document to serialize results to...
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// created root element
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement(APP);
		doc.appendChild(rootElement);

		// create analysis details root
		Element detailsElement = doc.createElement(DETAILS);
		rootElement.appendChild(detailsElement);
		
		// record analysis date
		long date = System.currentTimeMillis();
		Element dateElement = doc.createElement(DETAIL);
		dateElement.setAttribute(DATE, ("" + date));
		detailsElement.appendChild(dateElement);
		
		// import projects
		boolean build = false;
		File outputFile = null;
		String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		List<String> projects = new LinkedList<String>();
		for (int i = 0; i < args.length; ++i) {
			if("-import".equals(args[i]) && i + 1 < args.length) {
				projects.add(args[++i]);
			} else if("-build".equals(args[i])) {
				build = true;
			} else if("-output".equals(args[i])){
				outputFile = new File(args[++i]);
			}
		}

		if (projects.size() != 0) {
			for (String projectPath : projects) {
				// import project to workspace
				IProjectDescription description = ResourcesPlugin.getWorkspace().loadProjectDescription(
						new Path(projectPath).append(".project"));
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
				System.out.println("Importing " + project.getName() + "...");
				project.create(description, null);
				project.open(null);
			}

			// build all projects after importing
			if (build) {
				System.out.println("Re-building workspace");
				ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.CLEAN_BUILD, new NullProgressMonitor());
				ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
			}
		}
		
		// record projects in workspace
		Element workspaceElement = doc.createElement(WORKSPACE);
		rootElement.appendChild(workspaceElement);
		for(IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()){
			Element projectElement = doc.createElement(PROJECT);
			projectElement.setAttribute(NAME, project.getName());
			workspaceElement.appendChild(projectElement);
		}

		try {
			// index the workspace
			System.out.println("Indexing workspace...");
			IndexingUtils.indexWorkspace();
			
			// create scripts element
			Element scriptsElement = doc.createElement(SCRIPTS);
			rootElement.appendChild(scriptsElement);
			
			// run analysis scripts
			for(Script script : EnabledScripts.getEnabledScripts()){
				System.out.println("Running " + script.getName() + " analysis script...");
				Serializer serializer = Serializer.getScriptSerializer(script.getClass());
				Element scriptElement = doc.createElement(SCRIPT);
				scriptsElement.appendChild(scriptElement);
				serializer.serialize(doc, scriptElement, script);
			}
		} catch (Throwable t) {
			System.out.println("Indexing Failed.");
			Element indexTimeElement = doc.createElement(DETAIL);
			indexTimeElement.setAttribute(ERROR, Serializer.getBase64EncodeStackTrace(t));
			detailsElement.appendChild(indexTimeElement);
		}
		
		// write the content into xml file (with pretty print)
		if(outputFile == null){
			// just being lazy and using the current system time as a filename
			outputFile = new File("/Users/benjholla/Desktop/" + date + ".xml");
		}
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(outputFile);
		transformer.transform(source, result);
		
		System.out.println("Analysis finished.");
		return EXIT_OK;
	}

	@Override
	public void stop() {}

}
