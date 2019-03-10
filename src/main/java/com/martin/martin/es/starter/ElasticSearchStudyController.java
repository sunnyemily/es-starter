package com.martin.martin.es.starter;

import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author sunhuazhong
 * @create 2019/3/2 下午3:09
 **/
@RestController
@RequestMapping("/es")
public class ElasticSearchStudyController {

    @Autowired
    private TransportClient transportClient;

    @GetMapping("/get/book/novel")
    @ResponseBody
    public ResponseEntity get(@RequestParam(name = "id", defaultValue = "") String id) {
        if (id.isEmpty()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        GetResponse result = transportClient.prepareGet("book", "novel", id).get();
        if (!result.isExists()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity(result.getSource(), HttpStatus.OK);
    }

    @PostMapping("/add/book/novel")
    @ResponseBody
    public ResponseEntity add(@RequestParam(name = "author") String author,
                              @RequestParam(name = "title") String title,
                              @RequestParam(name = "word_count") int wordCount,
                              @RequestParam(name = "publish_date")
                              @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date publishDate) {
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder()
                    .startObject()
                    .field("author", author)
                    .field("title", title)
                    .field("word_count", wordCount)
                    .field("publish_date", publishDate.getTime())
                    .endObject();
            IndexResponse result = transportClient.prepareIndex("book", "novel")
                    .setSource(builder).get();
            return new ResponseEntity(result.getId(), HttpStatus.OK);
        } catch (IOException ex) {
            ex.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/delete/book/novel")
    @ResponseBody
    public ResponseEntity delete(@RequestParam(name = "id") String id) {
        if (id.isEmpty()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        DeleteResponse result = transportClient.prepareDelete("book", "novel", id).get();
        return new ResponseEntity(result.getResult().toString(), HttpStatus.OK);
    }

    @PutMapping("/update/book/novel")
    @ResponseBody
    public ResponseEntity update(@RequestParam(name = "id") String id,
                                 @RequestParam(name = "author", required = false) String author,
                                 @RequestParam(name = "title", required = false) String title) {
        UpdateRequest update = new UpdateRequest("book", "novel", id);
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder()
                    .startObject();
            if (author != null) {
                builder.field("author", author);
            }
            if (title != null) {
                builder.field("title", title);
            }
            builder.endObject();

            update.doc(builder);

            UpdateResponse result = transportClient.update(update).get();
            return new ResponseEntity(result.getResult().toString(), HttpStatus.OK);
        } catch (IOException ex) {
            ex.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/query/book/novel")
    @ResponseBody
    public ResponseEntity query(@RequestParam(name = "author", required = false) String author,
                                @RequestParam(name = "title", required = false) String title,
                                @RequestParam(name = "gt_word_count", defaultValue = "0") Integer gtWordCount,
                                @RequestParam(name = "lt_word_count", required = false) Integer ltWordCount) {
        BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
        if (author != null) {
            boolBuilder.must(QueryBuilders.matchQuery("author", author));
        }
        if (title != null) {
            boolBuilder.must(QueryBuilders.matchQuery("title", title));
        }
        RangeQueryBuilder rangeBuilder = QueryBuilders.rangeQuery("word_count")
                .from(gtWordCount + 1);
        if (ltWordCount != null & ltWordCount > 0) {
            rangeBuilder.to(ltWordCount - 1);
        }
        boolBuilder.filter(rangeBuilder);

        SearchRequestBuilder builder = transportClient.prepareSearch("book").setTypes("novel")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(boolBuilder)
                .setFrom(0)
                .setSize(10);
        System.out.println(builder.toString());

        SearchResponse response = builder.get();
        List<Map<String, Object>> result = new ArrayList<>();
        for (SearchHit searchHit : response.getHits()) {
            result.add(searchHit.getSource());
        }

        return new ResponseEntity(result, HttpStatus.OK);
    }
}
