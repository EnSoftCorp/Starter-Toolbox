package toolbox.interpreter.smartviews;

import com.ensoftcorp.atlas.java.core.query.Attr;
import com.ensoftcorp.atlas.java.core.query.Q;
import com.ensoftcorp.atlas.java.core.script.CommonQueries;
import com.ensoftcorp.atlas.java.core.script.StyledResult;
import com.ensoftcorp.atlas.java.ui.scripts.selections.AtlasSmartViewScript;

public class SuperTypeHierarchy implements AtlasSmartViewScript{
	@Override
	public String[] getSupportedNodeTags() {
		return new String[]{Attr.Node.TYPE};
	}

	@Override
	public String[] getSupportedEdgeTags() {
		return new String[]{Attr.Edge.SUPERTYPE};
	}

	@Override
	public StyledResult selectionChanged(SelectionInput input) {
		Q interpretedSelection = input.getInterpretedSelection();
		
		Q res = CommonQueries.typeHierarchy(interpretedSelection, CommonQueries.TraversalDirection.FORWARD);
		return new StyledResult(res);
	}

	@Override
	public String getTitle() {
		return "Supertype Hierarchy";
	}
}
