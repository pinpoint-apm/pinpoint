/*
 * Copyright 2022 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pinpoint.test.plugin;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexModel;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.PushOptions;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertManyResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonArray;
import org.bson.BsonBinary;
import org.bson.BsonBinarySubType;
import org.bson.BsonBoolean;
import org.bson.BsonDateTime;
import org.bson.BsonDbPointer;
import org.bson.BsonDecimal128;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonJavaScript;
import org.bson.BsonJavaScriptWithScope;
import org.bson.BsonNull;
import org.bson.BsonObjectId;
import org.bson.BsonRegularExpression;
import org.bson.BsonString;
import org.bson.BsonSymbol;
import org.bson.BsonTimestamp;
import org.bson.BsonUndefined;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static com.mongodb.client.model.Filters.ne;
import static com.mongodb.client.model.Filters.nor;
import static com.mongodb.client.model.Filters.not;
import static com.mongodb.client.model.Filters.or;
import static com.mongodb.client.model.Filters.regex;
import static com.mongodb.client.model.Updates.addEachToSet;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.pullAll;
import static com.mongodb.client.model.Updates.pushEach;
import static com.mongodb.client.model.Updates.set;
import static java.util.Arrays.asList;

@RestController
public class MongoPluginController {
    private static final String DATABASE_NAME = "myMongoDb";
    private static final String DATABASE_NAME_2 = "myMongoDb2";
    private static final String COLLECTION_NAME = "customers";

    private final MongoServer mongoServer;
    private MongoClient mongoClient;

    public MongoPluginController(MongoServer mongoServer) {
        this.mongoServer = Objects.requireNonNull(mongoServer, "mongoServer");
    }

    @PostConstruct
    private void start() {
        this.mongoClient = MongoClients.create(mongoServer.getUri());
    }

    @PreDestroy
    private void shutdown() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    @RequestMapping(value = "/mongodb/insert")
    public String insert() {
        MongoCollection<Document> collection = getDatabase(DATABASE_NAME).getCollection(COLLECTION_NAME);
        Document doc = new Document("name", "pinpoint").append("company", "Naver");
        InsertOneResult result = collection.insertOne(doc);
        return "Insert=" + result;
    }

    @RequestMapping(value = "/mongodb/insertMany")
    public String insertMany() {
        MongoCollection<Document> collection = getDatabase(DATABASE_NAME).getCollection(COLLECTION_NAME);
        List<Document> documentList = new ArrayList<>();
        Document doc = new Document("name", "manymanay").append("company", "ManyCompany");
        Document doc2 = new Document("name", "manymanay2").append("company", "ManyCompany2");
        documentList.add(doc);
        documentList.add(doc2);

        for (int i = 3; i < 100; i++)
            documentList.add(new Document("name", "manymanay" + i).append("company", "ManyCompany"));

        InsertManyResult result = collection.insertMany(documentList);
        return "Insert=" + result;
    }

    @RequestMapping(value = "/mongodb/insertComplexBson")
    public String insertComplex() {
        MongoCollection<Document> collection = getDatabase(DATABASE_NAME).getCollection(COLLECTION_NAME);
        BsonInt32[] bsonInt32s = new BsonInt32[40];
        for (int i = 0; i < 40; i++) {
            bsonInt32s[i] = new BsonInt32(i + 1);
        }

        BsonValue a = new BsonString("stest");
        BsonValue b = new BsonDouble(111);
        BsonValue c = new BsonBoolean(true);

        Document doc = new Document()
                .append("int32", new BsonInt32(12))
                .append("int64", new BsonInt64(77L))
                .append("bo\"olean", new BsonBoolean(true))
                .append("date", new BsonDateTime(new Date().getTime()))
                .append("double", new BsonDouble(12.3))
                .append("string", new BsonString("pinpoint"))
                .append("objectId", new BsonObjectId(new ObjectId()))
                .append("code", new BsonJavaScript("int i = 10;"))
                .append("codeWithScope", new BsonJavaScriptWithScope("int x = y", new BsonDocument("y", new BsonInt32(1))))
                .append("regex", new BsonRegularExpression("^test.*regex.*xyz$", "big"))
                .append("symbol", new BsonSymbol("wow"))
                .append("timestamp", new BsonTimestamp(0x12345678, 5))
                .append("undefined", new BsonUndefined())
                .append("binary1", new BsonBinary(new byte[]{(byte) 0xe0, 0x4f, (byte) 0xd0, 0x20}))
                .append("oldBinary", new BsonBinary(BsonBinarySubType.OLD_BINARY, new byte[]{1, 1, 1, 1, 1}))
                .append("arrayInt", new BsonArray(Arrays.asList(a, b, c, new BsonInt32(7))))
                .append("abbreviation", new BsonArray(Arrays.asList(bsonInt32s)))
                .append("document", new BsonDocument("a", new BsonInt32(77)))
                .append("dbPointer", new BsonDbPointer("db.coll", new ObjectId()))
                .append("null", new BsonNull())
                .append("decimal128", new BsonDecimal128(new Decimal128(55)));
        InsertOneResult result = collection.insertOne(doc);

        return "Insert=" + result;
    }

    @RequestMapping(value = "/mongodb/insertTo2ndserver")
    public String insertTo2ndserver() {
        MongoCollection<Document> collection = getDatabase(DATABASE_NAME_2).getCollection(COLLECTION_NAME);
        Document doc = new Document("name", "pinpoint").append("company", "Naver");
        collection.insertOne(doc);

        return "OK";
    }

    @RequestMapping(value = "/mongodb/insertNested")
    public String insertNested() {
        MongoCollection<Document> collection = getDatabase(DATABASE_NAME_2).getCollection(COLLECTION_NAME);
        Document doc2 = new Document("name", "pinpoint2").append("company", new Document("nestedDoc", "1"));
        InsertOneResult result = collection.insertOne(doc2);
        return "Insert=" + result;
    }

    @RequestMapping(value = "/mongodb/insertArray")
    public String insertArray() {
        MongoCollection<Document> collection = getDatabase(DATABASE_NAME_2).getCollection(COLLECTION_NAME);
        BsonValue a = new BsonDouble(111);
        BsonValue b = new BsonDouble(222);
        BsonArray bsonArray = new BsonArray();
        bsonArray.add(a);
        bsonArray.add(b);

        Document doc = new Document("array", bsonArray);
        InsertOneResult result = collection.insertOne(doc);
        return "Insert=" + result;
    }

    @RequestMapping(value = "/mongodb/insertCollection")
    public String insertCollection() {
        MongoCollection<Document> collection = getDatabase(DATABASE_NAME_2).getCollection(COLLECTION_NAME);
        Document doc = new Document("Java_Collection", Arrays.asList("naver", "apple"));
        InsertOneResult result = collection.insertOne(doc);
        return "Insert=" + result;
    }

    @RequestMapping(value = "/mongodb/find")
    public String find() {
        MongoCollection<Document> collection = getDatabase(DATABASE_NAME_2).getCollection(COLLECTION_NAME);
        Document document = new Document()
                .append("name", new BsonString("roy2"))
                .append("company", new BsonString("Naver2"));

        FindIterable<Document> documents = collection.find(document);
        return toString(documents);
    }

    @RequestMapping(value = "/mongodb/index")
    public String index() {
        MongoCollection<Document> collection = getDatabase(DATABASE_NAME_2).getCollection(COLLECTION_NAME);
        collection.createIndex(Indexes.ascending("name", "company"));

        List<IndexModel> indexes = new ArrayList<>();
        indexes.add(new IndexModel(Indexes.ascending("name2", "company")));
        indexes.add(new IndexModel(Indexes.ascending("name3", "company")));

        List<String> list = collection.createIndexes(indexes);
        return list.toString();
    }

    @RequestMapping(value = "/mongodb/update")
    public String update() {
        MongoCollection<Document> collection = getDatabase(DATABASE_NAME_2).getCollection(COLLECTION_NAME);
        Document doc1 = new Document("name", "pinpoint").append("company", "Naver");
        Document doc2 = new Document("$set", new Document("name", "pinpoint2").append("company", "Naver2"));

        UpdateResult updateResult = collection.updateOne(doc1, doc2);
        return "Update=" + updateResult.getMatchedCount();
    }

    @RequestMapping(value = "/mongodb/updateSimple")
    public String updateWithSet() {
        MongoCollection<Document> collection = getDatabase(DATABASE_NAME_2).getCollection(COLLECTION_NAME);
        Document doc1 = new Document("name", "pinpoint").append("company", "Naver");

        UpdateResult updateResult = collection.updateOne(doc1, and(set("name", "pinpointWithSet"), set("company", "NaverWithSet")));
        return "Update=" + updateResult.getMatchedCount();
    }

    @RequestMapping(value = "/mongodb/updateWithEach")
    public String updateWithEach() {
        MongoCollection<Document> collection = getDatabase(DATABASE_NAME_2).getCollection(COLLECTION_NAME);
        Document doc1 = new Document("name", "pinpoint").append("company", "Naver");

        UpdateResult updateResult = collection.updateOne(doc1, addEachToSet("arrayField", asList("pinpoint", "pinpoint2")), new UpdateOptions().upsert(true));
        return "Update=" + updateResult.getMatchedCount();
    }

    @RequestMapping(value = "/mongodb/updatePush")
    public String updatePush() {
        MongoCollection<Document> collection = getDatabase(DATABASE_NAME_2).getCollection(COLLECTION_NAME);
        Document doc1 = new Document("name", "pinpoint").append("company", "Naver");

        UpdateResult updateResult = collection.updateOne(doc1, pushEach("arrayField", asList("pinpoint", "pinpoint2"), new PushOptions().position(1)));
        return "Update=" + updateResult.getMatchedCount();
    }

    @RequestMapping(value = "/mongodb/updatePullAll")
    public String updatePullAll() {
        MongoCollection<Document> collection = getDatabase(DATABASE_NAME_2).getCollection(COLLECTION_NAME);
        Document doc1 = new Document("name", "pinpoint").append("company", "Naver");

        UpdateResult updateResult = collection.updateOne(doc1, pullAll("arrayField", asList("pinpoint", "pinpoint2")));
        return "Update=" + updateResult.getMatchedCount();
    }

    @RequestMapping(value = "/mongodb/updateComposite")
    public String updateComposite() {
        MongoCollection<Document> collection = getDatabase(DATABASE_NAME_2).getCollection(COLLECTION_NAME);
        Document doc1 = new Document("name", "pinpoint").append("company", "Naver");

        UpdateResult updateResult = collection.updateOne(doc1, combine(asList(pushEach("arrayField", asList("pinpoint", "pinpoint2")), pushEach("arrayField", asList("pinpoint", "pinpoint2")))));
        return "Update=" + updateResult.getMatchedCount();
    }

    @RequestMapping(value = "/mongodb/sortCompound")
    public String sortCompound() {
        MongoCollection<Document> collection = getDatabase(DATABASE_NAME_2).getCollection(COLLECTION_NAME);
        FindIterable<Document> iterable = collection.find(not(eq("name", "pinpoint"))).sort(Sorts.ascending("name", "name2"));

        return toString(iterable);
    }

    private String toString(Iterable<Document> iterable) {
        StringJoiner joiner = new StringJoiner(",");
        for (Document document : iterable) {
            joiner.add(document.toJson());
        }
        return joiner.toString();
    }

    @RequestMapping(value = "/mongodb/filterEQ")
    public String filterEQ() {
        MongoCollection<Document> collection = getDatabase(DATABASE_NAME_2).getCollection(COLLECTION_NAME);
        Document doc2 = new Document("$set", new Document("name", "pinpoint2").append("company", "Naver2"));

        UpdateResult updateResult = collection.updateOne(eq("name", "pinpoint"), doc2);
        return "Update=" + updateResult.getMatchedCount();
    }

    @RequestMapping(value = "/mongodb/filterAND")
    public String filterAND() {
        MongoCollection<Document> collection = getDatabase(DATABASE_NAME_2).getCollection(COLLECTION_NAME);
        Document doc2 = new Document("$set", new Document("name", "pinpoint2").append("company", "Naver2"));

        UpdateResult updateResult = collection.updateOne(and(eq("name", "pinpoint"), eq("company", "Naver")), doc2);
        return "Update=" + updateResult.getMatchedCount();
    }

    @RequestMapping(value = "/mongodb/filterNE")
    public String filterNE() {
        MongoCollection<Document> collection = getDatabase(DATABASE_NAME_2).getCollection(COLLECTION_NAME);
        Document doc2 = new Document("$set", new Document("name", "pinpoint2").append("company", "Naver2"));

        UpdateResult updateResult = collection.updateOne(and(ne("name", "pinpoint"), ne("company", "Naver")), doc2);
        return "Update=" + updateResult.getMatchedCount();
    }

    @RequestMapping(value = "/mongodb/filterREGREX")
    public String filterREGREX() {
        MongoCollection<Document> collection = getDatabase(DATABASE_NAME_2).getCollection(COLLECTION_NAME);
        FindIterable<Document> iterable = collection.find(regex("name", "%inpoint", "i"));

        return toString(iterable);
    }

    @RequestMapping(value = "/mongodb/filterNOT")
    public String filterNOT() {
        MongoCollection<Document> collection = getDatabase(DATABASE_NAME_2).getCollection(COLLECTION_NAME);
        FindIterable<Document> iterable = collection.find(not(eq("name", "pinpoint")));

        return toString(iterable);
    }

    @RequestMapping(value = "/mongodb/filterIN")
    public String filterIN() {
        MongoCollection<Document> collection = getDatabase(DATABASE_NAME_2).getCollection(COLLECTION_NAME);
        FindIterable<Document> iterable = collection.find(in("name", "pinpoint", "pinpoint2"));

        return toString(iterable);
    }

    @RequestMapping(value = "/mongodb/filterGEO")
    public String filterGEO() {
        MongoCollection<Document> collection = getDatabase(DATABASE_NAME_2).getCollection(COLLECTION_NAME);
        String coords = "28.56402,79.93652a27.27569,26.16394a42.69404,20.02808a48.61541,51.37207a";
        String[] coors = coords.split("a");
        final List<List<Double>> polygons = new ArrayList<>();

        for (String coor : coors) {
            String[] coo = coor.split(",");
            polygons.add(Arrays.asList(Double.parseDouble(coo[0]), Double.parseDouble(coo[1])));
        }

        FindIterable<Document> iterable = collection.find(Filters.geoWithinPolygon("loc", polygons));

        return toString(iterable);
    }

    @RequestMapping(value = "/mongodb/filterTEXT")
    public String filterTEXT() {
        MongoCollection<Document> collection = getDatabase(DATABASE_NAME_2).getCollection(COLLECTION_NAME);
        FindIterable<Document> iterable = collection.find(Filters.text("bakery coffee"));

        return toString(iterable);
    }

    @RequestMapping(value = "/mongodb/filterORNOR")
    public String filterORNOR() {
        MongoCollection<Document> collection = getDatabase(DATABASE_NAME_2).getCollection(COLLECTION_NAME);
        FindIterable<Document> iterable = collection.find(
                and(
                        or(eq("name", "pinpoint"), eq("company", "Naver")),
                        nor(eq("name", "pinpoint"), eq("company", "Naver"))
                )
        );

        return toString(iterable);
    }


    @RequestMapping(value = "/mongodb/delete")
    public String delete() {
        MongoCollection<Document> collection = getDatabase(DATABASE_NAME_2).getCollection(COLLECTION_NAME);
        Document doc = new Document("name", "pinpoint").append("company", "Naver");
        DeleteResult deleteResult = collection.deleteMany(doc);

        return "Deleted=" + deleteResult.getDeletedCount();
    }

    private MongoDatabase getDatabase(final String databaseName) {
        return this.mongoClient.getDatabase(databaseName);
    }
}
