package com.sun.bilibili.service;

import com.mysql.cj.x.protobuf.MysqlxCrud;
import com.sun.bilibili.dao.VideoDao;
//import com.sun.bilibili.domain.*;
import com.sun.bilibili.domain.*;
import com.sun.bilibili.domain.exception.ConditionException;
import com.sun.bilibili.service.util.FastDFSUtil;
import com.sun.bilibili.service.util.ImageUtil;
import com.sun.bilibili.service.util.IpUtil;
import eu.bitwalker.useragentutils.UserAgent;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class VideoService {

    @Autowired
    private VideoDao videoDao;

    @Autowired
    private FastDFSUtil fastDFSUtil;

    @Autowired
    private UserCoinService userCoinService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileService fileService;

    @Autowired
    private ImageUtil imageUtil;

    private static final int FRAME_NO = 256;

    @Transactional
    public void adaVideos(Video video) {
        Date now=new Date();
        video.setCreateTime(now);
        videoDao.addVideos(video);
        Long videoId=video.getId();
        List<VideoTag> tagList=video.getVideoTagList();
        for(VideoTag tag:tagList){
            tag.setCreateTime(now);
            tag.setVideoId(videoId);
        }
        videoDao.batchAddVideoTags(tagList);
    }

    public PageResult<Video> pageListVideos(Integer size, Integer no, String area) {
        if (size==null||no==null){
            throw new ConditionException("参数异常！");
        }
        Map<String,Object> params=new HashMap<>();
        params.put("start",(no-1)*size);
        params.put("limit",size);
        params.put("area",area);
        List<Video> list=new ArrayList<>();
        Integer total=videoDao.pageCountVideos(params);
        if (total>0){
            list=videoDao.pageListVideos(params);
        }
        return new PageResult<>(total,list);
    }

    public void viewVideoOnlineBySlices(HttpServletRequest request, HttpServletResponse response, String url) throws Exception{
        fastDFSUtil.viewVideoOnlineBySlices(request,response,url);
    }

    public void addVideoLike(Long videoId, Long userId) {
        Video video=videoDao.getVideoById(videoId);
        if(video==null){
            throw new ConditionException("非法视频！");
        }
        VideoLike videoLike=videoDao.getVideoLikeByVideoIdANdUserId(videoId,userId);
        if(videoLike!=null){
            throw new ConditionException("已经赞过！");
        }
        videoLike=new VideoLike();
        videoLike.setVideoId(videoId);
        video.setUserId(userId);
        videoLike.setCreateTime(new Date());
        videoDao.addVideoLike(videoLike);
    }

    public void deleteVideoLike(Long videoId, Long userId) {
        videoDao.deleteVideoLike(videoId,userId);
    }

    public Map<String, Object> getVideoLikes(Long videoId, Long userId) {
        Long count=videoDao.getVideoLikes(videoId);
        VideoLike videoLike=videoDao.getVideoLikeByVideoIdANdUserId(videoId,userId);
        boolean like=videoLike!=null;
        Map<String,Object> result=new HashMap<>();
        result.put("count",count);
        result.put("like",like);
        return result;
    }

    @Transactional
    public void addVideoCollection(VideoCollection videoCollection, Long userId) {
        Long videoId=videoCollection.getVideoId();
        Long groupId=videoCollection.getGroupId();
        if(videoId==null||groupId==null){
            throw new ConditionException("参数异常！");
        }
        Video video=videoDao.getVideoById(videoId);
        if(video==null){
            throw new ConditionException("参数异常！");
        }
        videoDao.deleteVideoCollection(videoId,userId);
        videoCollection.setCreateTime(new Date());
        videoCollection.setUserId(userId);
        videoDao.addVideoCollection(videoCollection);
    }

    public void deleteVideoCollection(Long videoId, Long userId) {
        videoDao.deleteVideoCollection(videoId,userId);
    }

    public Map<String, Object> getVideoCollections(Long videoId, Long userId) {
        Long count=videoDao.getVideoCollections(videoId);
        VideoCollection videoCollection=videoDao.getVideoCollectionByVideoIdANdUserId(videoId,userId);
        boolean like=videoCollection!=null;
        Map<String,Object> result=new HashMap<>();
        result.put("count",count);
        result.put("like",like);
        return result;
    }

    @Transactional
    public void addVideoCoins(VideoCoin videoCoin, Long userId) {
        Long videoId=videoCoin.getVideoId();
        Integer amount=videoCoin.getAmount();
        if(videoId==null){
            throw new ConditionException("参数异常！");
        }
        Video video=videoDao.getVideoById(videoId);
        if(video==null){
            throw new ConditionException("非法视频！");
        }
        Integer userCoinsAmount=userCoinService.getUserCoinsAmount(userId);
        userCoinsAmount=userCoinsAmount==null?0:userCoinsAmount;
        if (amount>userCoinsAmount){
            throw new ConditionException("数量不足");
        }
        VideoCoin dbVideoCoin=videoDao.getVideoCoinByVideoIdAndUserId(videoId,userId);
        if(dbVideoCoin==null){
            videoCoin.setUserId(userId);
            videoCoin.setCreateTime(new Date());
            videoDao.addVideoCoin(videoCoin);
        }else {
            Integer dbAmount=dbVideoCoin.getAmount();
            dbAmount+=amount;
            videoCoin.setUserId(userId);
            videoCoin.setAmount(dbAmount);
            videoCoin.setUpdateTime(new Date());
            videoDao.updateVideoCoin(videoCoin);
        }
        userCoinService.updateUserCoinAmount(userId,amount);
    }

    public Map<String, Object> getVideoCoins(Long videoId, Long userId) {
        Long amount=videoDao.getVideoCoinAmount(videoId);
        VideoCoin videoCollection=videoDao.getVideoCoinByVideoIdAndUserId(videoId,userId);
        boolean like=videoCollection!=null;
        Map<String,Object> result=new HashMap<>();
        result.put("count",amount);
        result.put("like",like);
        return result;
    }

    public void addVideoComment(VideoComment videoComment, Long userId) {
        Long videoId=videoComment.getVideoId();
        if(videoId==null){
            throw new ConditionException("参数错误！");
        }
        Video video=videoDao.getVideoById(videoId);
        if(video==null){
            throw new ConditionException("非法视频！");
        }
        videoComment.setUserId(userId);
        videoComment.setCreateTime(new Date());
        videoDao.addVideoComment(videoComment);
    }

    public PageResult<VideoComment> pageListVideoComments(Integer size, Integer no, Long videoId) {
        Video video=videoDao.getVideoById(videoId);
        if(video==null){
            throw new ConditionException("非法视频");
        }
        Map<String,Object> param=new HashMap();
        param.put("start",(no-1)*size);
        param.put("limit",size);
        param.put("videoId",videoId);
        Integer total=videoDao.pageCountVideoComments(param);
        List<VideoComment> list=new ArrayList<>();
        if(total>0) {
            list = videoDao.pageListVideoCmments(param);
            //批量查询二级评论
            List<Long> parentIdList = list.stream().map(VideoComment::getId).collect(Collectors.toList());
            List<VideoComment> childCommentList = videoDao.batchGetVideoCommentsByRootIds(parentIdList);
            //批量查询用户信息
            Set<Long> userIdList = list.stream().map(VideoComment::getUserId).collect(Collectors.toSet());
            Set<Long> replyUserIdList = childCommentList.stream().map(VideoComment::getUserId).collect(Collectors.toSet());
            Set<Long> childUserIdList = childCommentList.stream().map(VideoComment::getReplyUserId).collect(Collectors.toSet());
            userIdList.addAll(replyUserIdList);
            userIdList.addAll(childUserIdList);
            List<UserInfo> userInfoList = userService.batchGetUserInfoByUserIds(userIdList);
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getUserId, userInfo -> userInfo));
            list.forEach(comment -> {
                Long id = comment.getId();
                List<VideoComment> childList = new ArrayList<>();
                childCommentList.forEach(child -> {
                    if (id.equals(child.getRootId())) {
                        child.setUserInfo(userInfoMap.get(child.getUserId()));
                        child.setReplyUserInfo(userInfoMap.get(child.getReplyUserId()));
                        childList.add(child);
                    }
                });
                comment.setChildList(childList);
                comment.setUserInfo(userInfoMap.get(comment.getUserId()));
            });
        }
        return new PageResult<>(total,list);
    }

    public Map<String, Object> getVideoDetails(Long videoId) {
        Video video=videoDao.getVideoDetails(videoId);
        Long userId=video.getUserId();
        User user=userService.getUserInfo(userId);
        UserInfo userInfo=user.getUserInfo();
        Map<String,Object> result=new HashMap<>();
        result.put("video",video);
        result.put("userInfo",userInfo);
        return result;
    }

    public void addVideoView(VideoView videoView, HttpServletRequest request) {
        Long userId=videoView.getUserId();
        Long videoId=videoView.getVideoId();
        String agent=request.getHeader("User-Agent");
        UserAgent userAgent=UserAgent.parseUserAgentString(agent);
        String clientId=String.valueOf(userAgent.getId());
        String ip= IpUtil.getIP(request);
        Map<String,Object> param=new HashMap<>();
        if(userId!=null){
            param.put("userId",userId);
        }else {
            param.put("ip",ip);
            param.put("clientId",clientId);
        }
        Date date=new Date();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        param.put("today",simpleDateFormat.format(date));
        param.put("videoId",videoId);
        VideoView dbVideoView=videoDao.getVideoView(param);
        if(dbVideoView!=null){
            videoView.setIp(ip);
            videoView.setClientId(clientId);
            videoView.setCreateTime(new Date());
            videoDao.addVideoView(videoView);
        }
    }

    public Integer getVideoViewCounts(Long videoId) {
        return videoDao.getVideoViewCounts(videoId);
    }

    public List<Video> recommend(Long userId) throws TasteException {
        List<UserPreference> list = videoDao.getAllUserPreference();
        //创建数据模型
        DataModel dataModel = this.createDataModel(list);
        //获取用户相似程度
        UserSimilarity similarity = new UncenteredCosineSimilarity(dataModel);
        System.out.println(similarity.userSimilarity(11, 12));
        //获取用户邻居
        UserNeighborhood userNeighborhood = new NearestNUserNeighborhood(2, similarity, dataModel);
        long[] ar = userNeighborhood.getUserNeighborhood(userId);
        //构建推荐器
        Recommender recommender = new GenericUserBasedRecommender(dataModel, userNeighborhood, similarity);
        //推荐视频
        List<RecommendedItem> recommendedItems = recommender.recommend(userId, 5);
        List<Long> itemIds = recommendedItems.stream().map(RecommendedItem::getItemID).collect(Collectors.toList());
        return videoDao.batchGetVideosByIds(itemIds);
    }


    public List<Video> recommendByItem(Long userId, Long itemId, int howMany) throws TasteException {
        List<UserPreference> list = videoDao.getAllUserPreference();
        //创建数据模型
        DataModel dataModel = this.createDataModel(list);
        //获取内容相似程度
        ItemSimilarity similarity = new UncenteredCosineSimilarity(dataModel);
        GenericItemBasedRecommender genericItemBasedRecommender = new GenericItemBasedRecommender(dataModel, similarity);
        // 物品推荐相拟度，计算两个物品同时出现的次数，次数越多任务的相拟度越高
        List<Long> itemIds = genericItemBasedRecommender.recommendedBecause(userId, itemId, howMany)
                .stream()
                .map(RecommendedItem::getItemID)
                .collect(Collectors.toList());
        //推荐视频
        return videoDao.batchGetVideosByIds(itemIds);
    }

    private DataModel createDataModel(List<UserPreference> userPreferenceList) {
        FastByIDMap<PreferenceArray> fastByIdMap = new FastByIDMap<>();
        Map<Long, List<UserPreference>> map = userPreferenceList.stream().collect(Collectors.groupingBy(UserPreference::getUserId));
        Collection<List<UserPreference>> list = map.values();
        for(List<UserPreference> userPreferences : list){
            GenericPreference[] array = new GenericPreference[userPreferences.size()];
            for(int i = 0; i < userPreferences.size(); i++){
                UserPreference userPreference = userPreferences.get(i);
                GenericPreference item = new GenericPreference(userPreference.getUserId(), userPreference.getVideoId(), userPreference.getValue());
                array[i] = item;
            }
            fastByIdMap.put(array[0].getUserID(), new GenericUserPreferenceArray(Arrays.asList(array)));
        }
        return new GenericDataModel(fastByIdMap);
    }

    public List<VideoBinaryPicture> convertVideoToImage(Long videoId, String fileMd5) throws Exception{
        com.sun.bilibili.domain.File file = fileService.getFileByMd5(fileMd5);
        String filePath = "/Users/hat/tmpfile/fileForVideoId" + videoId + "." + file.getType();
        fastDFSUtil.downLoadFile(file.getUrl(), filePath);
        FFmpegFrameGrabber fFmpegFrameGrabber = FFmpegFrameGrabber.createDefault(filePath);
        fFmpegFrameGrabber.start();
        int ffLength = fFmpegFrameGrabber.getLengthInFrames();
        Frame frame;
        Java2DFrameConverter converter = new Java2DFrameConverter();
        int count = 1;
        List<VideoBinaryPicture> pictures = new ArrayList<>();
        for(int i=1; i<= ffLength; i ++){
            long timestamp = fFmpegFrameGrabber.getTimestamp();
            frame = fFmpegFrameGrabber.grabImage();
            if(count == i){
                if(frame == null){
                    throw new ConditionException("无效帧");
                }
                BufferedImage bufferedImage = converter.getBufferedImage(frame);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", os);
                InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
                //输出黑白剪影文件
                java.io.File outputFile = java.io.File.createTempFile("convert-" + videoId + "-", ".png");
                BufferedImage binaryImg = imageUtil.getBodyOutline(bufferedImage, inputStream);
                ImageIO.write(binaryImg, "png", outputFile);
                //有的浏览器或网站需要把图片白色的部分转为透明色，使用以下方法可实现
                imageUtil.transferAlpha(outputFile, outputFile);
                //上传视频剪影文件
                String imgUrl = fastDFSUtil.uploadCommonFile(outputFile, "png");
                VideoBinaryPicture videoBinaryPicture = new VideoBinaryPicture();
                videoBinaryPicture.setFrameNo(i);
                videoBinaryPicture.setUrl(imgUrl);
                videoBinaryPicture.setVideoId(videoId);
                videoBinaryPicture.setVideoTimestamp(timestamp);
                pictures.add(videoBinaryPicture);
                count += FRAME_NO;
                //删除临时文件
                outputFile.delete();
            }
        }
        //删除临时文件
        java.io.File tmpFile=new java.io.File(filePath);
        tmpFile.delete();
        //批量添加视频剪影文件
        videoDao.batchAddVideoBinaryPictures(pictures);
        return pictures;
    }

}
