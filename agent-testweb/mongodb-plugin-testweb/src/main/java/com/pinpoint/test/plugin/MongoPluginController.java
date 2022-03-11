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
import com.mongodb.client.MongoCursor;
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
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
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
    private final String databaseName = "myMongoDb";
    private final String databaseName2 = "myMongoDb2";
    private final String collectionName = "customers";

    private int port = 27017;
    private MongodExecutable mongodExecutable;
    private MongodProcess mongod;


    @PostConstruct
    private void start() throws Exception {
        MongodStarter starter = MongodStarter.getDefaultInstance();

        MongodConfig mongodConfig = MongodConfig.builder()
                .version(Version.Main.PRODUCTION)
                .net(new Net(port, Network.localhostIsIPv6()))
                .build();

        this.mongodExecutable = starter.prepare(mongodConfig);
        this.mongod = mongodExecutable.start();
    }

    @PreDestroy
    private void shutdown() {
        if (mongod != null) {
            mongod.stop();
        }
        if (mongodExecutable != null) {
            mongodExecutable.stop();
        }
    }

    @RequestMapping(value = "/mongodb/insert")
    public String insert() {
        MongoCollection collection = getDatabase(databaseName).getCollection(collectionName);
        Document doc = new Document("name", "pinpoint").append("company", "Naver");
        InsertOneResult result = collection.insertOne(doc);
        return "Insert=" + result.toString();
    }

    @RequestMapping(value = "/mongodb/insertMany")
    public String insertMany() {
        MongoCollection collection = getDatabase(databaseName).getCollection(collectionName);
        List<Document> documentList = new ArrayList<>();
        Document doc = new Document("name", "manymanay").append("company", "ManyCompany");
        Document doc2 = new Document("name", "manymanay2").append("company", "ManyCompany2");
        documentList.add(doc);
        documentList.add(doc2);

        for (int i = 3; i < 100; i++)
            documentList.add(new Document("name", "manymanay" + i).append("company", "ManyCompany"));

        InsertManyResult result = collection.insertMany(documentList);
        return "Insert=" + result.toString();
    }

    @RequestMapping(value = "/mongodb/insertComplexBson")
    public String insertComplex() {
        MongoCollection collection = getDatabase(databaseName).getCollection(collectionName);
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

        return "Insert=" + result.toString();
    }

    @RequestMapping(value = "/mongodb/insertTo2ndserver")
    public String insertTo2ndserver() {
        MongoCollection collection = getDatabase(databaseName2).getCollection(collectionName);
        Document doc = new Document("name", "pinpoint").append("company", "Naver");
        collection.insertOne(doc);

        return "OK";
    }

    @RequestMapping(value = "/mongodb/insertNested")
    public String insertNested() {
        MongoCollection collection = getDatabase(databaseName2).getCollection(collectionName);
        Document doc2 = new Document("name", "pinpoint2").append("company", new Document("nestedDoc", "1"));
        InsertOneResult result = collection.insertOne(doc2);
        return "Insert=" + result.toString();
    }

    @RequestMapping(value = "/mongodb/insertArray")
    public String insertArray() {
        MongoCollection collection = getDatabase(databaseName2).getCollection(collectionName);
        BsonValue a = new BsonDouble(111);
        BsonValue b = new BsonDouble(222);
        BsonArray bsonArray = new BsonArray();
        bsonArray.add(a);
        bsonArray.add(b);

        Document doc = new Document("array", bsonArray);
        InsertOneResult result = collection.insertOne(doc);
        return "Insert=" + result.toString();
    }

    @RequestMapping(value = "/mongodb/insertCollection")
    public String insertCollection() {
        MongoCollection collection = getDatabase(databaseName2).getCollection(collectionName);
        Document doc = new Document("Java_Collection", Arrays.asList("naver", "apple"));
        InsertOneResult result = collection.insertOne(doc);
        return "Insert=" + result.toString();
    }

    @RequestMapping(value = "/mongodb/find")
    public String find() {
        MongoCollection collection = getDatabase(databaseName2).getCollection(collectionName);
        Document document = new Document()
                .append("name", new BsonString("roy2"))
                .append("company", new BsonString("Naver2"));

        StringBuilder sb = new StringBuilder();
        try (MongoCursor<Document> cursor = collection.find(document).iterator()) {
            while (cursor.hasNext()) {
                if(sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(cursor.next().toJson());
            }
        }
        return sb.toString();
    }

    @RequestMapping(value = "/mongodb/index")
    public String index() {
        MongoCollection collection = getDatabase(databaseName2).getCollection(collectionName);
        collection.createIndex(Indexes.ascending("name", "company"));

        List<IndexModel> indexes = new ArrayList<>();
        indexes.add(new IndexModel(Indexes.ascending("name2", "company")));
        indexes.add(new IndexModel(Indexes.ascending("name3", "company")));

        List<String> list = collection.createIndexes(indexes);
        return list.toString();
    }

    @RequestMapping(value = "/mongodb/update")
    public String update() {
        MongoCollection collection = getDatabase(databaseName2).getCollection(collectionName);
        Document doc1 = new Document("name", "pinpoint").append("company", "Naver");
        Document doc2 = new Document("$set", new Document("name", "pinpoint2").append("company", "Naver2"));

        UpdateResult updateResult = collection.updateOne(doc1, doc2);
        return "Update=" + updateResult.getMatchedCount();
    }

    @RequestMapping(value = "/mongodb/updateSimple")
    public String updateWithSet() {
        MongoCollection collection = getDatabase(databaseName2).getCollection(collectionName);
        Document doc1 = new Document("name", "pinpoint").append("company", "Naver");

        UpdateResult updateResult = collection.updateOne(doc1, and(set("name", "pinpointWithSet"), set("company", "NaverWithSet")));
        return "Update=" + updateResult.getMatchedCount();
    }

    @RequestMapping(value = "/mongodb/updateWithEach")
    public String updateWithEach() {
        MongoCollection collection = getDatabase(databaseName2).getCollection(collectionName);
        Document doc1 = new Document("name", "pinpoint").append("company", "Naver");

        UpdateResult updateResult = collection.updateOne(doc1, addEachToSet("arrayField", asList("pinpoint", "pinpoint2")), new UpdateOptions().upsert(true));
        return "Update=" + updateResult.getMatchedCount();
    }

    @RequestMapping(value = "/mongodb/updatePush")
    public String updatePush() {
        MongoCollection collection = getDatabase(databaseName2).getCollection(collectionName);
        Document doc1 = new Document("name", "pinpoint").append("company", "Naver");

        UpdateResult updateResult = collection.updateOne(doc1, pushEach("arrayField", asList("pinpoint", "pinpoint2"), new PushOptions().position(1)));
        return "Update=" + updateResult.getMatchedCount();
    }

    @RequestMapping(value = "/mongodb/updatePullAll")
    public String updatePullAll() {
        MongoCollection collection = getDatabase(databaseName2).getCollection(collectionName);
        Document doc1 = new Document("name", "pinpoint").append("company", "Naver");

        UpdateResult updateResult = collection.updateOne(doc1, pullAll("arrayField", asList("pinpoint", "pinpoint2")));
        return "Update=" + updateResult.getMatchedCount();
    }

    @RequestMapping(value = "/mongodb/updateComposite")
    public String updateComposite() {
        MongoCollection collection = getDatabase(databaseName2).getCollection(collectionName);
        Document doc1 = new Document("name", "pinpoint").append("company", "Naver");

        UpdateResult updateResult = collection.updateOne(doc1, combine(asList(pushEach("arrayField", asList("pinpoint", "pinpoint2")), pushEach("arrayField", asList("pinpoint", "pinpoint2")))));
        return "Update=" + updateResult.getMatchedCount();
    }

    @RequestMapping(value = "/mongodb/sortCompound")
    public String sortCompound() {
        MongoCollection collection = getDatabase(databaseName2).getCollection(collectionName);
        FindIterable<Document> iterable = collection.find(not(eq("name", "pinpoint"))).sort(Sorts.ascending("name", "name2"));

        StringBuilder sb = new StringBuilder();
        for (Document document : iterable) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(document.toJson());
        }
        return sb.toString();
    }

    @RequestMapping(value = "/mongodb/filterEQ")
    public String filterEQ() {
        MongoCollection collection = getDatabase(databaseName2).getCollection(collectionName);
        Document doc1 = new Document("name", "pinpoint").append("company", "Naver");
        Document doc2 = new Document("$set", new Document("name", "pinpoint2").append("company", "Naver2"));

        UpdateResult updateResult = collection.updateOne(eq("name", "pinpoint"), doc2);
        return "Update=" + updateResult.getMatchedCount();
    }

    @RequestMapping(value = "/mongodb/filterAND")
    public String filterAND() {
        MongoCollection collection = getDatabase(databaseName2).getCollection(collectionName);
        Document doc1 = new Document("name", "pinpoint").append("company", "Naver");
        Document doc2 = new Document("$set", new Document("name", "pinpoint2").append("company", "Naver2"));

        UpdateResult updateResult = collection.updateOne(and(eq("name", "pinpoint"), eq("company", "Naver")), doc2);
        return "Update=" + updateResult.getMatchedCount();
    }

    @RequestMapping(value = "/mongodb/filterNE")
    public String filterNE() {
        MongoCollection collection = getDatabase(databaseName2).getCollection(collectionName);
        Document doc2 = new Document("$set", new Document("name", "pinpoint2").append("company", "Naver2"));

        UpdateResult updateResult = collection.updateOne(and(ne("name", "pinpoint"), ne("company", "Naver")), doc2);
        return "Update=" + updateResult.getMatchedCount();
    }

    @RequestMapping(value = "/mongodb/filterREGREX")
    public String filterREGREX() {
        MongoCollection collection = getDatabase(databaseName2).getCollection(collectionName);
        FindIterable<Document> iterable = collection.find(regex("name", "%inpoint", "i"));

        StringBuilder sb = new StringBuilder();
        for (Document document : iterable) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(document.toJson());
        }
        return sb.toString();
    }

    @RequestMapping(value = "/mongodb/filterNOT")
    public String filterNOT() {
        MongoCollection collection = getDatabase(databaseName2).getCollection(collectionName);
        FindIterable<Document> iterable = collection.find(not(eq("name", "pinpoint")));

        StringBuilder sb = new StringBuilder();
        for (Document document : iterable) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(document.toJson());
        }
        return sb.toString();
    }

    @RequestMapping(value = "/mongodb/filterIN")
    public String filterIN() {
        MongoCollection collection = getDatabase(databaseName2).getCollection(collectionName);
        FindIterable<Document> iterable = collection.find(in("name", "pinpoint", "pinpoint2"));

        StringBuilder sb = new StringBuilder();
        for (Document document : iterable) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(document.toJson());
        }
        return sb.toString();
    }

    @RequestMapping(value = "/mongodb/filterGEO")
    public String filterGEO() {
        MongoCollection collection = getDatabase(databaseName2).getCollection(collectionName);
        String coords = "28.56402,79.93652a27.27569,26.16394a42.69404,20.02808a48.61541,51.37207a";
        String[] coors = coords.split("a");
        final List<List<Double>> polygons = new ArrayList<>();

        for (int i = 0; i < coors.length; i++) {
            String[] coo = coors[i].split(",");
            polygons.add(Arrays.asList(Double.parseDouble(coo[0]), Double.parseDouble(coo[1])));
        }

        FindIterable<Document> iterable = collection.find(Filters.geoWithinPolygon("loc", polygons));

        StringBuilder sb = new StringBuilder();
        for (Document document : iterable) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(document.toJson());
        }
        return sb.toString();
    }

    @RequestMapping(value = "/mongodb/filterTEXT")
    public String filterTEXT() {
        MongoCollection collection = getDatabase(databaseName2).getCollection(collectionName);
        FindIterable<Document> iterable = collection.find(Filters.text("bakery coffee"));

        StringBuilder sb = new StringBuilder();
        for (Document document : iterable) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(document.toJson());
        }
        return sb.toString();
    }

    @RequestMapping(value = "/mongodb/filterORNOR")
    public String filterORNOR() {
        MongoCollection collection = getDatabase(databaseName2).getCollection(collectionName);
        FindIterable<Document> iterable = collection.find(
                and(
                        or(eq("name", "pinpoint"), eq("company", "Naver")),
                        nor(eq("name", "pinpoint"), eq("company", "Naver"))
                )
        );

        StringBuilder sb = new StringBuilder();
        for (Document document : iterable) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(document.toJson());
        }
        return sb.toString();
    }


    @RequestMapping(value = "/mongodb/delete")
    public String delete() {
        MongoCollection collection = getDatabase(databaseName2).getCollection(collectionName);
        Document doc = new Document("name", "pinpoint").append("company", "Naver");
        DeleteResult deleteResult = collection.deleteMany(doc);

        return "Deleted=" + deleteResult.getDeletedCount();
    }

    private MongoDatabase getDatabase(final String databaseName) {
        String uri = "mongodb://localhost:" + port;
        MongoClient mongo = MongoClients.create(uri);
        MongoDatabase db = mongo.getDatabase(databaseName);

        return db;
    }
}
