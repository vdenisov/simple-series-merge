package org.plukh.ssm;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class SubtitleTrack extends Track {
    private static final Set<String> EXTENSIONS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("srt", "ass")));

    public SubtitleTrack(String fileName, int trackNumber) {
        super(fileName, trackNumber);
    }

    public SubtitleTrack(String fileName, int trackNumber, String language, String codec) {
        super(fileName, trackNumber, language, codec);
    }

    @Override
    public String getTrackName(String seriesName, int season, int episode, EpisodeNames episodeNames, String
            defaultLanguage) {
        return Utils.getLanguage(getTrackLanguage(defaultLanguage));
    }

    public static Set<String> getSupportedExtensions() {
        return EXTENSIONS;
    }
}
