package com.sun.bilibili.service;

import com.sun.bilibili.dao.repository.UserInfoRepository;
import com.sun.bilibili.dao.repository.VideoRepository;
import com.sun.bilibili.domain.UserInfo;
import com.sun.bilibili.domain.Video;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ElasticSearchService{

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public void addUserInfo(UserInfo userInfo){
        userInfoRepository.save(userInfo);
    }

    public void addVideo(Video video){
        videoRepository.save(video);
    }

    public List<Map<String,Object>> getContents(String keyword,Integer pageNo,Integer pageSize) throws IOException {
        String[] indices={"videos","user-infos"};
        SearchRequest searchRequest=new SearchRequest(indices);
        SearchSourceBuilder searchSourceBuilder=new SearchSourceBuilder();
        searchSourceBuilder.from(pageNo-1);
        searchSourceBuilder.size(pageSize);
        MultiMatchQueryBuilder multiMatchQueryBuilder= QueryBuilders.multiMatchQuery(keyword,"title","nick","description");
        searchSourceBuilder.query(multiMatchQueryBuilder);
        searchRequest.source(searchSourceBuilder);
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        //高亮
        String[] array={"title","nick","description"};
        HighlightBuilder highlightBuilder=new HighlightBuilder();
        for(String s:array){
            highlightBuilder.fields().add(new HighlightBuilder.Field(s));
        }
        highlightBuilder.requireFieldMatch(false);//需要多个字段高亮就要设置成false;
        highlightBuilder.preTags("span style=\"color:red\">");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);
        SearchResponse searchResponse=restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        List<Map<String,Object>> re=new ArrayList<>();
        for(SearchHit hit:searchResponse.getHits()){
            Map<String, HighlightField> highLightBuilderFields=hit.getHighlightFields();
            Map<String,Object> sourceMap=hit.getSourceAsMap();
            for (String key:array){
                HighlightField field=highLightBuilderFields.get(key);
                if(field!=null){
                    Text[] fragments=field.getFragments();
                    String str=fragments.toString();
                    str=str.substring(1,str.length()-1);
                    sourceMap.put(key,str);
                }
            }
            re.add(sourceMap);
        }
        return re;
    }

    public Video getVideos(String keyword){
        return videoRepository.findByTitleLike(keyword);
    }

    public void deleteAllVideos(){
        videoRepository.deleteAll();
    }

}
