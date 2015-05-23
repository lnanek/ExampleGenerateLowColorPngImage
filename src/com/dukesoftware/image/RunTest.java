package com.dukesoftware.image;

import java.io.File;

public class RunTest {

	public static void main(String[] args) throws Exception {

		System.out.println("test started!");

		final String inputPath = "French_Road_Sign_Priority_Turn_100.png";

		final String outputPath = "indexed256_100.png";
		PngUtils.toIndexed256Colors(new File(inputPath), new File(outputPath));

		System.out.println("completed indexed");

		final String outputPath3 = "grayscale_100.png";
		GrayscalePngUtils.toGrayscale(inputPath, outputPath3, false);

		System.out.println("completed grayscale");

	}

}
