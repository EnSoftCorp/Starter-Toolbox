package toolbox.library.util;

import com.ensoftcorp.atlas.core.indexing.IIndexListener;
import com.ensoftcorp.atlas.core.indexing.IndexingUtil;

public class IndexingUtils {

	private static class IndexerErrorListener implements IIndexListener {

		private Throwable t = null;

		public boolean hasCaughtThrowable() {
			return t != null;
		}

		public Throwable getCaughtThrowable() {
			return t;
		}

		@Override
		public void indexOperationCancelled(IndexOperation io) {}

		@Override
		public void indexOperationError(IndexOperation io, Throwable t) {
			this.t = t;
		}

		@Override
		public void indexOperationStarted(IndexOperation io) {}

		@Override
		public void indexOperationComplete(IndexOperation io) {}
	};

	/**
	 * Index the workspace (blocking mode and throws index errors)
	 * 
	 * @throws Throwable
	 */
	public static void indexWorkspace() throws Throwable {
		IndexerErrorListener errorListener = new IndexerErrorListener();
		IndexingUtil.addListener(errorListener);
		IndexingUtil.indexWorkspace(true);
		IndexingUtil.removeListener(errorListener);
		if (errorListener.hasCaughtThrowable()) {
			throw errorListener.getCaughtThrowable();
		}
	}

}
