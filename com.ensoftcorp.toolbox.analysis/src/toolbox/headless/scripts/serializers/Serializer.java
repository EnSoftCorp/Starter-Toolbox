package toolbox.headless.scripts.serializers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import toolbox.analysis.Script;
import toolbox.analysis.scripts.DiscoverMainMethods;
import toolbox.headless.Headless;

import com.ensoftcorp.atlas.java.core.query.Q;
import com.ensoftcorp.atlas.java.core.script.CommonQueries;

public class Serializer {

	private static Map<Class<? extends Script>, Serializer> scriptSerializers = new HashMap<Class<? extends Script>, Serializer>() {
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
	
	public static Serializer getScriptSerializer(Class<? extends Script> script){
		return scriptSerializers.get(script);
	}
	
	public static final String NODES = "nodes";
	public static final String EDGES = "edges";
	
	protected Q envelope;
	
	/**
	 * Default serializer
	 * @param doc
	 * @param scriptElement
	 * @param script
	 */
	public void serialize(Document doc, Element scriptElement, Script script){
		try {
			// record analysis time
			long startAnalysis = System.currentTimeMillis();
			this.envelope = script.getEnvelope();
			long finishAnalysis = System.currentTimeMillis();
			scriptElement.setAttribute(Headless.TIME, "" + (finishAnalysis - startAnalysis));
			
			// record analysis result graph size
			long numNodes = CommonQueries.nodeSize(envelope);
			scriptElement.setAttribute(NODES, ("" + numNodes));
			long numEdges = CommonQueries.edgeSize(envelope);
			scriptElement.setAttribute(EDGES, ("" + numEdges));
		} catch (Throwable t){
			try {
				scriptElement.setAttribute(Headless.ERROR, Serializer.getBase64EncodeStackTrace(t));
			} catch (Exception e) {e.printStackTrace();}
		}
	}
}
