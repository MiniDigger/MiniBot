package me.minidigger.ircnotifier;

/**
 * Created by mbenndorf on 07.07.2017.
 */
public enum Color {

	WHITE("00"),
	BLACK("01"),
	BLUE("02"),
	GREEN("03"),
	RED("04"),
	BROWN("05"),
	PURPLE("06"),
	ORANGE("07"),
	YELLOW("08"),
	LIGHT_GREEN("09"),
	CYAN("10"),
	AQUA("11"),
	LIGHT_BLUE("12"),
	PINK("13"),
	GREY("14"),
	LIGHT_GRAY("15");

	private String code;

	Color(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static String color(Color foreground, Color background){
		return Formatting.COLORED_TEXT + foreground.getCode() + "," + background.getCode();
	}

	public static String color(Color color){
		return Formatting.COLORED_TEXT + color.getCode();
	}
}
