package de.vanmar.android.hoebapp.service;

/**
 * Created by Kolja on 29.05.2014.
 */
public enum Location {
	C("1", "C", "Zentralbibliothek"),
	L("21", "L", "Kinderbibliothek"),
	H4U("11", "H4U", "Hoeb4U"),
	A("4", "A", "Alstertal"),
	N("13", "N", "Altona"),
	K("35", "K", "Barmbek"),
	B("49", "B", "Bergedorf"),
	C2("6", "C2", "Billstedt"),
	S6("45", "S6", "Bramfeld"),
	D("28", "D", "Dehnhaide"),
	L2("23", "L2", "Eidelstedt"),
	T("27", "T", "Eimsbüttel"),
	ELB("40", "ELB", "Elbvororte"),
	S5("44", "S5", "Farmsen"),
	C1("5", "C1", "Finkenwerder"),
	G2("31", "G2", "Fuhlsbüttel"),
	R("51", "R", "Harburg"),
	W("19", "W", "Holstenstrasse"),
	X("9", "X", "Horn"),
	S8("47", "S8", "Hohenhorst"),
	R7("57", "R7", "Kirchdorf"),
	G5("34", "G5", "Langenhorn"),
	L5("25", "L5", "Lokstedt"),
	C3("7", "C3", "Mümmelmannsberg"),
	B1("50", "B1", "Neuallermöhe"),
	R4("54", "R4", "Neugraben"),
	L3("24", "L3", "Niendorf"),
	N6("17", "N6", "Osdorfer Born"),
	S3("42", "S3", "Rahlstedt"),
	L6("26", "L6", "Schnelsen"),
	K1("38", "K1", "Steilshoop"),
	V("48", "V", "Volksdorf"),
	S("39", "S", "Wandsbek"),
	R2("53", "R2", "Wilhelmsburg"),
	E("29", "E", "Winterhude"),
	F1("58", "F1", "Bücherbus Harburg"),
	F2("18", "F2", "Bus Bergedorf"),
	FA("3", "FA", "Fachstelle"),
	XX("0", "XX", "unbekannter Standort");

	private String owner;
	private String code;
	private String name;

	Location(String owner, String code, String name) {
		this.code = code;
		this.name = name;
		this.owner = owner;
	}

	public static Location get(String owner) {
		for (Location location : values()) {
			if (location.owner.equals(owner)) {
				return location;
			}
		}
		return XX;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}
}
