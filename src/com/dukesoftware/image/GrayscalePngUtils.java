package com.dukesoftware.image;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import ar.com.hjg.pngj.IImageLine;
import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineHelper;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.PngWriter;
import ar.com.hjg.pngj.chunks.ChunkCopyBehaviour;
import ar.com.hjg.pngj.chunks.PngChunkTextVar;

public final class GrayscalePngUtils {

	private static final int BUFFER_SIZE = 1024;

	private static int extractLuminance(final int r, final int g, final int b) {
		return (int) (0.299 * r + 0.587 * g + 0.114 * b);
	}

	public static void toGrayscale(final String inputFilename,
			final String outputFilename, final boolean preserveMetaData) {
		// Read input
		final PngReader inputPngReader = new PngReader(new File(inputFilename));
		System.out.println("Read input: " + inputPngReader.toString());

		// Confirm compatible
		final int inputChannels = inputPngReader.imgInfo.channels;
		if (inputChannels < 3 || inputPngReader.imgInfo.bitDepth != 8) {
			throw new RuntimeException("This method is for RGB8/RGBA8 images");
		}

		// Setup output
		final ImageInfo outputImageSettings = new ImageInfo(
				inputPngReader.imgInfo.cols, inputPngReader.imgInfo.rows, 8,
				false, true, false);
		final PngWriter outputPngWriter = new PngWriter(
				new File(outputFilename), outputImageSettings, true);
		final ImageLineInt outputImageLine = new ImageLineInt(
				outputImageSettings);

		// Copy meta data if desired
		if (preserveMetaData) {
			outputPngWriter.copyChunksFrom(inputPngReader.getChunksList(),
					ChunkCopyBehaviour.COPY_ALL_SAFE);
		}

		// For each row of input
		for (int rowIndex = 0; rowIndex < inputPngReader.imgInfo.rows; rowIndex++) {

			final IImageLine inputImageLine = inputPngReader.readRow();
			final int[] scanline = ((ImageLineInt) inputImageLine)
					.getScanline(); // to save typing

			// For each column
			for (int columnIndex = 0; columnIndex < inputPngReader.imgInfo.cols; columnIndex++) {
				outputImageLine.getScanline()[columnIndex] = extractLuminance(
						scanline[columnIndex * inputChannels],
						scanline[columnIndex * inputChannels + 1],
						scanline[columnIndex * inputChannels] + 2);
			}
			outputPngWriter.writeRow(outputImageLine, rowIndex);
		}
		inputPngReader.end(); // it's recommended to end the reader first, in
								// case there are trailing chunks to read
		outputPngWriter.end();
	}

	private final static void copy(final InputStream is, final OutputStream os)
			throws IOException {
		copy(is, os, new byte[BUFFER_SIZE]);
	}

	private final static void copy(final InputStream is, final OutputStream os,
			byte[] buffer) throws IOException {
		try {
			for (int bytes = 0; (bytes = is.read(buffer)) != -1;) {
				os.write(buffer, 0, bytes);
			}
			os.flush();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					// quietly close anyway
				}
			}
		}
	}

}