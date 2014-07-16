package cn.hugo.android.mtd.exception;

public class MTDError extends Exception {

	public MTDError(String message, Throwable tr) {
		super(message, tr);
	}

	public MTDError(Exception e) {
		super(e);
	}

	public MTDError(String message) {
		super(message);

	}

	private static final long serialVersionUID = -2720998960647552784L;

}
