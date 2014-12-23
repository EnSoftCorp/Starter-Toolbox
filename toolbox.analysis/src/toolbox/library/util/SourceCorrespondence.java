package toolbox.library.util;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.resources.ResourcesPlugin;

import com.ensoftcorp.atlas.core.db.graph.GraphElement;
import com.ensoftcorp.atlas.core.db.set.AtlasSet;
import com.ensoftcorp.atlas.core.query.Attr.Edge;
import com.ensoftcorp.atlas.core.query.Attr.Node;
import com.ensoftcorp.atlas.core.query.Q;

/**
 * A wrapper for some hacks to get file names, lines numbers, and other properties out of Q's
 * 
 * @author Ben Holland
 */
public class SourceCorrespondence implements Comparable<SourceCorrespondence> {
	private com.ensoftcorp.atlas.core.index.common.SourceCorrespondence sc;
	private String lineNumbers;
	private String relativeFile;
	private String project;
	private File file;
	private int startLine = -1;
	private int endLine = -1;
	private String name;

	private SourceCorrespondence(
			com.ensoftcorp.atlas.core.index.common.SourceCorrespondence sc) {
		this.sc = sc;
	}

	private SourceCorrespondence(
			com.ensoftcorp.atlas.core.index.common.SourceCorrespondence sc,
			String name) {
		this.sc = sc;
		this.name = name;
	}

	public boolean hasName() {
		boolean result = false;
		if (name != null) {
			result = true;
		}
		return result;
	}

	public String getName() {
		return name;
	}

	public String getLineNumbers() {
		if (lineNumbers == null) {
			int start = getStartLineNumber();
			int end = getEndLineNumber();
			if (start == end) {
				lineNumbers = "" + start;
			} else {
				lineNumbers = start + "-" + end;
			}
		}
		return lineNumbers;
	}

	public String getRelativeFile() {
		if (relativeFile == null) {
			File baseDirectory = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(getProject()).getLocation().toFile();
			relativeFile = OSUtils.ResourceUtils.getRelativePath(getFile()
					.getAbsolutePath(), baseDirectory.getParent(), ""
					+ File.separatorChar);
		}
		return relativeFile;
	}

	public String getProject() {
		if (project == null) {
			String filename = sc.toString().replace("[", "").replace("]", "");
			String relativePath = filename
					.substring(filename.indexOf("+/") + 1);
			String project = relativePath.substring(1);
			this.project = project.substring(0, project.indexOf("/"));
		}
		return project;
	}

	public File getFile() {
		if (file == null) {
			file = sc.sourceFile.getLocation().toFile();
		}
		return file;
	}

	@Override
	public String toString() {
		String lines = getLineNumbers();
		return "Filename: " + getRelativeFile() + " ("
				+ (lines.contains("-") ? "lines " : "line ") + lines + ")";
	}

	public int getOffset() {
		return sc.offset;
	}

	public int getLength() {
		return sc.length;
	}

	public int getStartLineNumber() {
		if (startLine == -1) {
			try {
				RandomAccessFile raf = new RandomAccessFile(getFile(), "r");
				int newLines = 0;
				while (raf.getFilePointer() < getOffset()
						&& raf.getFilePointer() < raf.length()) {
					raf.readLine();
					newLines++;
				}
				raf.close();
				startLine = newLines;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return startLine;
	}

	public int getEndLineNumber() {
		if (endLine == -1) {
			try {
				RandomAccessFile raf = new RandomAccessFile(getFile(), "r");
				int newLines = 0;
				while (raf.getFilePointer() < (getOffset() + getLength())
						&& raf.getFilePointer() < raf.length()) {
					raf.readLine();
					newLines++;
				}
				raf.close();
				endLine = newLines;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return endLine;
	}

	/**
	 * Returns a collection of source correspondents given a Q
	 * 
	 * @param q
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Collection<SourceCorrespondence> getSourceCorrespondents(Q q) {
		LinkedList<SourceCorrespondence> sourceCorrespondents = new LinkedList<SourceCorrespondence>();
		AtlasSet<GraphElement> nodes = q.eval().nodes();
		for (GraphElement node : nodes) {
			Object name = node.attr().get(Node.NAME);
			Object sc = node.attr().get(Node.SC);
			if (sc != null) {
				if (name != null) {
					sourceCorrespondents
							.add(new SourceCorrespondence(
									(com.ensoftcorp.atlas.core.index.common.SourceCorrespondence) sc,
									name.toString()));
				} else {
					sourceCorrespondents
							.add(new SourceCorrespondence(
									(com.ensoftcorp.atlas.core.index.common.SourceCorrespondence) sc));
				}
			}
			Object scList = node.attr().get(Edge.SC_LIST);
			if (scList != null) {
				for (com.ensoftcorp.atlas.core.index.common.SourceCorrespondence scListItem : (List<com.ensoftcorp.atlas.core.index.common.SourceCorrespondence>) scList) {
					if (name != null) {
						sourceCorrespondents
								.add(new SourceCorrespondence(
										(com.ensoftcorp.atlas.core.index.common.SourceCorrespondence) scListItem,
										name.toString()));
					} else {
						sourceCorrespondents
								.add(new SourceCorrespondence(
										(com.ensoftcorp.atlas.core.index.common.SourceCorrespondence) scListItem));
					}
				}
			}
		}
		return sourceCorrespondents;
	}

	@SuppressWarnings("unchecked")
	public static SourceCorrespondence getSourceCorrespondent(GraphElement ge) {
		SourceCorrespondence sourceCorrespondent = null;
		Object name = ge.attr().get(Node.NAME);
		Object sc = ge.attr().get(Node.SC);
		if (sc != null) {
			if (name != null) {
				sourceCorrespondent = new SourceCorrespondence(
						(com.ensoftcorp.atlas.core.index.common.SourceCorrespondence) sc,
						name.toString());
			} else {
				sourceCorrespondent = new SourceCorrespondence(
						(com.ensoftcorp.atlas.core.index.common.SourceCorrespondence) sc);
			}
		}
		Object scList = ge.attr().get(Edge.SC_LIST);
		if (scList != null) {
			for (com.ensoftcorp.atlas.core.index.common.SourceCorrespondence scListItem : (List<com.ensoftcorp.atlas.core.index.common.SourceCorrespondence>) scList) {
				if (name != null) {
					sourceCorrespondent = new SourceCorrespondence(
							(com.ensoftcorp.atlas.core.index.common.SourceCorrespondence) scListItem,
							name.toString());
				} else {
					sourceCorrespondent = new SourceCorrespondence(
							(com.ensoftcorp.atlas.core.index.common.SourceCorrespondence) scListItem);
				}
			}
		}
		return sourceCorrespondent;
	}

	public static String summarize(Q q) {
		return summarize(getSourceCorrespondents(q), getSourceCorrespondents(q.nodesTaggedWithAny(Node.METHOD)));
	}

	private static String summarize(Collection<SourceCorrespondence> allSources, Collection<SourceCorrespondence> methods) {
		StringBuilder result = new StringBuilder();

		// get the files and the line numbers in the source correspondents
		Map<String, SortedSet<LineNumber>> filesToLineNumbers = new HashMap<String, SortedSet<LineNumber>>();
		for (SourceCorrespondence sc : allSources) {
			if (filesToLineNumbers.containsKey(sc.getRelativeFile())) {
				LineNumber line = new LineNumber(sc.getLineNumbers());
				filesToLineNumbers.get(sc.getRelativeFile()).add(line);
			} else {
				SortedSet<LineNumber> lineNumbersSet = new TreeSet<LineNumber>();
				lineNumbersSet.add(new LineNumber(sc.getLineNumbers()));
				filesToLineNumbers.put(sc.getRelativeFile(), lineNumbersSet);
			}
		}

		// get the names of methods (grouped by file) of the methods source
		// correspondents note not using a set here, because the Q should 
		// insure there are no duplicates also duplicate method names may
		// be present in a given file (considering inner classes and other structures)
		Map<String, LinkedList<String>> filesToMethods = new HashMap<String, LinkedList<String>>();
		for (SourceCorrespondence sc : methods) {
			if (sc.hasName()) {
				if (filesToMethods.containsKey(sc.getRelativeFile())) {
					filesToMethods.get(sc.getRelativeFile()).add(sc.getName());
				} else {
					LinkedList<String> names = new LinkedList<String>();
					names.add(sc.getName());
					filesToMethods.put(sc.getRelativeFile(), names);
				}
			}
		}

		if (filesToLineNumbers.size() == 0) {
			result.append("Empty.");
		} else if (filesToLineNumbers.size() == 1) {
			result.append("File: ");
		} else {
			result.append("Files:\n");
		}
		
		for (Entry<String, SortedSet<LineNumber>> entry : filesToLineNumbers.entrySet()) {
			SortedSet<LineNumber> condensedLineNumbers = condenseLineNumbers(entry.getValue());
			String plurality = "s";
			if (condensedLineNumbers.size() == 1) {
				if (!condensedLineNumbers.first().lines.contains("-")) {
					plurality = "";
				}
			}

			// print the filename and line numbers
			if (!condensedLineNumbers.isEmpty()) {
				result.append(entry.getKey() + " " + condensedLineNumbers.toString()
								.replace("[", "(line" + plurality + " ")
								.replace("]", ")\n"));
			} else {
				// not sure this is even a case, but its here for good measure
				result.append(entry.getKey() + "\n");
			}

			// print the methods for that file
			if (filesToMethods.containsKey(entry.getKey())) {
				LinkedList<String> methodNames = filesToMethods.get(entry.getKey());
				if (methodNames.size() == 1) {
					result.append("Method: ");
				} else if (methodNames.size() > 1) {
					result.append("Methods: ");
				}
				for (String name : methodNames) {
					result.append(name + ", ");
				}
				if (!methodNames.isEmpty()) {
					result.delete(result.length() - 2, result.length());
				}
				result.append("\n");
			}
			result.append("\n");
		}

		return result.toString().trim();
	}

	private static SortedSet<LineNumber> condenseLineNumbers(
			SortedSet<LineNumber> lines) {
		SortedSet<LineNumber> condensed = new TreeSet<LineNumber>();
		SortedSet<LineNumber> ranges = getLineNumberRanges(lines);
		SortedSet<LineNumber> singleLines = getSingleLineNumbers(lines);

		// condense overlapping ranges
		Collection<LineNumber> rangesToRemove = new LinkedList<LineNumber>();
		for (LineNumber rangeOutside : ranges) {
			for (LineNumber rangeInside : ranges) {
				if (rangeOutside.equals(rangeInside)) {
					// skip, this is the same range
					continue;
				} else {
					String[] startEndLinesInside = rangeInside.toString()
							.split("-");
					int startInside = Integer.parseInt(startEndLinesInside[0]);
					int endInside = Integer.parseInt(startEndLinesInside[1]);
					String[] startEndLinesOutside = rangeOutside.toString()
							.split("-");
					int startOutside = Integer
							.parseInt(startEndLinesOutside[0]);
					int endOutside = Integer.parseInt(startEndLinesOutside[1]);
					if (startOutside >= startInside && endOutside <= endInside) {
						rangesToRemove.add(rangeOutside);
					}
				}
			}
		}
		ranges.removeAll(rangesToRemove);
		condensed.addAll(ranges);

		// condense single lines into ranges
		Collection<LineNumber> singlesToRemove = new LinkedList<LineNumber>();
		for (LineNumber range : ranges) {
			for (LineNumber singleLine : singleLines) {
				String[] startEndLines = range.toString().split("-");
				int start = Integer.parseInt(startEndLines[0]);
				int end = Integer.parseInt(startEndLines[1]);
				int line = Integer.parseInt(singleLine.toString());
				if (line >= start && line <= end) {
					singlesToRemove.add(singleLine);
				}
			}
		}
		singleLines.removeAll(singlesToRemove);
		condensed.addAll(singleLines);

		return joinConsecutiveLines(condensed);
	}

	private static SortedSet<LineNumber> getLineNumberRanges(
			SortedSet<LineNumber> allLineNumbers) {
		SortedSet<LineNumber> ranges = new TreeSet<LineNumber>();
		for (LineNumber lineNumber : allLineNumbers) {
			if (lineNumber.toString().contains("-")) {
				ranges.add(lineNumber);
			}
		}
		return ranges;
	}

	private static SortedSet<LineNumber> getSingleLineNumbers(
			SortedSet<LineNumber> allLineNumbers) {
		SortedSet<LineNumber> ranges = new TreeSet<LineNumber>();
		for (LineNumber lineNumber : allLineNumbers) {
			if (!lineNumber.toString().contains("-")) {
				ranges.add(lineNumber);
			}
		}
		return ranges;
	}

	private static SortedSet<LineNumber> joinConsecutiveLines(
			SortedSet<LineNumber> condensedLines) {
		LineNumber toRemove = null;
		found: for (LineNumber base : condensedLines) {
			for (LineNumber reference : condensedLines) {
				if (!base.lines.equals(reference.lines)) {
					// if its a single test it against each incrementing single
					// (except itself) or range until
					// either the single or range start is one less than the
					// single or there are no more singles/ranges to test
					if (base.isSingle()) {
						if (base.getStart() + 1 == reference.getStart()) {
							reference.growBackwardsOne();
							toRemove = base;
							break found;
						}
					} else {
						if (base.getEnd() == reference.getStart()
								|| base.getEnd() + 1 == reference.getStart()) {
							reference.growBackwardsToStart(base.getStart());
							toRemove = base;
							break found;
						}
					}
				}
			}
		}

		if (toRemove != null) {
			condensedLines.remove(toRemove);
			return joinConsecutiveLines(condensedLines);
		}

		return condensedLines;
	}

	@Override
	public int compareTo(SourceCorrespondence sc) {
		return this.getStartLineNumber() - sc.getStartLineNumber();
	}

	// Just a little helper class for sorting line number strings (based on the start line number)
	public static class LineNumber implements Comparable<LineNumber> {

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((lines == null) ? 0 : lines.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj){
				return true;
			}
			if (obj == null){
				return false;
			}
			if (getClass() != obj.getClass()){
				return false;
			}
			LineNumber other = (LineNumber) obj;
			if (lines == null) {
				if (other.lines != null){
					return false;
				}
			} else if (!lines.equals(other.lines)){
				return false;
			}
			return true;
		}

		private int startLine;
		private int endLine;
		private String lines;
		private boolean single = false;

		private LineNumber(String lines) {
			this.lines = lines;
			if (lines.contains("-")) {
				startLine = Integer.parseInt(lines.split("-")[0]);
				endLine = Integer.parseInt(lines.split("-")[1]);
			} else {
				single = true;
				startLine = Integer.parseInt(lines);
			}
		}

		public void growBackwardsToStart(int newStart) {
			if (isSingle()) {
				single = false;
				this.lines = newStart + "-" + startLine;
				this.endLine = startLine;
				this.startLine = newStart;
			} else {
				this.startLine = newStart;
				this.lines = startLine + "-" + endLine;
			}
		}

		public int getEnd() {
			return endLine;
		}

		public int getStart() {
			return startLine;
		}

		public boolean isSingle() {
			return single;
		}

		public void growBackwardsOne() {
			if (isSingle()) {
				single = false;
				this.lines = (startLine - 1) + "-" + startLine;
				this.endLine = startLine;
				this.startLine = startLine - 1;
			} else {
				this.startLine = startLine - 1;
				this.lines = startLine + "-" + endLine;
			}
		}

		@Override
		public String toString() {
			return lines;
		}

		@Override
		public int compareTo(LineNumber other) {
			if (this.isSingle() && other.isSingle()) {
				return this.startLine - other.startLine;
			} else if (this.isSingle() && !other.isSingle()) {
				if (this.startLine == other.startLine) {
					return this.startLine - other.endLine;
				}
			} else if (!this.isSingle() && other.isSingle()) {
				if (this.startLine == other.startLine) {
					return this.endLine - other.startLine;
				}
			} else {
				if (this.startLine == other.startLine) {
					return this.endLine - other.endLine;
				}
			}
			return this.startLine - other.startLine;
		}
	}

}