package com.atguan.gamll.list;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListServiceApplicationTests {


    @Autowired
    private JestClient jestClient;



    /**
     * 测试能否与es连通
     */
    @Test
    public void testES() throws IOException {

        //GET /movie_chn/movie/_search
        String query = "{\n" +
                "  \"query\": {\n" +
                "    \"term\": {\n" +
                "      \"actorList.name\": \"张译\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

        //查询get
        Search search = new Search.Builder(query).addIndex("movie_chn").addType("movie").build();

        //执行
        SearchResult searchResult = jestClient.execute(search);

        //获取执行结果
        List<SearchResult.Hit<Map, Void>> hits = searchResult.getHits(Map.class);

        for (SearchResult.Hit<Map, Void> hit : hits) {
            Map source = hit.source;
            System.err.println(source.get("name"));
        }

    }

}
