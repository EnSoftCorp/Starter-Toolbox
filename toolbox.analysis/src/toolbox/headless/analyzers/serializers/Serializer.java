package toolbox.headless.analyzers.serializers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import toolbox.analysis.analyzers.DiscoverMainMethods;
import toolbox.headless.Headless;

import com.ensoftcorp.atlas.core.log.Log;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.java.core.script.CommonQueries;
import com.ensoftcorp.open.toolbox.commons.analysis.Analyzer;

public class Serializer {

	private static Map<Class<? extends Analyzer>, Serializer> scriptSerializers = new HashMap<Class<? extends Analyzer>, Serializer>() {
		private static final long serialVersionUID = 1L;
		{
			put(DiscoverMainMethods.class, new DiscoverMainMethodsSerializer());
		}
	};

	public static String getBase64EncodeStackTrace(Throwable t) throws Exception {
		StringWriter errors = new StringWriter();
		t.printStackTrace(new PrintWriter(errors));
		String stackTrace = errors.toString();
		byte[] encoded = Base64.encodeBase64(stackTrace.getBytes());
		return new String(encoded);
	}
	
	public static Serializer getAnalyzerSerializer(Class<? extends Analyzer> script){
		if(scriptSerializers.containsKey(script)){
			return scriptSerializers.get(script);
		} else {
			Log.info("No custom analyzer for " + script.getName() + ", using default serializer.");
			return new Serializer();
		}
	}
	
	public static final String NODES = "nodes";
	public static final String EDGES = "edges";
	public static final String NAME = "name";
	
	protected Q envelope;
	
	/**
	 * Default serializer
	 * @param doc
	 * @param analyzerElement
	 * @param analyzer
	 */
	public void serialize(Document doc, Element analyzerElement, Analyzer analyzer){
		try {
			// set analyzer name
			analyzerElement.setAttribute(NAME, analyzer.getName());
			
			// record analysis time
			long startAnalysis = System.currentTimeMillis();
			this.envelope = analyzer.getEnvelope();
			long finishAnalysis = System.currentTimeMillis();
			analyzerElement.setAttribute(Headless.TIME, "" + (finishAnalysis - startAnalysis));
			
			// record analysis result graph size
			long numNodes = CommonQueries.nodeSize(envelope);
			analyzerElement.setAttribute(NODES, ("" + numNodes));
			long numEdges = CommonQueries.edgeSize(envelope);
			analyzerElement.setAttribute(EDGES, ("" + numEdges));
		} catch (Throwable t){
			try {
				analyzerElement.setAttribute(Headless.ERROR, Serializer.getBase64EncodeStackTrace(t));
			} catch (Exception e) {e.printStackTrace();}
		}
	}
}
