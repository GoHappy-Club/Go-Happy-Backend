package com.startup.goHappy.controllers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.startup.goHappy.entities.model.Video;
import com.startup.goHappy.entities.repository.VideoRepository;
import io.swagger.annotations.ApiOperation;
import netscape.javascript.JSObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/videos")
public class VideoController {

    @Autowired
    VideoRepository videoService;

    @ApiOperation(value = "Get random videos for the overview screen")
    @GetMapping("/getRandom")
    public Set<Video> getRandomVideo() throws ExecutionException, InterruptedException {
        CollectionReference playlistsRef = videoService.getCollectionReference();
        double randomThreshold = Math.random();
        ApiFuture<QuerySnapshot> query = playlistsRef
                .whereGreaterThan("random", randomThreshold)
                .limit(6)
                .get();

        List<QueryDocumentSnapshot> documents = query.get().getDocuments();
        Set<Video> playlists = new LinkedHashSet<>();

        for (QueryDocumentSnapshot doc : documents) {
            playlists.add(doc.toObject(Video.class));
        }

        if (playlists.size() < 6) {
            ApiFuture<QuerySnapshot> fallbackQuery = playlistsRef
                    .whereLessThan("random", randomThreshold)
                    .limit(6 - playlists.size())
                    .get();

            List<QueryDocumentSnapshot> fallbackDocuments = fallbackQuery.get().getDocuments();
            for (QueryDocumentSnapshot doc : fallbackDocuments) {
                playlists.add(doc.toObject(Video.class));
            }
        }

        if (playlists.size() > 6) {
            return playlists.stream().limit(6).collect(Collectors.toSet());
        }
        return playlists;
    }


    @ApiOperation(value = "Add playlists to firestore")
    @PostMapping("/add")
    public void addVideo(@RequestBody JSONObject params) {
        String type = params.getString("type");
        if ("INDIVIDUAL".equals(type)) {
            Video newVideo = new Video();
            newVideo.setId(UUID.randomUUID().toString());
            newVideo.setCategory(params.getString("category"));
            newVideo.setTitle(params.getString("title"));
            newVideo.setThumbnail(params.getString("thumbnail"));
            newVideo.setRandom(Math.random());
            newVideo.setVideoUrl(params.getString("videoUrl"));
            newVideo.setPlaylistLink(params.getString("playlistLink"));
            videoService.save(newVideo);
        } else {
            List<Map<String, String>> videosMetadata = (List<Map<String, String>>) params.get("videos");
            for (Map<String, String> metadata : videosMetadata) {
                String title = metadata.get("title");
                String videoUrl = metadata.get("url");
                String thumbnail = metadata.get("thumbnail");
                Video newVideo = new Video();
                newVideo.setId(UUID.randomUUID().toString());
                newVideo.setCategory(params.getString("category"));
                newVideo.setTitle(title);
                newVideo.setThumbnail(thumbnail);
                newVideo.setRandom(Math.random());
                newVideo.setVideoUrl(videoUrl);
                newVideo.setPlaylistLink(params.getString("playlistUrl"));
                videoService.save(newVideo);
            }
        }
    }

}
