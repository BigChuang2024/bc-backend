# 这个脚本用于从数据库中导出1000条数据到json
mongoexport --uri="mongodb://mongo_3EPX8r:mongo_xawRts@bc-mongodb.nanyan.cc:21633/bigchuang_test?authSource=admin" \
            --collection="job_details" \
            --out="./data.json" \
            --sort='{ "_id": 1 }' \
            --limit=1000 \
            --jsonArray


