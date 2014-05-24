package de.vanmar.android.hoebapp.service;

/**
 * Created by Kolja on 21.05.2014.
 */
public class MaterialType {
	public static String valueOf(String materialTypeCode) {
		if ("1".equals(materialTypeCode)) {
			return "Buch Erwachsene";
		} else if ("2".equals(materialTypeCode)) {
			return "Zeitschrift";
		} else if ("3".equals(materialTypeCode)) {
			return "Zeitschrift unter 3 Euro";
		} else if ("4".equals(materialTypeCode)) {
			return "Bestseller";
		} else if ("5".equals(materialTypeCode)) {
			return "MP3";
		} else if ("6".equals(materialTypeCode)) {
			return "Konsolenspiel";
		} else if ("7".equals(materialTypeCode)) {
			return "CD";
		} else if ("8".equals(materialTypeCode)) {
			return "Buch Kinder/Jugendliche";
		} else if ("9".equals(materialTypeCode)) {
			return "Kitakisten";
		} else if ("10".equals(materialTypeCode)) {
			return "eBook";
		} else if ("11".equals(materialTypeCode)) {
			return "eAudio";
		} else if ("12".equals(materialTypeCode)) {
			return "eVideo";
		} else if ("14".equals(materialTypeCode)) {
			return "VHS-Video";
		} else if ("15".equals(materialTypeCode)) {
			return "Note";
		} else if ("16".equals(materialTypeCode)) {
			return "Plan, Karte";
		} else if ("17".equals(materialTypeCode)) {
			return "eMusik";
		} else if ("18".equals(materialTypeCode)) {
			return "eInfo";
		} else if ("19".equals(materialTypeCode)) {
			return "Software";
		} else if ("20".equals(materialTypeCode)) {
			return "eLearning";
		} else if ("21".equals(materialTypeCode)) {
			return "Kassette";
		} else if ("22".equals(materialTypeCode)) {
			return "Kinderkassette";
		} else if ("23".equals(materialTypeCode)) {
			return "Spiel";
		} else if ("24".equals(materialTypeCode)) {
			return "CD-ROM";
		} else if ("25".equals(materialTypeCode)) {
			return "DVD";
		} else if ("26".equals(materialTypeCode)) {
			return "Blu-ray Disc";
		} else if ("27".equals(materialTypeCode)) {
			return "eDocument";
		} else if ("28".equals(materialTypeCode)) {
			return "Playaway";
		} else if ("29".equals(materialTypeCode)) {
			return "Sommerferienprogramm";
		} else if ("30".equals(materialTypeCode)) {
			return "Blu-ray 3D";
		}
		return null;
	}
}
