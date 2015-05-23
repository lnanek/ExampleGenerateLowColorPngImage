package com.dukesoftware.image;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ar.com.hjg.pngj.ImageInfo;
import ar.com.hjg.pngj.chunks.PngChunkPLTE;

public final class PngChunkPLTEBuilder {

    private final Map<Integer, Integer> map = new HashMap<Integer, Integer>();
    private int index = -1;

    public int getIndex(int r, int g, int b) {
        Integer key = (r << 16) | (g << 8) | b;
        Integer indexInMap = map.get(key);
        if (indexInMap != null) {
            return indexInMap;
        }
        index++;
        map.put(key, index);
        return index;
    }

    public PngChunkPLTE buildPngChunkPaletteFromCurrentMap(ImageInfo pnginfo) {
        PngChunkPLTE palette = new PngChunkPLTE(pnginfo);
        palette.setNentries(map.size());
        for (Entry<Integer, Integer> entry : map.entrySet()) {
            int key = entry.getKey();
            palette.setEntry(
                    entry.getValue(),
                    key >> 16,
                    (key & 0xFF00) >> 8,
                    key & 0x00FF
            );
        }
        return palette;
    }

    public int getNumberOfColors() {
        return map.size();
    }

}