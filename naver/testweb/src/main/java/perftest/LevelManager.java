package perftest;

import java.util.LinkedList;

public class LevelManager {

	LinkedList<Level> levels = new LinkedList<Level>();

	public LevelManager() {
		Level topLevel = new Level("toplevel");
		levels.add(topLevel);
		for (int i = 1; i < 10; i++) {
			Level prev = levels.get(i - 1);
			Level curr = new Level("level" + i);
			curr.setUp(prev);
			prev.setDown(curr);
			levels.add(curr);
		}
	}

	public void traverse() {
		for (Level each : levels) {
			each.down();
		}
	}

}
