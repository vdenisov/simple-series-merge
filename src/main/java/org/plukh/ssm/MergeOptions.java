package org.plukh.ssm;

import org.plukh.options.Option;
import org.plukh.options.Options;
import org.plukh.options.Persistence;
import org.plukh.options.PersistenceType;

@Persistence(PersistenceType.PROPERTIES_FILE)
public interface MergeOptions extends Options {
    @Option(key = "basedir", readOnly = true)
    String getBaseDir();

    @Option(key = "outputdir", readOnly = true)
    String getOutputDir();

    @Option(key = "seriesname", readOnly = true)
    String getSeriesName();

    @Option(key = "season", readOnly = true)
    int getSeason();

    @Option(key = "episodenamesfile", readOnly = true)
    String getEpisodeNamesFile();

    @Option(key = "videolanguage", readOnly = true)
    String getVideoTrackLanguage();

    @Option(key = "defaultlanguage", readOnly = true)
    String getDefaultLanguage();

    @Option(key = "audiolangstokeep", readOnly = true)
    String getAudioLanguagesToKeep();

    @Option(key = "subtitlelangstokeep", readOnly = true)
    String getSubtitleLanguagesToKeep();

    @Option(key = "setsingletrackasdefault", readOnly = true)
    boolean isSetSingleTrackAsDefault();

    @Option(key = "ignoredaudiotracks", readOnly = true)
    String getIgnoredAudioTracks();
}
