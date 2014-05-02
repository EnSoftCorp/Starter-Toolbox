package toolbox.headless;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Element;

import toolbox.analysis.Script;
import toolbox.analysis.scripts.DiscoverMainMethods;
import toolbox.headless.script.serializers.DiscoverMainMethodsSerializer;

public class Serializer {

	private static Map<Class<? extends Script>, Serializer> scriptSerializers = new HashMap<Class<? extends Script>, Serializer>() {
		private static final long serialVersionUID = 1L;
		{
			put(DiscoverMainMethods.class, new DiscoverMainMethodsSerializer());
		}
	};

	public static Serializer getScriptSerializer(Class<? extends Script> script){
		return scriptSerializers.get(script);
	}
	
	public void serialize(Element resultsElement, Script script){
		
	}
}
