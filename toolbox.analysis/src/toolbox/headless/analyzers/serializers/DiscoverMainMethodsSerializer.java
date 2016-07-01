package toolbox.headless.analyzers.serializers;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.open.commons.analysis.Analyzer;
import com.ensoftcorp.open.commons.utils.FormattedSourceCorrespondence;

public class DiscoverMainMethodsSerializer extends Serializer {

	public static final String LOCATION = "location";
	
	@Override
	public void serialize(Document doc, Element analyzerElement, Analyzer analyzer){
		super.serialize(doc, analyzerElement, analyzer);
		
		// for each main method list the source file and line numbers
		for(GraphElement mainMethod : envelope.eval().nodes()){
			Element locationElement = doc.createElement(LOCATION);
			FormattedSourceCorrespondence sc = FormattedSourceCorrespondence.getSourceCorrespondent(mainMethod);
			locationElement.setTextContent(sc.toString());
			analyzerElement.appendChild(locationElement);
		}
	}
	
}
