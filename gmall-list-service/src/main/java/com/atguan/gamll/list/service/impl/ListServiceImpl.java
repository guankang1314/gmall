package com.atguan.gamll.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguan.gmall.bean.SkuLsInfo;
import com.atguan.gmall.bean.SkuLsParams;
import com.atguan.gmall.bean.SkuLsResult;
import com.atguan.gmall.config.RedisUtil;
import com.atguan.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.Update;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.apache.lucene.util.QueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ListServiceImpl implements ListService {


    @Autowired
    private JestClient jestClient;

    @Autowired
    private RedisUtil redisUtil;


    public static final String ES_INDEX="gmall";

    public static final String ES_TYPE="SkuInfo";


    @Override
    public void saveSkuLsInfo(SkuLsInfo skuLsInfo) {

        Index index = new Index.Builder(skuLsInfo).index(ES_INDEX).type(ES_TYPE).id(skuLsInfo.getId()).build();

        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public SkuLsResult search(SkuLsParams skuLsParams) {


        //定义dsl语句
        String query = makeQueryStringForSearch(skuLsParams);

        //查询
        Search search = new Search.Builder(query).addIndex(ES_INDEX).addType(ES_TYPE).build();

        SearchResult result = null;
        //执行
        try {
            result = jestClient.execute(search);
            System.err.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SkuLsResult skuLsResult = makeResultForSearch(result,skuLsParams);

        return skuLsResult;
    }

    @Override
    public void incrHotScore(String skuId) {

        //获取jedis
        Jedis jedis = redisUtil.getJedis();

        String hotKey = "hotScore";

        Double count = jedis.zincrby(hotKey, 1, "skuId" + skuId);
        //按照一定规则更新
        if (count%10 == 0) {
            //更新语句
            updateHotScore(skuId,Math.round(count));
        }
    }

    private void updateHotScore(String skuId, long round) {

        //dsl语句
        String upd = "{\n" +
                "  \"doc\": {\n" +
                "    \"hotScore\":"+round+"\n" +
                "  }\n" +
                "}";
        Update build = new Update.Builder(upd).index(ES_INDEX).type(ES_TYPE).id(skuId).build();

        try {
            jestClient.execute(build);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private SkuLsResult makeResultForSearch(SearchResult result, SkuLsParams skuLsParams) {

        SkuLsResult skuLsResult = new SkuLsResult();

        List<SkuLsInfo> list = new ArrayList<>();

        //给集合赋值
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = result.getHits(SkuLsInfo.class);

        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {

            SkuLsInfo skuLsInfo = hit.source;

            if (hit.highlight != null && hit.highlight.size() > 0) {

                Map<String, List<String>> highlight = hit.highlight;
                List<String> skuName = highlight.get("skuName");

                //高亮sku
                String s = skuName.get(0);
                skuLsInfo.setSkuName(s);
            }
            list.add(skuLsInfo);
        }

        skuLsResult.setSkuLsInfoList(list);

        //total
        skuLsResult.setTotal(skuLsResult.getTotal());

        //page
        //long pages = skuLsResult.getTotal()%skuLsParams.getPageSize() == 0?skuLsResult.getTotal()/skuLsParams.getPageSize():(skuLsResult.getTotal()/skuLsParams.getPageSize())+1;
        long pages = (result.getTotal()+skuLsParams.getPageSize()-1)/skuLsParams.getPageSize();
        skuLsResult.setTotalPages(pages);

        //平台属性
        MetricAggregation aggregations = result.getAggregations();
        TermsAggregation groupby_attr = aggregations.getTermsAggregation("groupby_attr");
        List<TermsAggregation.Entry> buckets = groupby_attr.getBuckets();

        List<String> attrValueIdList = new ArrayList<>();
        for (TermsAggregation.Entry bucket : buckets) {
            String valueId = bucket.getKey();

            if (valueId != null) {
                attrValueIdList.add(valueId);
            }
        }

        skuLsResult.setAttrValueIdList(attrValueIdList);

        return skuLsResult;
    }

    private String makeQueryStringForSearch(SkuLsParams skuLsParams) {

        /**
         * GET gmall/SkuInfo/_search
         * {
         *   "query": {
         *     "bool": {
         *       "filter": [{"term":{"catalog3Id":"61"}},
         *       {"term":{"skuAttrValueList.valueId":"82"}}
         *       ],
         *       "must": [
         *         {
         *           "match": {
         *             "skuName": "华为"
         *           }
         *         }
         *       ]
         *     }
         *   },
         *     "highlight": {
         *       "pre_tags": ["<span style=color:red>"],
         *       "post_tags": ["</span>"],
         *       "fields": {"skuName": {}}
         *     },
         *     "from": 0
         *     ,"size": 20
         *     ,"sort": [
         *       {
         *         "hotScore": {
         *           "order": "desc"
         *         }
         *       }
         *     ],
         *     "aggs": {
         *       "groupby_attr": {
         *         "terms": {
         *           "field": "skuAttrValueList.valueId"
         *         }
         *       }
         *     }
         * }
         */
        //定义一个查询器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        //创建bool
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        //判断三级分类id
        if (skuLsParams.getCatalog3Id() != null && skuLsParams.getCatalog3Id().length() > 0) {

            //创建term
            TermQueryBuilder catalog3Id = new TermQueryBuilder("catalog3Id", skuLsParams.getCatalog3Id());

            //创建filter
            boolQueryBuilder.filter(catalog3Id);
        }

        //判断平台属性值valueId
        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {

            for (String valueId : skuLsParams.getValueId()) {

                //创建term
                TermQueryBuilder  termQueryBuilder= new TermQueryBuilder("skuAttrValueList.valueId", valueId);

                //创建filter
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }

        //判断keyword是否为空
        if (skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length() > 0) {

            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName",skuLsParams.getKeyword());

            //创建must
            boolQueryBuilder.must(matchQueryBuilder);

            //设置高亮
            HighlightBuilder highlighter = searchSourceBuilder.highlighter();
            //设置高亮规则
            highlighter.field("skuName");
            highlighter.preTags("<span style=color:red>");
            highlighter.postTags("</span>");
            //放入查询器
            searchSourceBuilder.highlight(highlighter);
        }

        //query
        searchSourceBuilder.query(boolQueryBuilder);

        //设置分页
        int from = (skuLsParams.getPageNo()-1)*skuLsParams.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(skuLsParams.getPageSize());

        //设置排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);


        //设置聚合
        TermsBuilder groupby_attr = AggregationBuilders.terms("groupby_attr");
        groupby_attr.field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupby_attr);

        String query = searchSourceBuilder.toString();
        System.err.println(query);
        return query;
    }
}
