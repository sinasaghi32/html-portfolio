package exception;
public class NetworkException extends Exception { public NetworkException(String m, Throwable c){ super(m,c); } public NetworkException(String m){super(m);} }
