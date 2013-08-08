package perftest;

public class Level {

	String id;
	int score;
	Level up, down;

	public Level(String id) {
		this.id = id;
	}

	public Level(String id, Level up, Level down) {
		this.id = id;
		this.up = up;
		this.down = down;
	}

	public String enter(String id, int score) {
		String hello = id + "_" + score;
		return hello;
	}

	public void down() {
		if (down != null) {
			down.enter(this.id, ++score);
		}
	}

	public void up() {
		if (up != null) {
			up.enter(this.id, ++score);
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public Level getUp() {
		return up;
	}

	public void setUp(Level up) {
		this.up = up;
	}

	public Level getDown() {
		return down;
	}

	public void setDown(Level down) {
		this.down = down;
	}

}
