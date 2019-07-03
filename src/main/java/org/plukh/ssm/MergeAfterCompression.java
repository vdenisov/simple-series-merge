package org.plukh.ssm;

import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.plukh.options.OptionsException;
import org.plukh.options.OptionsFactory;
import org.plukh.options.impl.persistence.FileConfig;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class MergeAfterCompression {
    private static MergeOptions options = (MergeOptions) OptionsFactory.getOptionsInstance(MergeOptions.class);

    public static void main(String[] args) throws IOException {
        System.err.println("SimpleSeriesMerge starting (directory mode)...");
        Utils.init();
        initOptions();
        EpisodeNames names = initEpisodeNames();
        List<String> dirsToScan = readDirectoryList();
        mergeEpisodes(dirsToScan, names);
    }

    private static EpisodeNames initEpisodeNames() {
        EpisodeNames names = new EpisodeNames();
        try {
            names.load(options.getEpisodeNamesFile());
        } catch (IOException e) {
            System.err.println("Error loading episode names");
            e.printStackTrace();
            System.exit(1);
        }
        return names;
    }

    private static List<String> readDirectoryList() {
        System.err.println("Scanning base directory " + options.getBaseDir());

        File baseDir = new File(options.getBaseDir());
        File[] dirs = baseDir.listFiles((java.io.FileFilter) DirectoryFileFilter.DIRECTORY);
        List<String> dirsToScan = new LinkedList<>();
        for (File dir : dirs) {
            dirsToScan.add(dir.getAbsolutePath());
        }

        System.err.println("Found " + dirsToScan.size() + " directories");

        return dirsToScan;
    }

    private static void mergeEpisodes(List<String> dirs, EpisodeNames names) {
        for (String dir : dirs) {
            System.err.println("Merging files in directory " + dir);

            TrackAssembly track = DirScanner.scanDirectory(dir);
            //Process tracks for defaultness
            setDefaultTracks(track, options.getDefaultLanguage());
            //Adjust video track language
            track.getVideoTrack().setLanguage(options.getVideoTrackLanguage());

            String outputFile = new File(options.getOutputDir(),
                    track.getVideoTrack().getTrackName(options.getSeriesName(), options.getSeason(), track.getEpisode(),
                            names, options.getDefaultLanguage()) + ".mkv")
                    .getAbsolutePath();
            SimpleSeriesMerge ssm = new SimpleSeriesMerge(options.getSeriesName(), options.getSeason(), names, track,
                    outputFile, options.getDefaultLanguage());

            try {
                int result = ssm.merge();
                if (result > 0) {
                    System.err.println("Exit code from mkvmerge: " + result + ", aborting...");
                    System.exit(1);
                }
            } catch (IOException e) {
                System.err.println("Error merging track assembly");
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private static void setDefaultTracks(TrackAssembly track, String defaultLanguage) {
        track.getVideoTrack().setDefaultTrack(true);

        if (options.isSetSingleTrackAsDefault() && track.getAudioTracks().size() == 1) {
            //Single audio track, set default regardless of language
            track.getAudioTracks().get(0).setDefaultTrack(true);
        } else {
            track.getAudioTracks().stream()
                .filter(audioTrack -> defaultLanguage.equals(audioTrack.getTrackLanguage(defaultLanguage)))
                .findFirst()
                .ifPresent(audioTrack -> audioTrack.setDefaultTrack(true));
        }

        if (options.isSetSingleTrackAsDefault() && track.getSubtitleTracks().size() == 1) {
            //Single subtitle track, set as default regardless of language
            track.getSubtitleTracks().get(0).setDefaultTrack(true);
        } else {
            track.getSubtitleTracks().stream()
                .filter(subtitleTrack -> defaultLanguage.equals(subtitleTrack.getTrackLanguage(defaultLanguage)))
                .findFirst()
                .ifPresent(subtitleTrack -> subtitleTrack.setDefaultTrack(true));
        }
    }

    private static void initOptions() {
        try {
            options.configurePersistenceProvider(new FileConfig(System.getProperty("user.dir"), "ssm.properties"));
            options.load(false);
        } catch (OptionsException e) {
            System.err.println("Error loading options");
            e.printStackTrace();
            System.exit(1);
        }
    }


}
