package cn.hugo.android.mtd.exception;

public class NetworkError extends MTDError {

	public NetworkError(String message, Throwable tr) {
		super(message, tr);
	}

	public NetworkError(String message) {
		super(message);
	}

}
