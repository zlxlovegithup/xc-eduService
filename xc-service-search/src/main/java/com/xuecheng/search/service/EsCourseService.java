package com.xuecheng.search.service;

import com.xuecheng.framework.domain.course.CoursePub;
import com.xuecheng.framework.domain.course.TeachplanMediaPub;
import com.xuecheng.framework.domain.search.CourseSearchParam;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.QueryResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EsCourseService {

    //日志
    Logger LOGGER = LoggerFactory.getLogger(EsCourseService.class);

    @Value("${xuecheng.elasticsearch.course.index}")
    private String es_index; //ElasticSearch中的索引
    @Value("${xuecheng.elasticsearch.course.type}")
    private String es_type; //文档类型
    @Value("${xuecheng.elasticsearch.course.source_field}")
    private String source_field;
    @Value("${xuecheng.elasticsearch.media.index}")
    private String media_index;
    @Value("${xuecheng.elasticsearch.media.type}")
    private String media_type; //文档类型
    @Value("${xuecheng.elasticsearch.media.source_field}")
    private String media_source_field;


    @Autowired
    RestHighLevelClient restHighLevelClient;

    /**
     * 搜索的课程列表
     * @param page 当前页码
     * @param size 每页记录数
     * @param courseSearchParam 搜索的条件(关键字,一级分类,二级分类,难度等级,价格区间,排序字段,过滤字段)
     * @return
     */
    public QueryResponseResult<CoursePub> list(int page, int size, CourseSearchParam courseSearchParam){
        //设置索引
        SearchRequest searchRequest = new SearchRequest(es_index);
        //设置文档类型
        searchRequest.types(es_type);
        //创建搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //source源字段过滤 第一个参数结果集包括哪些字段，第二个参数表示结果集不包括哪些字段
        String[] source_fields = source_field.split(",");
        searchSourceBuilder.fetchSource(source_fields,new String[]{});

        //==== 搜索条件====
        //布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //△△根据关键字搜索,采用MultiMatchQuery
        if(StringUtils.isNotEmpty(courseSearchParam.getKeyword())){
            //匹配关键字 多字段(name,description,teachplan)查询
            MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders.multiMatchQuery(courseSearchParam.getKeyword(), "name", "description", "teachplan");
            multiMatchQueryBuilder.minimumShouldMatch("70%"); //关键字进行分词之后至少需要匹配(分词后的总次数*0.7再向下取整的词数)
            multiMatchQueryBuilder.field("name",10); //将name字段的权重(Boost值)提高十倍
            boolQueryBuilder.must(multiMatchQueryBuilder);
        }

        //△△根据分类,课程等级搜索,采用过滤器实现
        if(StringUtils.isNotEmpty(courseSearchParam.getMt())){
            //课程分类(大分类)
            boolQueryBuilder.filter(QueryBuilders.termQuery("mt",courseSearchParam.getMt()));
        }
        if(StringUtils.isNotEmpty(courseSearchParam.getSt())){
            //课程分类(小分类)
            boolQueryBuilder.filter(QueryBuilders.termQuery("st",courseSearchParam.getSt()));
        }
        if(StringUtils.isNotEmpty(courseSearchParam.getGrade())){
            //课程等级
            boolQueryBuilder.filter(QueryBuilders.termQuery("grade",courseSearchParam.getGrade()));
        }

        //△△进行分页
        if(page<=0){
            page = 1; //最少为第1页
        }
        if(size<=0){
            size = 20; //每页默认为10条数据
        }
        int start = (page - 1)*size;
        //设置分页
        searchSourceBuilder.from(start); //起始数据为第多少条数据
        searchSourceBuilder.size(size);  //每页显示的数据条数

        //设置布尔查询
        searchSourceBuilder.query(boolQueryBuilder);

        //△△高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<font class='blue>'");
        highlightBuilder.postTags("</font>");
        //设置高亮字段
        highlightBuilder.fields().add(new HighlightBuilder.Field("name"));

        //设置高亮查询
        searchSourceBuilder.highlighter(highlightBuilder);

        //设置查询
        searchRequest.source(searchSourceBuilder);

        SearchResponse searchResponse = null;
        try {
            //====执行搜索====
            searchResponse = restHighLevelClient.search(searchRequest);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("根据关键字搜索出现异常..{}",e.getMessage());
            return new QueryResponseResult<>(CommonCode.FAIL,new QueryResult<CoursePub>());
        }
        //对结果集进程处理
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        //取出记录总数
        long totalHits = hits.getTotalHits();
        //数据列表
        List<CoursePub> list = new ArrayList<>();

        for(SearchHit hit: searchHits){
            CoursePub coursePub = new CoursePub();
            //取出内容
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            //课程id
            String courseId = (String) sourceAsMap.get("id");
            coursePub.setId(courseId);
            //名称
            String name = (String) sourceAsMap.get("name");
            //取出高亮字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            if(highlightFields!=null){
                //取出name字段的高亮片段
                HighlightField highlightNameField = highlightFields.get("name");
                if(highlightNameField!=null){
                    Text[] fragments = highlightNameField.getFragments();
                    StringBuffer stringBuffer = new StringBuffer();
                    for (Text str:
                         fragments) {
                        stringBuffer.append(str.string());
                    }
                    name = stringBuffer.toString();
                }
            }
            coursePub.setName(name);
            //图片
            String pic = (String) sourceAsMap.get("pic");
            coursePub.setPic(pic);
            //价格
            Double price = null;
            try {
                if(sourceAsMap.get("price")!=null){
                    price = (double) sourceAsMap.get("price");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            coursePub.setPrice(price);
            //原价格
            Double price_old = null;
            try {
                if(sourceAsMap.get("price_old")!=null){
                    price_old = (double) sourceAsMap.get("price_old");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            coursePub.setPrice_old(price_old);
            //将数据存入集合
            list.add(coursePub);
        }


        QueryResult<CoursePub> queryResult = new QueryResult<>();
        queryResult.setList(list); //数据列表
        queryResult.setTotal(totalHits); //数据总数
        QueryResponseResult<CoursePub> coursePubQueryResponseResult = new QueryResponseResult<>(CommonCode.SUCCESS,queryResult);

        //返回结果
        return coursePubQueryResponseResult;
    }

    /**
     * 使用ES的客户端向ES请求查询索引信息
     * @param id
     * @return
     */
    public Map<String,CoursePub> getall(String id){
        //定义一个搜索请求对象
        SearchRequest searchRequest = new SearchRequest(es_index);
        //指定type
        searchRequest.types(es_type);

        //创建搜索源构建对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //查询条件，根据课程id查询
        //搜索方式
        //termQuery精准匹配
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("id", id);
        searchSourceBuilder.query(termQueryBuilder);
        //source源字段过滤 第一个参数结果集包括哪些字段，第二个参数表示结果集不包括哪些字段
        //searchSourceBuilder.fetchSource(new String[]{"name","grade","charge","pic"},new String[]{});

        //设置查询
        //取消source源字段过虑，查询所有字段
        searchRequest.source(searchSourceBuilder);

        Map<String,CoursePub> map = new HashMap<>();

        SearchResponse searchResponse = null;
        try {
            //执行搜索
            searchResponse = restHighLevelClient.search(searchRequest);
            //获取搜索到的结果
            SearchHits hits = searchResponse.getHits();
            //得到匹配度高的文档
            SearchHit[] searchHits = hits.getHits();

            for (SearchHit hit:
                    searchHits) {
                //获取源文档的内容
                Map<String, Object> sourceAsMap = hit.getSourceAsMap();
                String courseId = (String) sourceAsMap.get("id");
                String name = (String) sourceAsMap.get("name");
                String grade = (String) sourceAsMap.get("grade");
                String charge = (String) sourceAsMap.get("charge");
                String pic = (String) sourceAsMap.get("pic");
                String description = (String) sourceAsMap.get("description");
                String teachplan = (String) sourceAsMap.get("teachplan");
                CoursePub coursePub = new CoursePub();
                coursePub.setId(courseId);
                coursePub.setName(name);
                coursePub.setGrade(grade);
                coursePub.setCharge(charge);
                coursePub.setPic(pic);
                coursePub.setDescription(description);
                coursePub.setTeachplan(teachplan);
                map.put("courseId",coursePub);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }

    /**
     * 根据多个课程计划查询多个课程媒资信息
     * @param teachplanIds
     * @return
     */
    public QueryResponseResult<TeachplanMediaPub> getmedia(String[] teachplanIds){
        //定义一个搜索请求对象
        SearchRequest searchRequest = new SearchRequest(media_index);
        //指定type
        searchRequest.types(media_type);

        //定义SearchSourceBuilder
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //设置使用termsQuery根据多个id 查询
        searchSourceBuilder.query(QueryBuilders.termsQuery("teachplan_id",teachplanIds));
        //过虑源字段
        String[] includes = media_source_field.split(",");
        //source源字段过滤 第一个参数结果集包括哪些字段，第二个参数表示结果集不包括哪些字段
        searchSourceBuilder.fetchSource(includes,new String[]{});
        searchRequest.source(searchSourceBuilder);

        //使用es客户端进行搜索请求Es
        List<TeachplanMediaPub> teachplanMediaPubList = new ArrayList<>();
        long totalHits = 0;
        try {
            //执行搜索
            SearchResponse search = restHighLevelClient.search(searchRequest);
            SearchHits hits = search.getHits();
            totalHits = hits.totalHits; //匹配到的总记录数
            //得到匹配度高的文档
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit searchHit:
                    searchHits) {
                TeachplanMediaPub teachplanMediaPub = new TeachplanMediaPub();
                //取出课程计划媒资信息
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                String courseid = (String) sourceAsMap.get("courseid");
                String teachplan_id = (String) sourceAsMap.get("teachplan_id");
                String media_id = (String) sourceAsMap.get("media_id");
                String media_url = (String) sourceAsMap.get("media_url");
                String media_fileoriginalname = (String) sourceAsMap.get("media_fileoriginalname");
                teachplanMediaPub.setCourseId(courseid);
                teachplanMediaPub.setTeachplanId(teachplan_id);
                teachplanMediaPub.setMediaId(media_id);
                teachplanMediaPub.setMediaUrl(media_url);
                teachplanMediaPub.setMediaFileOriginalName(media_fileoriginalname);

                //将数据加入列表
                teachplanMediaPubList.add(teachplanMediaPub);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        QueryResult<TeachplanMediaPub> queryResult = new QueryResult<>();
        queryResult.setTotal(totalHits);
        queryResult.setList(teachplanMediaPubList);
        //返回操作结果
        QueryResponseResult<TeachplanMediaPub> queryResponseResult=new QueryResponseResult<>(CommonCode.SUCCESS,queryResult);
        return queryResponseResult;
    }
}
