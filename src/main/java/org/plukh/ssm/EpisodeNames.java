package org.plukh.ssm;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EpisodeNames {
    private static final Pattern NUMBERED_NAME = Pattern.compile("^(\\d+) *= *(.*)");

    private Map<Integer, String> episodeNames;

    public EpisodeNames() {
        episodeNames = new HashMap<>(50);
    }

    public void load(String episodeNamesFile) throws IOException {
        int episode = 0;

        List<String> lines = FileUtils.readLines(new File(episodeNamesFile), "UTF-8");
        for (String line : lines) {
            //Detect episode number and name
            Matcher m = NUMBERED_NAME.matcher(line);
            String name;
            if (m.matches()) {
                //Numbered name
                episode = Integer.parseInt(m.group(1));
                name = m.group(2);
            } else {
                //Unnumbered name
                episode++;
                name = line;
            }

            //Store in map
            episodeNames.put(episode, name);
        }
    }

    public String getEpisodeName(int episode) throws UnknownEpisodeException {
        String name = episodeNames.get(episode);
        if (name == null) throw new UnknownEpisodeException(episode);
        return name;
    }
}
