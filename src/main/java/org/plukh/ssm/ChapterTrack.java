package org.plukh.ssm;

public class ChapterTrack extends Track {
    public ChapterTrack(String fileName, int trackNumber) {
        super(fileName, trackNumber);
    }

    public ChapterTrack(String fileName, int trackNumber, String language, String type) {
        super(fileName, trackNumber, language, type);
    }

    @Override
    public String getTrackName(String seriesName, int season, int episode, EpisodeNames episodeNames, String
            defaultLanguage) {
        return null;
    }
}
