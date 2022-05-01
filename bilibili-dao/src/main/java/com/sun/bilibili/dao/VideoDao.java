package com.sun.bilibili.dao;

import com.sun.bilibili.domain.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface VideoDao {

    Integer addVideos(Video video);

    Integer batchAddVideoTags(List<VideoTag> videoTagList);

    Integer pageCountVideos(Map<String, Object> params);

    List<Video> pageListVideos(Map<String, Object> params);

    Video getVideoById(Long videoId);

    VideoLike getVideoLikeByVideoIdANdUserId(@Param("videoId") Long videoId, @Param("userId") Long userId);

    Integer addVideoLike(VideoLike videoLike);

    Integer deleteVideoLike(Long videoId, Long userId);

    Long getVideoLikes(Long videoId);

    Integer deleteVideoCollection(@Param("videoId") Long videoId, @Param("userId") Long userId);

    Integer addVideoCollection(VideoCollection videoCollection);

    Long getVideoCollections(Long videoId);

    VideoCollection getVideoCollectionByVideoIdANdUserId(@Param("videoId") Long videoId, @Param("userId") Long userId);

    VideoCoin getVideoCoinByVideoIdAndUserId(@Param("videoId") Long videoId, @Param("userId") Long userId);

    Integer addVideoCoin(VideoCoin videoCoin);

    Integer updateVideoCoin(VideoCoin videoCoin);

    Long getVideoCoinAmount(Long videoId);

    Integer addVideoComment(VideoComment videoComment);

    Integer pageCountVideoComments(Map<String, Object> param);

    List<VideoComment> pageListVideoCmments(Map<String, Object> param);

    List<VideoComment> batchGetVideoCommentsByRootIds(List<Long> parentIdList);

    Video getVideoDetails(Long videoId);

    VideoView getVideoView(Map<String,Object> param);


    Integer addVideoView(VideoView videoView);

    Integer getVideoViewCounts(Long videoId);
}
