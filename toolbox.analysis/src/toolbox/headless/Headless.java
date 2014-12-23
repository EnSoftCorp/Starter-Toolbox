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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ensoftcorp.atlas.core.log.Log;

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
		
		// parse plugin arguments
		boolean closeImportedProjectsAfterAnalysis = false;
		boolean removeImportedProjectsAfterAnalysis = false;
		boolean build = false;
		File outputFile = null;
		String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		List<File> projects = new LinkedList<File>();
		for (int i = 0; i < args.length; ++i) {
			if("-import".equals(args[i]) && i + 1 < args.length) {
				projects.add(new File(args[++i]));
			} else if("-build".equals(args[i])) {
				build = true;
			} else if("-output".equals(args[i])){
				outputFile = new File(args[++i]);
			} else if("-close-imported-projects-after-analysis".equals(args[i])){
				closeImportedProjectsAfterAnalysis = true;
			} else if("-remove-imported-projects-after-analysis".equals(args[i])){
				removeImportedProjectsAfterAnalysis = true;
			}
		}

		// import projects
		List<IProject> eclipseProjects = new LinkedList<IProject>();
		if (projects.size() != 0) {
			for (File project : projects) {
				// import eclipse project to workspace
				IProject eclipseProject = importEclipseProject(project);
				if(eclipseProject != null){
					eclipseProjects.add(eclipseProject);
				}
			}

			// build all projects after importing
			if (build) {
				Log.info("Re-building workspace");
				ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.CLEAN_BUILD, new NullProgressMonitor());
				ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
			}
		}
		
		// record open projects in workspace
		// TODO: Check if flagged for indexing as well
		Element workspaceElement = doc.createElement(WORKSPACE);
		rootElement.appendChild(workspaceElement);
		for(IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()){
			if(project.isOpen()){
				Element projectElement = doc.createElement(PROJECT);
				projectElement.setAttribute(NAME, project.getName());
				workspaceElement.appendChild(projectElement);
			}
		}

		try {
			// index the workspace
			Log.info("Indexing workspace...");
			IndexingUtils.indexWorkspace();
		} catch (Throwable t) {
			Log.error("Indexing Failed.", t);
			Element indexTimeElement = doc.createElement(DETAIL);
			indexTimeElement.setAttribute(ERROR, Serializer.getBase64EncodeStackTrace(t));
			detailsElement.appendChild(indexTimeElement);
		}
		
		// create scripts element
		Element scriptsElement = doc.createElement(SCRIPTS);
		rootElement.appendChild(scriptsElement);
		
		// run analysis scripts
		for(Script script : EnabledScripts.getEnabledScripts()){
			Log.info("Running " + script.getName() + " analysis script...");
			Serializer serializer = Serializer.getScriptSerializer(script.getClass());
			Element scriptElement = doc.createElement(SCRIPT);
			scriptsElement.appendChild(scriptElement);
			serializer.serialize(doc, scriptElement, script);
		}
		
		// write the content into xml file (with pretty print)
		if(outputFile == null){
			// just being lazy and using the current system time as a filename
			outputFile = new File(date + ".xml");
		}
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(outputFile);
		transformer.transform(source, result);
		
		Log.info("Finished analysis.");
		
		// clean up the workspace
		if(closeImportedProjectsAfterAnalysis || removeImportedProjectsAfterAnalysis){
			Log.info("Closing imported projects...");
			for (IProject eclipseProject : eclipseProjects) {
				eclipseProject.close(null);
				if(removeImportedProjectsAfterAnalysis){
					Log.info("Removing imported projects...");
					boolean deleteContent = true;
					eclipseProject.delete(deleteContent, true, null);
				}
			}
		}

		Log.info("Exiting.");
		return EXIT_OK;
	}

	private IProject importEclipseProject(File eclipseProjectPath) {
		IProject eclipseProject = null;
		IPath path = new Path(eclipseProjectPath.getAbsolutePath()).append(".project");
		if(path.toFile().exists()){
			try {
				IProjectDescription description = ResourcesPlugin.getWorkspace().loadProjectDescription(path);
				eclipseProject = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
				Log.info("Importing " + eclipseProject.getName() + "...");
				eclipseProject.create(description, null);
				eclipseProject.open(null);
			} catch (Exception e){
				Log.error("Failed to import: " + eclipseProjectPath.getName(), e);
			}
		}
		return eclipseProject;
	}

	@Override
	public void stop() {}

}
