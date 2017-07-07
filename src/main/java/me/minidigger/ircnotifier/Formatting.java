package me.minidigger.ircnotifier;

/**
 * Created by mbenndorf on 07.07.2017.
 */
public enum Formatting {

	BOLD("\\x02"), COLORED_TEXT("\\x03"), ITALIX("\\x1D"), UNDERLINE("\\1F"), SWAP_COLOR("\\16"), RESET("\\x0F");

	private String code;

	Formatting(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	@Override public String toString() {
		return getCode();
	}
}
