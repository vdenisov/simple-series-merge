package org.plukh.ssm;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class VideoTrack extends Track {
    private static final Set<String> EXTENSIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("mkv")));

    public VideoTrack(String fileName, int trackNumber) {
        super(fileName, trackNumber);
    }

    public VideoTrack(String fileName, int trackNumber, String language, String type) {
        super(fileName, trackNumber, language, type);
    }

    @Override
    public String getTrackName(String seriesName, int season, int episode, EpisodeNames episodeNames, String
            defaultLanguage) {
        try {
            return seriesName + " - " + "S" + Utils.getPaddedNumber(season) + "E" + Utils.getPaddedNumber(episode) + " - " + episodeNames.getEpisodeName(episode);
        } catch (UnknownEpisodeException e) {
            throw new UnexpectedException("Error creating video track name for episode " + episode, e);
        }
    }

    public static Set<String> getSupportedExtensions() {
        return EXTENSIONS;
    }
}
