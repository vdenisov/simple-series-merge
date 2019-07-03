package org.plukh.ssm;

import org.apache.commons.io.FilenameUtils;
import org.plukh.options.OptionsException;
import org.plukh.options.OptionsFactory;
import org.plukh.options.impl.persistence.FileConfig;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class MergeAfterDownload {
    private static MergeOptions options = (MergeOptions) OptionsFactory.getOptionsInstance(MergeOptions.class);

    private static final Set<String> ALLOWED_EXTENSIONS;

    static {
        Set<String> s = new HashSet<>();
        s.add("mkv");
        s.add("ts");
        s.add("mp4");
        s.add("avi");
        ALLOWED_EXTENSIONS = Collections.unmodifiableSet(s);
    }

    public static void main(String[] args) throws IOException {
        System.err.println("SimpleSeriesMerge starting (file mode)...");
        Utils.init();
        initOptions();
        EpisodeNames names = initEpisodeNames();
        List<String> filesToScan = readFilesList();
        mergeEpisodes(filesToScan, names);
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

    private static List<String> readFilesList() {
        System.err.println("Scanning base directory " + options.getBaseDir());

        File baseDir = new File(options.getBaseDir());
        File[] files = baseDir.listFiles(pathname -> !pathname.isDirectory() &&
                ALLOWED_EXTENSIONS.contains(FilenameUtils.getExtension(pathname.getName()).toLowerCase()));

        List<String> filesToScan = new LinkedList<>();
        for (File file : files) {
            filesToScan.add(file.getAbsolutePath());
        }

        System.err.println("Found " + filesToScan.size() + " files");

        return filesToScan;
    }

    private static void mergeEpisodes(List<String> files, EpisodeNames names) {
        for (String file : files) {
            System.err.println("Merging files in directory " + options.getBaseDir());

            try {
                String ignoredAudioTracksStr = options.getIgnoredAudioTracks();
                Set<Integer> ignoredAudioTracks = ignoredAudioTracksStr != null ?
                        Arrays.asList(ignoredAudioTracksStr.split(",")).stream().map(Integer::parseInt).collect(Collectors.toSet()) :
                        Collections.emptySet();

                System.err.println("Will ignore audio tracks: " + ignoredAudioTracks);

                TrackAssembly track = FileScanner.scanFile(file, getLangs(options.getAudioLanguagesToKeep()),
                        getLangs(options.getSubtitleLanguagesToKeep()), ignoredAudioTracks, options.getDefaultLanguage());

                //Process tracks for defaultness
                setDefaultTracks(track, options.getDefaultLanguage());

                //Adjust video track language
                track.getVideoTrack().setLanguage(options.getVideoTrackLanguage());

                String outputFile = new File(options.getOutputDir(),
                        filterForWindows(track.getVideoTrack().getTrackName(options.getSeriesName(), options.getSeason(), track.getEpisode(),
                                names, options.getDefaultLanguage())) + ".mkv")
                        .getAbsolutePath();
                SimpleSeriesMerge ssm = new SimpleSeriesMerge(options.getSeriesName(), options.getSeason(), names, track,
                        outputFile, options.getDefaultLanguage());

                int result = ssm.merge();
                if (result > 0) {
                    System.err.println("Exit code from mkvmerge: " + result + ", aborting...");
                    System.exit(1);
                }
            } catch (IOException | UnknownEpisodeException e) {
                System.err.println("Error merging track assembly");
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    private static String filterForWindows(String name) {
        return name.replaceAll("\\?", "").replaceAll(":", " -").replaceAll("[\\\\/]", "-");
    }

    private static Set<String> getLangs(String langList) {
        return new HashSet<>(Arrays.asList(langList.split(",")));
    }

    private static void setDefaultTracks(TrackAssembly track, String defaultLanguage) {
        track.getVideoTrack().setDefaultTrack(true);

        //Set default audio track based on language
        if (options.isSetSingleTrackAsDefault() && track.getAudioTracks().size() == 1) {
            //Single audio track, set default regardless of language
            track.getAudioTracks().get(0).setDefaultTrack(true);
        } else {
            track.getAudioTracks().stream()
                .filter(audioTrack -> options.getDefaultLanguage().equals(audioTrack.getTrackLanguage(defaultLanguage)))
                .findFirst()
                .ifPresent(audioTrack -> audioTrack.setDefaultTrack(true));
        }

        //Set default subtitle track based on language
        if (options.isSetSingleTrackAsDefault() && track.getSubtitleTracks().size() == 1) {
            //Single subtitle track, set as default regardless of language
            track.getSubtitleTracks().get(0).setDefaultTrack(true);
        } else {
            track.getSubtitleTracks().stream()
                .filter(subtitleTrack -> options.getDefaultLanguage().equals(subtitleTrack.getTrackLanguage(defaultLanguage)))
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
