package toolbox.headless;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
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
	public static final String ARGUMENTS = "arguments";
	public static final String ARGUMENT = "argument";
	
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
		
		// record the analysis arguments
		Element argumentsElement = doc.createElement(ARGUMENTS);
		rootElement.appendChild(argumentsElement);
		String[] args = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
		for(String arg : args){
			Element argumentElement = doc.createElement(ARGUMENT);
			argumentElement.setTextContent(arg);
			argumentsElement.appendChild(argumentElement);
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
		// just being lazy and using the current system time as a filename
		File outputFile = new File("/Users/benjholla/Desktop/" + date + ".xml");
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
