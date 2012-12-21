package fr.soleil.bossanova;

public class BossanovaException extends Exception {
	
	public BossanovaException(String message, Throwable cause){
		super(message, cause);
	}
	public String getUserMessage(){
		return getMessage();
	}
}
