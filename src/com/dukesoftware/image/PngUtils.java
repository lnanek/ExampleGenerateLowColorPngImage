package com.dukesoftware.image;

import ar.com.hjg.pngj.IImageLine;
import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngReader;
import ar.com.hjg.pngj.PngWriter;
import ar.com.hjg.pngj.chunks.ChunkCopyBehaviour;
import ar.com.hjg.pngj.chunks.PngChunkSingle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public final class PngUtils {

    private static final int _1K_BYTES = 1024;

    public static void toIndexed256Colors(File input, File output) throws IOException {
        PngReader png1 = new PngReader(input);
        ImageInfo pnginfo1 = png1.imgInfo;

        if (pnginfo1.indexed) {
            // already indexed, skip
            copy(new FileInputStream(input), new FileOutputStream(output));
            return;
        }

        // 1st step: create color palette from source png and also create indexed color pixel lines used by the palette
        final PngChunkPLTEBuilder builder = new PngChunkPLTEBuilder();
        int[][] indexedLine = new int[pnginfo1.rows][pnginfo1.cols];
        final int channel = pnginfo1.channels;

        if (channel == 1) {
            for (int row = 0, rows = pnginfo1.rows; row < rows; row++) {
                IImageLine l1 = png1.readRow();
                int[] scanline = ((ImageLineInt) l1).getScanline(); // to save typing
                for (int col = 0, len = scanline.length / channel, offset = 0; col < len; col++, offset = col * channel) {
                    indexedLine[row][col] = builder.getIndex(scanline[offset], 0, 0);
                }
            }
        } else if (channel == 2) {
            for (int row = 0, rows = pnginfo1.rows; row < rows; row++) {
                IImageLine l1 = png1.readRow();
                int[] scanline = ((ImageLineInt) l1).getScanline();
                for (int col = 0, len = scanline.length / channel, offset = 0; col < len; col++, offset = col * channel) {
                    indexedLine[row][col] = builder.getIndex(scanline[offset], scanline[offset + 1], 0);
                }
            }
        } else if (channel == 3) {
            for (int row = 0, rows = pnginfo1.rows; row < rows; row++) {
                IImageLine l1 = png1.readRow();
                int[] scanline = ((ImageLineInt) l1).getScanline();
                for (int col = 0, len = scanline.length / channel, offset = 0; col < len; col++, offset = col * channel) {
                    indexedLine[row][col] = builder.getIndex(scanline[offset], scanline[offset + 1], scanline[offset + 2]);
                }
            }
        } else if (channel == 4) {
            for (int row = 0, rows = pnginfo1.rows; row < rows; row++) {
                IImageLine l1 = png1.readRow();
                int[] scanline = ((ImageLineInt) l1).getScanline();
                for (int col = 0, len = scanline.length / channel, offset = 0; col < len; col++, offset = col * channel) {
                    if(scanline[offset + 3] != 255)
                    {
                        throw new IllegalArgumentException("sorry png image which has alpha value is not supported");
                    }
                    indexedLine[row][col] = builder.getIndex(scanline[offset], scanline[offset + 1], scanline[offset + 2]);
                }
            }
        }

        png1.end();

        System.out.println("Number of colors: " + builder.getNumberOfColors());
        int bitDepth = determineBitDepth(builder.getNumberOfColors());

        // 2nd step: write the palette and the indexed color pixel lines to output png 
        ImageInfo pnginfo2 = new ImageInfo(png1.imgInfo.cols, png1.imgInfo.rows, bitDepth, false, false,  true);
        PngWriter png2 = new PngWriter(output, pnginfo2);
        png2.setCompLevel(9);
        PngChunkSingle palette = builder.buildPngChunkPaletteFromCurrentMap(pnginfo2);
        png2.getMetadata().queueChunk(palette);
        png2.copyChunksFrom(png1.getChunksList(), ChunkCopyBehaviour.COPY_ALL_SAFE);

        ImageLineInt l2 = new ImageLineInt(pnginfo2);
        for (int row = 0, rows = png1.imgInfo.rows; row < rows; row++) {
            System.arraycopy(indexedLine[row], 0, l2.getScanline(), 0, indexedLine[row].length);
            png2.writeRow(l2, row);
        }
        png2.end();
    }

    private static int determineBitDepth(int numberOfColors) {
        if (numberOfColors <= 1)
            return 1;
        else if(numberOfColors <= 16)
            return 4;
        else if(numberOfColors <= 256)
            return 8;
        else
            throw new IllegalStateException("Number of colors should be less than 256");
    }

    private final static void copy(InputStream is, OutputStream os) throws IOException {
        copy(is, os, new byte[_1K_BYTES]);
    }

    private final static void copy(InputStream is, OutputStream os, byte[] buffer) throws IOException {
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