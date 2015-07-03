package eu.comsode.unifiedviews.plugins.extractor.corrector;

import java.util.ArrayList;
import java.util.List;

import eu.comsode.unifiedviews.plugins.extractor.service.Compare;

public class Corrector {

	public String[] getArgs() {
		String[] args = {};
		return args;
	}

	public List<String> correct(List<String> row) {
		if (isHeader(row)) {
			return null;
		}
		List<String> newRow = new ArrayList<String>();
		for (String s : row) {
			newRow.add(s.trim());
		}
		if (newRow.size() == 7) {
			for (int i = 0; i < newRow.size(); i++) {
				if (newRow.get(i).isEmpty()) {
					newRow.remove(i);
				}
			}
		}
		newRow.add(isConsistent(newRow) ? "correct" : "mismatch");
		return newRow;
	}

	private boolean isConsistent(List<String> row) {
		if (row.size() != 6) {
			return false;
		}
		return true;
	}

	public boolean isHeader(List<String> row) {
		if (!Compare.isInteger(row.get(0))) {
			return true;
		}
		return false;
	}
}
