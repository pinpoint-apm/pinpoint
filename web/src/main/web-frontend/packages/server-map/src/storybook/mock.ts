import { Node, Edge } from '../types'

export const data: {
  applicationMapData: {
    range: any,
    nodeDataArray: any,
    linkDataArray: any,
  }
} = {
  "applicationMapData": {
    "range": {
      "from": 1664171948000,
      "to": 1664172248000,
      "toInstant": 1664172248.000000000,
      "fromInstant": 1664171948.000000000,
      "fromDateTime": "2022-09-26 14:59:08",
      "toDateTime": "2022-09-26 15:04:08"
    },
    "nodeDataArray": [
      {
        "key": "ACL-PORTAL-DEV_SPRING_BOOT^USER",
        "applicationName": "USER",
        "category": "USER",
        "serviceType": "USER",
        "serviceTypeCode": "2",
        "isWas": false,
        "isQueue": false,
        "isAuthorized": true,
        "totalCount": 20943,
        "errorCount": 0,
        "slowCount": 0,
        "hasAlert": false,
        "responseStatistics": {
          "Tot": 20943,
          "Sum": 73918,
          "Avg": 3,
          "Max": 0
        },
        "histogram": {
          "1s": 20943,
          "3s": 0,
          "5s": 0,
          "Slow": 0,
          "Error": 0
        },
        "apdexScore": 1.0,
        "apdexFormula": {
          "satisfiedCount": 20943,
          "toleratingCount": 0,
          "totalSamples": 20943
        },
        "timeSeriesHistogram": [
          {
            "key": "1s",
            "values": [
              [
                1664171940000,
                4020
              ],
              [
                1664172000000,
                4147
              ],
              [
                1664172060000,
                4044
              ],
              [
                1664172120000,
                4170
              ],
              [
                1664172180000,
                4008
              ],
              [
                1664172240000,
                554
              ]
            ]
          },
          {
            "key": "3s",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "5s",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Slow",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Error",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Avg",
            "values": [
              [
                1664171940000,
                3
              ],
              [
                1664172000000,
                4
              ],
              [
                1664172060000,
                3
              ],
              [
                1664172120000,
                3
              ],
              [
                1664172180000,
                3
              ],
              [
                1664172240000,
                4
              ]
            ]
          },
          {
            "key": "Max",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Sum",
            "values": [
              [
                1664171940000,
                13019
              ],
              [
                1664172000000,
                16811
              ],
              [
                1664172060000,
                14820
              ],
              [
                1664172120000,
                14610
              ],
              [
                1664172180000,
                12404
              ],
              [
                1664172240000,
                2254
              ]
            ]
          },
          {
            "key": "Tot",
            "values": [
              [
                1664171940000,
                4020
              ],
              [
                1664172000000,
                4147
              ],
              [
                1664172060000,
                4044
              ],
              [
                1664172120000,
                4170
              ],
              [
                1664172180000,
                4008
              ],
              [
                1664172240000,
                554
              ]
            ]
          }
        ],
        "instanceCount": 0,
        "instanceErrorCount": 0,
        "agentIds": [],
        "agentIdNameMap": {}
      },
      {
        "key": "ACL-PORTAL-DEV^SPRING_BOOT",
        "applicationName": "ACL-PORTAL-DEV",
        "category": "SPRING_BOOT",
        "serviceType": "SPRING_BOOT",
        "serviceTypeCode": "1210",
        "isWas": true,
        "isQueue": false,
        "isAuthorized": true,
        "totalCount": 21773,
        "errorCount": 0,
        "slowCount": 1,
        "hasAlert": false,
        "responseStatistics": {
          "Tot": 21773,
          "Sum": 217692,
          "Avg": 9,
          "Max": 0
        },
        "histogram": {
          "1s": 21763,
          "3s": 9,
          "5s": 1,
          "Slow": 0,
          "Error": 0
        },
        "apdexScore": 0.999,
        "apdexFormula": {
          "satisfiedCount": 21763,
          "toleratingCount": 9,
          "totalSamples": 21773
        },
        "timeSeriesHistogram": [
          {
            "key": "1s",
            "values": [
              [
                1664171940000,
                4101
              ],
              [
                1664172000000,
                4370
              ],
              [
                1664172060000,
                4245
              ],
              [
                1664172120000,
                4318
              ],
              [
                1664172180000,
                4157
              ],
              [
                1664172240000,
                572
              ]
            ]
          },
          {
            "key": "3s",
            "values": [
              [
                1664171940000,
                3
              ],
              [
                1664172000000,
                3
              ],
              [
                1664172060000,
                1
              ],
              [
                1664172120000,
                2
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "5s",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                1
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Slow",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Error",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Avg",
            "values": [
              [
                1664171940000,
                6
              ],
              [
                1664172000000,
                15
              ],
              [
                1664172060000,
                10
              ],
              [
                1664172120000,
                9
              ],
              [
                1664172180000,
                8
              ],
              [
                1664172240000,
                8
              ]
            ]
          },
          {
            "key": "Max",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Sum",
            "values": [
              [
                1664171940000,
                26880
              ],
              [
                1664172000000,
                67469
              ],
              [
                1664172060000,
                46091
              ],
              [
                1664172120000,
                39020
              ],
              [
                1664172180000,
                33375
              ],
              [
                1664172240000,
                4857
              ]
            ]
          },
          {
            "key": "Tot",
            "values": [
              [
                1664171940000,
                4104
              ],
              [
                1664172000000,
                4374
              ],
              [
                1664172060000,
                4246
              ],
              [
                1664172120000,
                4320
              ],
              [
                1664172180000,
                4157
              ],
              [
                1664172240000,
                572
              ]
            ]
          }
        ],
        "instanceCount": 2,
        "instanceErrorCount": 0,
        "agentIds": [
          "dev-aclportal01.ncl",
          "dev-aclportal02.ncl"
        ],
        "agentIdNameMap": {
          "dev-aclportal01.ncl": "",
          "dev-aclportal02.ncl": ""
        }
      },
      {
        "key": "spt.dev-cerberus-admin^SPRING_BOOT",
        "applicationName": "spt.dev-cerberus-admin",
        "category": "SPRING_BOOT",
        "serviceType": "SPRING_BOOT",
        "serviceTypeCode": "1210",
        "isWas": true,
        "isQueue": false,
        "isAuthorized": true,
        "totalCount": 206,
        "errorCount": 0,
        "slowCount": 0,
        "hasAlert": false,
        "responseStatistics": {
          "Tot": 206,
          "Sum": 10462,
          "Avg": 50,
          "Max": 0
        },
        "histogram": {
          "1s": 205,
          "3s": 1,
          "5s": 0,
          "Slow": 0,
          "Error": 0
        },
        "apdexScore": 0.995,
        "apdexFormula": {
          "satisfiedCount": 205,
          "toleratingCount": 1,
          "totalSamples": 206
        },
        "timeSeriesHistogram": [
          {
            "key": "1s",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                182
              ],
              [
                1664172120000,
                23
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "3s",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                1
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "5s",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Slow",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Error",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Avg",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                49
              ],
              [
                1664172120000,
                62
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Max",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Sum",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                9030
              ],
              [
                1664172120000,
                1432
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Tot",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                183
              ],
              [
                1664172120000,
                23
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          }
        ],
        "instanceCount": 2,
        "instanceErrorCount": 0,
        "agentIds": [
          "t-220811-192514-7dl87cx",
          "t-220926-145401-68ph5kv"
        ],
        "agentIdNameMap": {
          "t-220811-192514-7dl87cx": "",
          "t-220926-145401-68ph5kv": ""
        }
      },
      {
        "key": "acl^MYSQL",
        "applicationName": "acl",
        "category": "MYSQL",
        "serviceType": "MYSQL",
        "serviceTypeCode": "2101",
        "isWas": false,
        "isQueue": false,
        "isAuthorized": true,
        "totalCount": 45281,
        "errorCount": 0,
        "slowCount": 0,
        "hasAlert": false,
        "responseStatistics": {
          "Tot": 45281,
          "Sum": 15175,
          "Avg": 0,
          "Max": 0
        },
        "histogram": {
          "1s": 45281,
          "3s": 0,
          "5s": 0,
          "Slow": 0,
          "Error": 0
        },
        "apdexScore": 1.0,
        "apdexFormula": {
          "satisfiedCount": 45281,
          "toleratingCount": 0,
          "totalSamples": 45281
        },
        "timeSeriesHistogram": [
          {
            "key": "1s",
            "values": [
              [
                1664171940000,
                8358
              ],
              [
                1664172000000,
                9369
              ],
              [
                1664172060000,
                8839
              ],
              [
                1664172120000,
                8948
              ],
              [
                1664172180000,
                8509
              ],
              [
                1664172240000,
                1258
              ]
            ]
          },
          {
            "key": "3s",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "5s",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Slow",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Error",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Avg",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Max",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Sum",
            "values": [
              [
                1664171940000,
                2721
              ],
              [
                1664172000000,
                3283
              ],
              [
                1664172060000,
                3010
              ],
              [
                1664172120000,
                2992
              ],
              [
                1664172180000,
                2704
              ],
              [
                1664172240000,
                465
              ]
            ]
          },
          {
            "key": "Tot",
            "values": [
              [
                1664171940000,
                8358
              ],
              [
                1664172000000,
                9369
              ],
              [
                1664172060000,
                8839
              ],
              [
                1664172120000,
                8948
              ],
              [
                1664172180000,
                8509
              ],
              [
                1664172240000,
                1258
              ]
            ]
          }
        ],
        "instanceCount": 1,
        "instanceErrorCount": 0,
        "agentIds": [
          "10.113.130.122:13306"
        ],
        "agentIdNameMap": {
          "10.113.130.122:13306": null
        }
      },
      {
        "key": "STOREP_MEMBER_API_ENVOY^ENVOY",
        "applicationName": "STOREP_MEMBER_API_ENVOY",
        "category": "ENVOY",
        "serviceType": "ENVOY",
        "serviceTypeCode": "1550",
        "isWas": true,
        "isQueue": false,
        "isAuthorized": true,
        "totalCount": 202,
        "errorCount": 0,
        "slowCount": 0,
        "hasAlert": false,
        "responseStatistics": {
          "Tot": 202,
          "Sum": 2651,
          "Avg": 13,
          "Max": 0
        },
        "histogram": {
          "1s": 202,
          "3s": 0,
          "5s": 0,
          "Slow": 0,
          "Error": 0
        },
        "apdexScore": 1.0,
        "apdexFormula": {
          "satisfiedCount": 202,
          "toleratingCount": 0,
          "totalSamples": 202
        },
        "timeSeriesHistogram": [
          {
            "key": "1s",
            "values": [
              [
                1664171940000,
                35
              ],
              [
                1664172000000,
                46
              ],
              [
                1664172060000,
                41
              ],
              [
                1664172120000,
                36
              ],
              [
                1664172180000,
                35
              ],
              [
                1664172240000,
                9
              ]
            ]
          },
          {
            "key": "3s",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "5s",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Slow",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Error",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Avg",
            "values": [
              [
                1664171940000,
                14
              ],
              [
                1664172000000,
                12
              ],
              [
                1664172060000,
                14
              ],
              [
                1664172120000,
                10
              ],
              [
                1664172180000,
                8
              ],
              [
                1664172240000,
                32
              ]
            ]
          },
          {
            "key": "Max",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Sum",
            "values": [
              [
                1664171940000,
                502
              ],
              [
                1664172000000,
                568
              ],
              [
                1664172060000,
                596
              ],
              [
                1664172120000,
                379
              ],
              [
                1664172180000,
                310
              ],
              [
                1664172240000,
                296
              ]
            ]
          },
          {
            "key": "Tot",
            "values": [
              [
                1664171940000,
                35
              ],
              [
                1664172000000,
                46
              ],
              [
                1664172060000,
                41
              ],
              [
                1664172120000,
                36
              ],
              [
                1664172180000,
                35
              ],
              [
                1664172240000,
                9
              ]
            ]
          }
        ],
        "instanceCount": 9,
        "instanceErrorCount": 0,
        "agentIds": [
          "dev-member-api-iss-747qe",
          "dev-member-api-iss-kmjpu",
          "dev-member-api-iss-xzzzz",
          "dev-member-api-iss-gbgzh",
          "dev-member-api-iss-lasy6",
          "dev-member-api-iss-wryca",
          "dev-member-api-iss-e7am1",
          "dev-member-api-iss-68f52",
          "dev-member-api-iss-u3sm0"
        ],
        "agentIdNameMap": {
          "dev-member-api-iss-747qe": "",
          "dev-member-api-iss-kmjpu": "",
          "dev-member-api-iss-xzzzz": "",
          "dev-member-api-iss-gbgzh": "",
          "dev-member-api-iss-lasy6": "",
          "dev-member-api-iss-wryca": "",
          "dev-member-api-iss-e7am1": "",
          "dev-member-api-iss-68f52": "",
          "dev-member-api-iss-u3sm0": ""
        }
      },
      {
        "key": "cvkafaka^KAFKA_CLIENT",
        "applicationName": "cvkafaka",
        "category": "KAFKA_CLIENT",
        "serviceType": "KAFKA_CLIENT",
        "serviceTypeCode": "8660",
        "isWas": false,
        "isQueue": true,
        "isAuthorized": true,
        "totalCount": 0,
        "errorCount": 0,
        "slowCount": 0,
        "hasAlert": false,
        "responseStatistics": {
          "Tot": 0,
          "Sum": 0,
          "Avg": 0,
          "Max": 0
        },
        "histogram": {
          "100ms": 0,
          "300ms": 0,
          "500ms": 0,
          "Slow": 0,
          "Error": 0
        },
        "apdexScore": 0.0,
        "apdexFormula": {
          "satisfiedCount": 0,
          "toleratingCount": 0,
          "totalSamples": 0
        },
        "timeSeriesHistogram": [
          {
            "key": "100ms",
            "values": []
          },
          {
            "key": "300ms",
            "values": []
          },
          {
            "key": "500ms",
            "values": []
          },
          {
            "key": "Slow",
            "values": []
          },
          {
            "key": "Error",
            "values": []
          },
          {
            "key": "Avg",
            "values": []
          },
          {
            "key": "Max",
            "values": []
          },
          {
            "key": "Sum",
            "values": []
          },
          {
            "key": "Tot",
            "values": []
          }
        ],
        "instanceCount": 0,
        "instanceErrorCount": 0,
        "agentIds": [],
        "agentIdNameMap": {}
      },
      {
        "key": "api.nconsole.com:3000^UNKNOWN",
        "applicationName": "api.nconsole.com:3000",
        "category": "UNKNOWN",
        "serviceType": "UNKNOWN",
        "serviceTypeCode": "1",
        "isWas": false,
        "isQueue": false,
        "isAuthorized": true,
        "totalCount": 50,
        "errorCount": 0,
        "slowCount": 0,
        "hasAlert": false,
        "responseStatistics": {
          "Tot": 50,
          "Sum": 212,
          "Avg": 4,
          "Max": 0
        },
        "histogram": {
          "1s": 50,
          "3s": 0,
          "5s": 0,
          "Slow": 0,
          "Error": 0
        },
        "apdexScore": 1.0,
        "apdexFormula": {
          "satisfiedCount": 50,
          "toleratingCount": 0,
          "totalSamples": 50
        },
        "timeSeriesHistogram": [
          {
            "key": "1s",
            "values": [
              [
                1664171940000,
                3
              ],
              [
                1664172000000,
                18
              ],
              [
                1664172060000,
                10
              ],
              [
                1664172120000,
                10
              ],
              [
                1664172180000,
                4
              ],
              [
                1664172240000,
                5
              ]
            ]
          },
          {
            "key": "3s",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "5s",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Slow",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Error",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Avg",
            "values": [
              [
                1664171940000,
                4
              ],
              [
                1664172000000,
                4
              ],
              [
                1664172060000,
                3
              ],
              [
                1664172120000,
                4
              ],
              [
                1664172180000,
                4
              ],
              [
                1664172240000,
                4
              ]
            ]
          },
          {
            "key": "Max",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Sum",
            "values": [
              [
                1664171940000,
                13
              ],
              [
                1664172000000,
                76
              ],
              [
                1664172060000,
                39
              ],
              [
                1664172120000,
                41
              ],
              [
                1664172180000,
                19
              ],
              [
                1664172240000,
                24
              ]
            ]
          },
          {
            "key": "Tot",
            "values": [
              [
                1664171940000,
                3
              ],
              [
                1664172000000,
                18
              ],
              [
                1664172060000,
                10
              ],
              [
                1664172120000,
                10
              ],
              [
                1664172180000,
                4
              ],
              [
                1664172240000,
                5
              ]
            ]
          }
        ],
        "instanceCount": 0,
        "instanceErrorCount": 0,
        "agentIds": [],
        "agentIdNameMap": {}
      },
      {
        "key": "esfarm-cluster.svc.com:10200^UNKNOWN",
        "applicationName": "esfarm-cluster.svc.com:10200",
        "category": "UNKNOWN",
        "serviceType": "UNKNOWN",
        "serviceTypeCode": "1",
        "isWas": false,
        "isQueue": false,
        "isAuthorized": true,
        "totalCount": 679,
        "errorCount": 0,
        "slowCount": 1,
        "hasAlert": false,
        "responseStatistics": {
          "Tot": 679,
          "Sum": 140605,
          "Avg": 207,
          "Max": 0
        },
        "histogram": {
          "1s": 669,
          "3s": 9,
          "5s": 1,
          "Slow": 0,
          "Error": 0
        },
        "apdexScore": 0.991,
        "apdexFormula": {
          "satisfiedCount": 669,
          "toleratingCount": 9,
          "totalSamples": 679
        },
        "timeSeriesHistogram": [
          {
            "key": "1s",
            "values": [
              [
                1664171940000,
                51
              ],
              [
                1664172000000,
                193
              ],
              [
                1664172060000,
                170
              ],
              [
                1664172120000,
                118
              ],
              [
                1664172180000,
                119
              ],
              [
                1664172240000,
                18
              ]
            ]
          },
          {
            "key": "3s",
            "values": [
              [
                1664171940000,
                3
              ],
              [
                1664172000000,
                3
              ],
              [
                1664172060000,
                1
              ],
              [
                1664172120000,
                2
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "5s",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                1
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Slow",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Error",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Avg",
            "values": [
              [
                1664171940000,
                251
              ],
              [
                1664172000000,
                252
              ],
              [
                1664172060000,
                178
              ],
              [
                1664172120000,
                198
              ],
              [
                1664172180000,
                172
              ],
              [
                1664172240000,
                138
              ]
            ]
          },
          {
            "key": "Max",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Sum",
            "values": [
              [
                1664171940000,
                13554
              ],
              [
                1664172000000,
                49713
              ],
              [
                1664172060000,
                30518
              ],
              [
                1664172120000,
                23851
              ],
              [
                1664172180000,
                20478
              ],
              [
                1664172240000,
                2491
              ]
            ]
          },
          {
            "key": "Tot",
            "values": [
              [
                1664171940000,
                54
              ],
              [
                1664172000000,
                197
              ],
              [
                1664172060000,
                171
              ],
              [
                1664172120000,
                120
              ],
              [
                1664172180000,
                119
              ],
              [
                1664172240000,
                18
              ]
            ]
          }
        ],
        "instanceCount": 0,
        "instanceErrorCount": 0,
        "agentIds": [],
        "agentIdNameMap": {}
      }
    ],
    "linkDataArray": [
      {
        "key": "ACL-PORTAL-DEV^SPRING_BOOT~api.nconsole.com:3000^UNKNOWN",
        "from": "ACL-PORTAL-DEV^SPRING_BOOT",
        "to": "api.nconsole.com:3000^UNKNOWN",
        "fromAgent": [
          "dev-aclportal01.ncl",
          "dev-aclportal02.ncl"
        ],
        "fromAgentIdNameMap": {
          "dev-aclportal01.ncl": "",
          "dev-aclportal02.ncl": ""
        },
        "sourceInfo": {
          "applicationName": "ACL-PORTAL-DEV",
          "serviceType": "SPRING_BOOT",
          "serviceTypeCode": 1210,
          "isWas": true
        },
        "targetInfo": {
          "applicationName": "api.nconsole.com:3000",
          "serviceType": "UNKNOWN",
          "serviceTypeCode": 1,
          "isWas": false
        },
        "filterApplicationName": "ACL-PORTAL-DEV",
        "filterApplicationServiceTypeCode": 1210,
        "filterApplicationServiceTypeName": "SPRING_BOOT",
        "totalCount": 50,
        "errorCount": 0,
        "slowCount": 0,
        "responseStatistics": {
          "Tot": 50,
          "Sum": 212,
          "Avg": 4,
          "Max": 0
        },
        "histogram": {
          "1s": 50,
          "3s": 0,
          "5s": 0,
          "Slow": 0,
          "Error": 0
        },
        "timeSeriesHistogram": [
          {
            "key": "1s",
            "values": [
              [
                1664171940000,
                3
              ],
              [
                1664172000000,
                18
              ],
              [
                1664172060000,
                10
              ],
              [
                1664172120000,
                10
              ],
              [
                1664172180000,
                4
              ],
              [
                1664172240000,
                5
              ]
            ]
          },
          {
            "key": "3s",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "5s",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Slow",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Error",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Avg",
            "values": [
              [
                1664171940000,
                4
              ],
              [
                1664172000000,
                4
              ],
              [
                1664172060000,
                3
              ],
              [
                1664172120000,
                4
              ],
              [
                1664172180000,
                4
              ],
              [
                1664172240000,
                4
              ]
            ]
          },
          {
            "key": "Max",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Sum",
            "values": [
              [
                1664171940000,
                13
              ],
              [
                1664172000000,
                76
              ],
              [
                1664172060000,
                39
              ],
              [
                1664172120000,
                41
              ],
              [
                1664172180000,
                19
              ],
              [
                1664172240000,
                24
              ]
            ]
          },
          {
            "key": "Tot",
            "values": [
              [
                1664171940000,
                3
              ],
              [
                1664172000000,
                18
              ],
              [
                1664172060000,
                10
              ],
              [
                1664172120000,
                10
              ],
              [
                1664172180000,
                4
              ],
              [
                1664172240000,
                5
              ]
            ]
          }
        ],
        "hasAlert": false
      },
      {
        "key": "STOREP_MEMBER_API_ENVOY^ENVOY~ACL-PORTAL-DEV^SPRING_BOOT",
        "from": "STOREP_MEMBER_API_ENVOY^ENVOY",
        "to": "ACL-PORTAL-DEV^SPRING_BOOT",
        "fromAgent": [
          "dev-member-api-iss-747qe",
          "dev-member-api-iss-kmjpu",
          "dev-member-api-iss-xzzzz",
          "dev-member-api-iss-gbgzh",
          "dev-member-api-iss-lasy6",
          "dev-member-api-iss-wryca",
          "dev-member-api-iss-e7am1",
          "dev-member-api-iss-68f52",
          "dev-member-api-iss-u3sm0"
        ],
        "toAgent": [
          "dev-aclportal01.ncl",
          "dev-aclportal02.ncl"
        ],
        "fromAgentIdNameMap": {
          "dev-member-api-iss-747qe": "",
          "dev-member-api-iss-kmjpu": "",
          "dev-member-api-iss-xzzzz": "",
          "dev-member-api-iss-gbgzh": "",
          "dev-member-api-iss-lasy6": "",
          "dev-member-api-iss-wryca": "",
          "dev-member-api-iss-e7am1": "",
          "dev-member-api-iss-68f52": "",
          "dev-member-api-iss-u3sm0": ""
        },
        "toAgentIdNameMap": {
          "dev-aclportal01.ncl": "",
          "dev-aclportal02.ncl": ""
        },
        "sourceInfo": {
          "applicationName": "STOREP_MEMBER_API_ENVOY",
          "serviceType": "ENVOY",
          "serviceTypeCode": 1550,
          "isWas": true
        },
        "targetInfo": {
          "applicationName": "ACL-PORTAL-DEV",
          "serviceType": "SPRING_BOOT",
          "serviceTypeCode": 1210,
          "isWas": true
        },
        "filterApplicationName": "STOREP_MEMBER_API_ENVOY",
        "filterApplicationServiceTypeCode": 1550,
        "filterApplicationServiceTypeName": "ENVOY",
        "filterTargetRpcList": [],
        "totalCount": 154,
        "errorCount": 0,
        "slowCount": 0,
        "responseStatistics": {
          "Tot": 154,
          "Sum": 563,
          "Avg": 3,
          "Max": 0
        },
        "histogram": {
          "1s": 154,
          "3s": 0,
          "5s": 0,
          "Slow": 0,
          "Error": 0
        },
        "timeSeriesHistogram": [
          {
            "key": "1s",
            "values": [
              [
                1664171940000,
                30
              ],
              [
                1664172000000,
                30
              ],
              [
                1664172060000,
                30
              ],
              [
                1664172120000,
                30
              ],
              [
                1664172180000,
                30
              ],
              [
                1664172240000,
                4
              ]
            ]
          },
          {
            "key": "3s",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "5s",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Slow",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Error",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Avg",
            "values": [
              [
                1664171940000,
                3
              ],
              [
                1664172000000,
                4
              ],
              [
                1664172060000,
                4
              ],
              [
                1664172120000,
                3
              ],
              [
                1664172180000,
                3
              ],
              [
                1664172240000,
                3
              ]
            ]
          },
          {
            "key": "Max",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Sum",
            "values": [
              [
                1664171940000,
                94
              ],
              [
                1664172000000,
                131
              ],
              [
                1664172060000,
                136
              ],
              [
                1664172120000,
                100
              ],
              [
                1664172180000,
                90
              ],
              [
                1664172240000,
                12
              ]
            ]
          },
          {
            "key": "Tot",
            "values": [
              [
                1664171940000,
                30
              ],
              [
                1664172000000,
                30
              ],
              [
                1664172060000,
                30
              ],
              [
                1664172120000,
                30
              ],
              [
                1664172180000,
                30
              ],
              [
                1664172240000,
                4
              ]
            ]
          }
        ],
        "hasAlert": false
      },
      {
        "key": "ACL-PORTAL-DEV^SPRING_BOOT~acl^MYSQL",
        "from": "ACL-PORTAL-DEV^SPRING_BOOT",
        "to": "acl^MYSQL",
        "fromAgent": [
          "dev-aclportal01.ncl",
          "dev-aclportal02.ncl"
        ],
        "fromAgentIdNameMap": {
          "dev-aclportal01.ncl": "",
          "dev-aclportal02.ncl": ""
        },
        "sourceInfo": {
          "applicationName": "ACL-PORTAL-DEV",
          "serviceType": "SPRING_BOOT",
          "serviceTypeCode": 1210,
          "isWas": true
        },
        "targetInfo": {
          "applicationName": "acl",
          "serviceType": "MYSQL",
          "serviceTypeCode": 2101,
          "isWas": false
        },
        "filterApplicationName": "ACL-PORTAL-DEV",
        "filterApplicationServiceTypeCode": 1210,
        "filterApplicationServiceTypeName": "SPRING_BOOT",
        "totalCount": 45281,
        "errorCount": 0,
        "slowCount": 0,
        "responseStatistics": {
          "Tot": 45281,
          "Sum": 15175,
          "Avg": 0,
          "Max": 0
        },
        "histogram": {
          "1s": 45281,
          "3s": 0,
          "5s": 0,
          "Slow": 0,
          "Error": 0
        },
        "timeSeriesHistogram": [
          {
            "key": "1s",
            "values": [
              [
                1664171940000,
                8358
              ],
              [
                1664172000000,
                9369
              ],
              [
                1664172060000,
                8839
              ],
              [
                1664172120000,
                8948
              ],
              [
                1664172180000,
                8509
              ],
              [
                1664172240000,
                1258
              ]
            ]
          },
          {
            "key": "3s",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "5s",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Slow",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Error",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Avg",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Max",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Sum",
            "values": [
              [
                1664171940000,
                2721
              ],
              [
                1664172000000,
                3283
              ],
              [
                1664172060000,
                3010
              ],
              [
                1664172120000,
                2992
              ],
              [
                1664172180000,
                2704
              ],
              [
                1664172240000,
                465
              ]
            ]
          },
          {
            "key": "Tot",
            "values": [
              [
                1664171940000,
                8358
              ],
              [
                1664172000000,
                9369
              ],
              [
                1664172060000,
                8839
              ],
              [
                1664172120000,
                8948
              ],
              [
                1664172180000,
                8509
              ],
              [
                1664172240000,
                1258
              ]
            ]
          }
        ],
        "hasAlert": false
      },
      {
        "key": "spt.dev-cerberus-admin^SPRING_BOOT~ACL-PORTAL-DEV^SPRING_BOOT",
        "from": "spt.dev-cerberus-admin^SPRING_BOOT",
        "to": "ACL-PORTAL-DEV^SPRING_BOOT",
        "fromAgent": [
          "t-220811-192514-7dl87cx",
          "t-220926-145401-68ph5kv"
        ],
        "toAgent": [
          "dev-aclportal01.ncl",
          "dev-aclportal02.ncl"
        ],
        "fromAgentIdNameMap": {
          "t-220811-192514-7dl87cx": "",
          "t-220926-145401-68ph5kv": ""
        },
        "toAgentIdNameMap": {
          "dev-aclportal01.ncl": "",
          "dev-aclportal02.ncl": ""
        },
        "sourceInfo": {
          "applicationName": "spt.dev-cerberus-admin",
          "serviceType": "SPRING_BOOT",
          "serviceTypeCode": 1210,
          "isWas": true
        },
        "targetInfo": {
          "applicationName": "ACL-PORTAL-DEV",
          "serviceType": "SPRING_BOOT",
          "serviceTypeCode": 1210,
          "isWas": true
        },
        "filterApplicationName": "spt.dev-cerberus-admin",
        "filterApplicationServiceTypeCode": 1210,
        "filterApplicationServiceTypeName": "SPRING_BOOT",
        "filterTargetRpcList": [],
        "totalCount": 1,
        "errorCount": 0,
        "slowCount": 0,
        "responseStatistics": {
          "Tot": 1,
          "Sum": 3,
          "Avg": 3,
          "Max": 0
        },
        "histogram": {
          "1s": 1,
          "3s": 0,
          "5s": 0,
          "Slow": 0,
          "Error": 0
        },
        "timeSeriesHistogram": [
          {
            "key": "1s",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                1
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "3s",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "5s",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Slow",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Error",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Avg",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                3
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Max",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Sum",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                3
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Tot",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                1
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          }
        ],
        "hasAlert": false
      },
      {
        "key": "ACL-PORTAL-DEV_SPRING_BOOT^USER~ACL-PORTAL-DEV^SPRING_BOOT",
        "from": "ACL-PORTAL-DEV_SPRING_BOOT^USER",
        "to": "ACL-PORTAL-DEV^SPRING_BOOT",
        "toAgent": [
          "dev-aclportal01.ncl",
          "dev-aclportal02.ncl"
        ],
        "toAgentIdNameMap": {
          "dev-aclportal01.ncl": "",
          "dev-aclportal02.ncl": ""
        },
        "sourceInfo": {
          "applicationName": "ACL-PORTAL-DEV_SPRING_BOOT",
          "serviceType": "USER",
          "serviceTypeCode": 2,
          "isWas": false
        },
        "targetInfo": {
          "applicationName": "ACL-PORTAL-DEV",
          "serviceType": "SPRING_BOOT",
          "serviceTypeCode": 1210,
          "isWas": true
        },
        "filterApplicationName": "ACL-PORTAL-DEV",
        "filterApplicationServiceTypeCode": 1210,
        "filterApplicationServiceTypeName": "SPRING_BOOT",
        "totalCount": 20943,
        "errorCount": 0,
        "slowCount": 0,
        "responseStatistics": {
          "Tot": 20943,
          "Sum": 73918,
          "Avg": 3,
          "Max": 0
        },
        "histogram": {
          "1s": 20943,
          "3s": 0,
          "5s": 0,
          "Slow": 0,
          "Error": 0
        },
        "timeSeriesHistogram": [
          {
            "key": "1s",
            "values": [
              [
                1664171940000,
                4020
              ],
              [
                1664172000000,
                4147
              ],
              [
                1664172060000,
                4044
              ],
              [
                1664172120000,
                4170
              ],
              [
                1664172180000,
                4008
              ],
              [
                1664172240000,
                554
              ]
            ]
          },
          {
            "key": "3s",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "5s",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Slow",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Error",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Avg",
            "values": [
              [
                1664171940000,
                3
              ],
              [
                1664172000000,
                4
              ],
              [
                1664172060000,
                3
              ],
              [
                1664172120000,
                3
              ],
              [
                1664172180000,
                3
              ],
              [
                1664172240000,
                4
              ]
            ]
          },
          {
            "key": "Max",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Sum",
            "values": [
              [
                1664171940000,
                13019
              ],
              [
                1664172000000,
                16811
              ],
              [
                1664172060000,
                14820
              ],
              [
                1664172120000,
                14610
              ],
              [
                1664172180000,
                12404
              ],
              [
                1664172240000,
                2254
              ]
            ]
          },
          {
            "key": "Tot",
            "values": [
              [
                1664171940000,
                4020
              ],
              [
                1664172000000,
                4147
              ],
              [
                1664172060000,
                4044
              ],
              [
                1664172120000,
                4170
              ],
              [
                1664172180000,
                4008
              ],
              [
                1664172240000,
                554
              ]
            ]
          }
        ],
        "hasAlert": false
      },
      {
        "key": "ACL-PORTAL-DEV^SPRING_BOOT~esfarm-cluster.svc.com:10200^UNKNOWN",
        "from": "ACL-PORTAL-DEV^SPRING_BOOT",
        "to": "esfarm-cluster.svc.com:10200^UNKNOWN",
        "fromAgent": [
          "dev-aclportal01.ncl",
          "dev-aclportal02.ncl"
        ],
        "fromAgentIdNameMap": {
          "dev-aclportal01.ncl": "",
          "dev-aclportal02.ncl": ""
        },
        "sourceInfo": {
          "applicationName": "ACL-PORTAL-DEV",
          "serviceType": "SPRING_BOOT",
          "serviceTypeCode": 1210,
          "isWas": true
        },
        "targetInfo": {
          "applicationName": "esfarm-cluster.svc.com:10200",
          "serviceType": "UNKNOWN",
          "serviceTypeCode": 1,
          "isWas": false
        },
        "filterApplicationName": "ACL-PORTAL-DEV",
        "filterApplicationServiceTypeCode": 1210,
        "filterApplicationServiceTypeName": "SPRING_BOOT",
        "totalCount": 679,
        "errorCount": 0,
        "slowCount": 1,
        "responseStatistics": {
          "Tot": 679,
          "Sum": 140605,
          "Avg": 207,
          "Max": 0
        },
        "histogram": {
          "1s": 669,
          "3s": 9,
          "5s": 1,
          "Slow": 0,
          "Error": 0
        },
        "timeSeriesHistogram": [
          {
            "key": "1s",
            "values": [
              [
                1664171940000,
                51
              ],
              [
                1664172000000,
                193
              ],
              [
                1664172060000,
                170
              ],
              [
                1664172120000,
                118
              ],
              [
                1664172180000,
                119
              ],
              [
                1664172240000,
                18
              ]
            ]
          },
          {
            "key": "3s",
            "values": [
              [
                1664171940000,
                3
              ],
              [
                1664172000000,
                3
              ],
              [
                1664172060000,
                1
              ],
              [
                1664172120000,
                2
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "5s",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                1
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Slow",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Error",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Avg",
            "values": [
              [
                1664171940000,
                251
              ],
              [
                1664172000000,
                252
              ],
              [
                1664172060000,
                178
              ],
              [
                1664172120000,
                198
              ],
              [
                1664172180000,
                172
              ],
              [
                1664172240000,
                138
              ]
            ]
          },
          {
            "key": "Max",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Sum",
            "values": [
              [
                1664171940000,
                13554
              ],
              [
                1664172000000,
                49713
              ],
              [
                1664172060000,
                30518
              ],
              [
                1664172120000,
                23851
              ],
              [
                1664172180000,
                20478
              ],
              [
                1664172240000,
                2491
              ]
            ]
          },
          {
            "key": "Tot",
            "values": [
              [
                1664171940000,
                54
              ],
              [
                1664172000000,
                197
              ],
              [
                1664172060000,
                171
              ],
              [
                1664172120000,
                120
              ],
              [
                1664172180000,
                119
              ],
              [
                1664172240000,
                18
              ]
            ]
          }
        ],
        "hasAlert": false
      },
      {
        "key": "cvkafaka^KAFKA_CLIENT~ACL-PORTAL-DEV^SPRING_BOOT",
        "from": "cvkafaka^KAFKA_CLIENT",
        "to": "ACL-PORTAL-DEV^SPRING_BOOT",
        "toAgent": [
          "dev-aclportal01.ncl",
          "dev-aclportal02.ncl"
        ],
        "toAgentIdNameMap": {
          "dev-aclportal01.ncl": "",
          "dev-aclportal02.ncl": ""
        },
        "sourceInfo": {
          "applicationName": "cvkafaka",
          "serviceType": "KAFKA_CLIENT",
          "serviceTypeCode": 8660,
          "isWas": false
        },
        "targetInfo": {
          "applicationName": "ACL-PORTAL-DEV",
          "serviceType": "SPRING_BOOT",
          "serviceTypeCode": 1210,
          "isWas": true
        },
        "filterApplicationName": "ACL-PORTAL-DEV",
        "filterApplicationServiceTypeCode": 1210,
        "filterApplicationServiceTypeName": "SPRING_BOOT",
        "totalCount": 679,
        "errorCount": 0,
        "slowCount": 1,
        "responseStatistics": {
          "Tot": 679,
          "Sum": 143222,
          "Avg": 210,
          "Max": 0
        },
        "histogram": {
          "1s": 669,
          "3s": 9,
          "5s": 1,
          "Slow": 0,
          "Error": 0
        },
        "timeSeriesHistogram": [
          {
            "key": "1s",
            "values": [
              [
                1664171940000,
                51
              ],
              [
                1664172000000,
                193
              ],
              [
                1664172060000,
                170
              ],
              [
                1664172120000,
                118
              ],
              [
                1664172180000,
                119
              ],
              [
                1664172240000,
                18
              ]
            ]
          },
          {
            "key": "3s",
            "values": [
              [
                1664171940000,
                3
              ],
              [
                1664172000000,
                3
              ],
              [
                1664172060000,
                1
              ],
              [
                1664172120000,
                2
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "5s",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                1
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Slow",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Error",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Avg",
            "values": [
              [
                1664171940000,
                254
              ],
              [
                1664172000000,
                256
              ],
              [
                1664172060000,
                182
              ],
              [
                1664172120000,
                202
              ],
              [
                1664172180000,
                175
              ],
              [
                1664172240000,
                144
              ]
            ]
          },
          {
            "key": "Max",
            "values": [
              [
                1664171940000,
                0
              ],
              [
                1664172000000,
                0
              ],
              [
                1664172060000,
                0
              ],
              [
                1664172120000,
                0
              ],
              [
                1664172180000,
                0
              ],
              [
                1664172240000,
                0
              ]
            ]
          },
          {
            "key": "Sum",
            "values": [
              [
                1664171940000,
                13767
              ],
              [
                1664172000000,
                50527
              ],
              [
                1664172060000,
                31132
              ],
              [
                1664172120000,
                24310
              ],
              [
                1664172180000,
                20881
              ],
              [
                1664172240000,
                2605
              ]
            ]
          },
          {
            "key": "Tot",
            "values": [
              [
                1664171940000,
                54
              ],
              [
                1664172000000,
                197
              ],
              [
                1664172060000,
                171
              ],
              [
                1664172120000,
                120
              ],
              [
                1664172180000,
                119
              ],
              [
                1664172240000,
                18
              ]
            ]
          }
        ],
        "hasAlert": false
      }
    ]
  }
}

const getTransactionInfo = (node: any) => {
  const { isWas, isAuthorized } = node;

  if (isWas && isAuthorized) {
    return {
      good: ['1s', '3s', '5s'].reduce((prev, curr) => {
        return prev + node?.histogram?.[curr]!;
      }, 0),
      slow: node.histogram?.Slow!,
      bad: node.histogram?.Error!,
    }
  }

}

export const getServerMapData = (): {
  nodes: Node[],
  edges: Edge[],
} => {
  const { nodeDataArray = [], linkDataArray = [] } = data?.applicationMapData!;
  const nodes = nodeDataArray.map((node: any) => {
    return {
      id: node.key,
      label: node.applicationName,
      type: node.serviceType,
      imgPath: `/assets/img/servers/${node.serviceType}.png`,
      transactionInfo: getTransactionInfo(node),
    };
  });

  const edges = linkDataArray.map((link: any, i: number) => ({
    id: link.key,
    source: link.from,
    target: link.to,
    transactionInfo: {
      totalCount: link.totalCount,
    },
  }));

  return {
    nodes,
    edges,
  }
}