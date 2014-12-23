package toolbox.shell.smartviews;

import org.eclipse.core.runtime.IProgressMonitor;

import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.CommonQueries;
import com.ensoftcorp.atlas.core.script.StyledResult;
import com.ensoftcorp.atlas.ui.scripts.selections.AtlasSmartViewScript;
import com.ensoftcorp.atlas.ui.selection.event.IAtlasSelectionEvent;

public class SuperTypeHierarchy implements AtlasSmartViewScript{

	@Override
	public String getTitle() {
		return "Supertype Hierarchy";
	}

	@Override
	public void indexChanged(IProgressMonitor arg0) {}

	@Override
	public void indexCleared() {}

	@Override
	public StyledResult selectionChanged(IAtlasSelectionEvent input) {
		Q interpretedSelection = input.getSelection();
		Q res = CommonQueries.typeHierarchy(interpretedSelection, CommonQueries.TraversalDirection.FORWARD);
		return new StyledResult(res);
	}
	
}
