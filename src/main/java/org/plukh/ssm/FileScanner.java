package org.plukh.ssm;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileScanner {
    private static final Set<String> EXTENSIONS;

    static {
        Set<String> ex = new HashSet<>();

        //Additional video not supported yet - must reside in the main file!
        ex.addAll(AudioTrack.getSupportedExtensions());
        ex.addAll(SubtitleTrack.getSupportedExtensions());
        //This is for external chapters
        ex.add("txt");

        EXTENSIONS = Collections.unmodifiableSet(ex);
    }

    private static final Pattern EPISODE_PATTERN = Pattern.compile("[ \\.][sS]\\d\\d[eE](\\d\\d)[ \\.]");

    public static TrackAssembly scanFile(String file, Set<String> audioLanguages, Set<String> subtitleLanguges,
                                         Set<Integer> ignoredAudioTracks,
                                         String defaultLanguage) throws IOException, UnknownEpisodeException {

        TrackAssembly trackAssembly = new TrackAssembly();

        //Prefer external tracks, where possible
        scanForExternalTracks(trackAssembly, file, audioLanguages, subtitleLanguges, defaultLanguage);
        scanForInternalTracks(trackAssembly, file, audioLanguages, subtitleLanguges, ignoredAudioTracks, defaultLanguage);

        Matcher m = EPISODE_PATTERN.matcher(file);
        if (!m.find()) throw new UnknownEpisodeException("Can't detect episode in file: " + file);
        trackAssembly.setEpisode(Integer.parseInt(m.group(1)));

        return trackAssembly;
    }

    private static void scanForExternalTracks(TrackAssembly trackAssembly, String file,
                                              Set<String> audioLanguages, Set<String> subtitleLanguges,
                                              String defaultLanguage) {
        List<Track> externalTracks = checkForExternalTracks(file, EXTENSIONS, defaultLanguage);

        trackAssembly.setAudioTracks(findExternalAudioTracks(externalTracks));
        trackAssembly.setSubtitleTracks(findExternalSubtitleTracks(externalTracks));
        if (externalChaptersPresent(externalTracks)) trackAssembly.setChapterFile(findExternalChapterFile(externalTracks));
    }

    private static <T extends Track> List<Track> findExternalTracksOfType(List<Track> tracks, Class<T> type) {
        return tracks.stream()
                .filter(track -> type.isAssignableFrom(track.getClass()))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private static String findExternalChapterFile(List<Track> tracks) {
        List<Track> chapterTracks = findExternalTracksOfType(tracks, ChapterTrack.class);
        if (chapterTracks.isEmpty()) return null;

        String chapterFile = chapterTracks.get(0).getFileName();
        System.err.println("Found external chapters file: " + chapterFile);

        return chapterFile;
    }

    private static boolean externalChaptersPresent(List<Track> tracks) {
        return !findExternalTracksOfType(tracks, ChapterTrack.class).isEmpty();
    }

    private static List<SubtitleTrack> findExternalSubtitleTracks(List<Track> tracks) {
        List<SubtitleTrack> subtitleTracks = findExternalTracksOfType(tracks, SubtitleTrack.class).stream().map(track
                -> (SubtitleTrack) track).collect(Collectors.toCollection(LinkedList::new));

        if (subtitleTracks.isEmpty()) {
            System.err.println("No external subtitle tracks found");
        } else {
            System.err.println("Found external subtitle tracks: ");
            subtitleTracks.stream().forEachOrdered(track -> System.err.println(track.toString()));
        }

        return subtitleTracks;
    }

    private static List<AudioTrack> findExternalAudioTracks(List<Track> tracks) {
        List<AudioTrack> audioTracks = findExternalTracksOfType(tracks, AudioTrack.class).stream().map(track
                -> (AudioTrack) track).collect(Collectors.toCollection(LinkedList::new));

        if (audioTracks.isEmpty()) {
            System.err.println("No external audio tracks found");
        } else {
            System.err.println("Found external audio tracks: ");
            audioTracks.stream().forEachOrdered(track -> System.err.println(track.toString()));
        }

        return audioTracks;
    }

    private static void scanForInternalTracks(TrackAssembly trackAssembly, String file,
                                              Set<String> audioLanguages, Set<String> subtitleLanguges,
                                              Set<Integer> ignoredAudioTracks, String defaultLanguage) throws IOException {
        //Run mkvmerge -J to extract track information
        //Note that mkvmerge now supplies track information in JSON format only, so the code is adjusted appropriately
        String command = "mkvmerge --ui-language en -J \"" + file + "\"";
        Process process = Runtime.getRuntime().exec(command);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new Utils.StreamGobbler(process.getInputStream(), out).start();
        new Utils.StreamGobbler(process.getErrorStream(), System.err).start();

        try {
            int result = process.waitFor();
            if (result > 0) throw new UnexpectedException("mkvmerge returned non-zero result code: " + result);
        } catch (InterruptedException e) {
            throw new UnexpectedException("Unexpectedly interrupted while waiting for mkvmerge to complete", e);
        }

        //Parse mkvmerge output
        List<MkvTrackInfo> tracks = MkvTrackInfo.parse(new ByteArrayInputStream(out.toByteArray()), defaultLanguage);
        System.err.println("Found " + tracks.size() + " internal tracks");
        for (MkvTrackInfo track : tracks) {
            System.err.println(track.toString());
        }

        //Assemble track info
        trackAssembly.setVideoTrack(findInternalVideoTrack(file, tracks));
        System.err.println("Set video track: " + trackAssembly.getVideoTrack());

        if (trackAssembly.getAudioTracks() == null || trackAssembly.getAudioTracks().isEmpty()) {
            trackAssembly.setAudioTracks(findInternalAudioTracks(file, tracks, audioLanguages, ignoredAudioTracks));
            System.err.println("Set internal audio tracks: " + trackAssembly.getAudioTracks());
        } else {
            System.err.println("External audio tracks present; ignoring internal");
        }

        if (trackAssembly.getSubtitleTracks() == null || trackAssembly.getSubtitleTracks().isEmpty()) {
            trackAssembly.setSubtitleTracks(findInternalSubtitleTracks(file, tracks, subtitleLanguges));
            System.err.println("Set internal subtitles: " + trackAssembly.getSubtitleTracks());
        } else {
            System.err.println("External subtitle tracks present; ignoring internal");
        }

        if (!StringUtils.isEmpty(trackAssembly.getChapterFile())) {
            System.err.println("External chapters track present; ignoring internal");
        } else
        if (isInternalChaptersPresent(tracks)) {
            trackAssembly.setChapterFile(file);
        }
    }

    private static List<Track> checkForExternalTracks(String file, Set<String> extensions, String defaultLanguage) {
        String sourceBaseName = FilenameUtils.getBaseName(file);

        File[] externalTrackFiles = new File(FilenameUtils.getFullPath(file)).listFiles((dir, name) -> {
            String baseName = FilenameUtils.getBaseName(name);
            //We check that this track filename starts with series name (so base name of "Series" will also match "Series - Suffix")
            return baseName.startsWith(sourceBaseName) && extensions.contains(FilenameUtils.getExtension(name));
        });

        List<Track> tracks = new LinkedList<>();

        for (File trackFile : externalTrackFiles) {
            String fileName = trackFile.getAbsolutePath();
            //Detect chapter track
            String extension = FilenameUtils.getExtension(fileName).toLowerCase();
            if (extension.equals("txt")) tracks.add(new ChapterTrack(fileName, 0));
            else
            if (AudioTrack.getSupportedExtensions().contains(extension))
                tracks.add(parseExternalAudioTrack(fileName, defaultLanguage));
            else
            if (SubtitleTrack.getSupportedExtensions().contains(extension))
                tracks.add(parseExternalSubtitleTrack(fileName, defaultLanguage));
            else System.err.println("File " + fileName + " is skipped");
        }

        return tracks;
    }

    private static SubtitleTrack parseExternalSubtitleTrack(String file, String defaultLanguage) {
        return new SubtitleTrack(file, 0, defaultLanguage, null);
    }

    private static AudioTrack parseExternalAudioTrack(String file, String defaultLanguage) {
        return new AudioTrack(file, 0, defaultLanguage, null);
    }

    private static List<MkvTrackInfo> findInternalTracksOfType(List<MkvTrackInfo> tracks, MkvTrackInfo.TrackType
            trackType) {
        return tracks.stream().filter(track -> track.getTrackType().equals(trackType)).collect
                (Collectors.toCollection(LinkedList::new));
    }

    private static VideoTrack findInternalVideoTrack(String file, List<MkvTrackInfo> tracks) {
        MkvTrackInfo videoTrack = findInternalTracksOfType(tracks, MkvTrackInfo.TrackType.VIDEO).get(0);

        VideoTrack vtr = new VideoTrack(file, videoTrack.getTrackNumber());
        vtr.setDefaultTrack(true);

        return vtr;
    }

    private static List<AudioTrack> findInternalAudioTracks(String file, List<MkvTrackInfo> tracks,
                                                            Set<String> languages, Set<Integer> ignoredAudioTracks) {
        List<MkvTrackInfo> audioTracks = findInternalTracksOfType(tracks, MkvTrackInfo.TrackType.AUDIO);

        return audioTracks.stream()
            .filter(track -> !ignoredAudioTracks.contains(track.getTrackNumber())
                && (track.getLanguage() == null || languages.contains(track.getLanguage())))
            .map(track -> new AudioTrack(file, track.getTrackNumber(), track.getLanguage(),
                track.getCodec())).collect(Collectors.toList());
    }

    private static List<SubtitleTrack> findInternalSubtitleTracks(String file, List<MkvTrackInfo> tracks,
                                                                  Set<String> languages) {
        List<MkvTrackInfo> subtitleTracks = findInternalTracksOfType(tracks, MkvTrackInfo.TrackType.SUBTITLES);
        return subtitleTracks.stream().filter(track -> languages.contains(track
                .getLanguage())).map(track -> new SubtitleTrack(file, track.getTrackNumber(), track.getLanguage(),
                track.getCodec())).collect(Collectors.toCollection(LinkedList::new));
    }

    private static boolean isInternalChaptersPresent(List<MkvTrackInfo> tracks) {
        List<MkvTrackInfo> chapterTracks = findInternalTracksOfType(tracks, MkvTrackInfo.TrackType.CHAPTERS);
        return !chapterTracks.isEmpty();
    }
}
