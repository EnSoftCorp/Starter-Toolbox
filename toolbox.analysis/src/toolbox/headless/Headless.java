package toolbox.headless;

import java.io.File;
import java.util.Collections;
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

import toolbox.headless.analyzers.EnabledAnalyzers;
import toolbox.headless.analyzers.serializers.Serializer;

import com.ensoftcorp.abp.common.util.ProjectUtil;
import com.ensoftcorp.abp.core.conversion.ApkToJimple;
import com.ensoftcorp.atlas.core.index.ProjectPropertiesUtil;
import com.ensoftcorp.atlas.core.indexing.IMappingSettings;
import com.ensoftcorp.atlas.core.indexing.IndexingUtil;
import com.ensoftcorp.atlas.core.licensing.AtlasLicenseException;
import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.open.toolbox.commons.analysis.Analyzer;
import com.ensoftcorp.open.toolbox.commons.utils.IndexingUtils;

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
		File sdkBundlePath = null;
		
		for (int i = 0; i < args.length; ++i) {
			if("-import".equals(args[i]) && i + 1 < args.length) {
				projects.add(new File(args[++i]));
			} else if("-build".equals(args[i])) {
				build = true;
			} else if ("-sdk-bundle-path".equals(args[i])){
				sdkBundlePath = new File(args[++i]);
				if(!sdkBundlePath.exists()){
					throw new IllegalArgumentException("Invalid Android SDK Bundle Path");
				}
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
				if (project.getAbsolutePath().endsWith(".apk")) {
					// decompile and import APK
					String projectName = project.getName();
					Log.info("Importing APK: " + projectName);
					projectName = projectName.substring(0, projectName.length() - ".apk".length());
					IProject eclipseProject = importAPK(project, sdkBundlePath, projectName);
					if (eclipseProject != null) {
						eclipseProjects.add(eclipseProject);
					}
				} else {
					// import eclipse project to workspace
					Log.info("Importing Project: " + project.getName());
					IProject eclipseProject = importEclipseProject(project);
					if (eclipseProject != null) {
						eclipseProjects.add(eclipseProject);
					}
				}
			}

			// build all projects after importing
			if (build) {
				Log.info("Building workspace");
				ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.CLEAN_BUILD, new NullProgressMonitor());
				ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
			}
		}
		
		// enable mapping for the project (so it can be indexed)
		for(IProject project : eclipseProjects){
			mapProject(project);
		}
		
		// record open projects in workspace
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
		for(Analyzer script : EnabledAnalyzers.getEnabledAnalyzers()){
			Log.info("Running " + script.getName() + " analysis script...");
			Serializer serializer = Serializer.getAnalyzerSerializer(script.getClass());
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
	
	public static void mapProject(IProject project) throws AtlasLicenseException {
		// configure project for indexing
		
		// Disable indexing for all projects
		List<IProject> allEnabledProjects = ProjectPropertiesUtil.getAllEnabledProjects();
		ProjectPropertiesUtil.setIndexingEnabledAndDisabled(Collections.<IProject>emptySet(), allEnabledProjects);
		
		// Enable indexing for this project
		List<IProject> ourProjects = Collections.singletonList(project);
		ProjectPropertiesUtil.setIndexingEnabledAndDisabled(ourProjects, Collections.<IProject>emptySet());
	
		// TODO: set jar indexing mode to: used only (same as default)
		IndexingUtil.indexWithSettings(/*saveIndex*/true, /*indexingSettings*/Collections.<IMappingSettings>emptySet(), ourProjects.toArray(new IProject[1]));
	}
	
	public static IProject importAPK(File apk, File androidSDKPath, String projectName) {
		IProject eclipseProject = null;
		if (apk.exists()) {
			try {
				Log.info("Importing " + apk.getName() + "...");
				// decompile APK directly into workspace
				String outputDir = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
				ApkToJimple.createAndroidBinaryProject(projectName, new Path(outputDir), apk, androidSDKPath, new NullProgressMonitor());
				eclipseProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			} catch (Exception e) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				ProjectUtil.deleteProject(project);
				Log.error("Failed to import: " + apk, e);
			}
		}
		return eclipseProject;
	}

	@Override
	public void stop() {}

}
