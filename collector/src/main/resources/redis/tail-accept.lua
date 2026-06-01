-- KEYS[1] = buffer:{txid}   (list)
-- KEYS[2] = decision:{txid} (string)
-- KEYS[3] = pending         (zset, global)
-- KEYS[4] = error:{txid}    (string flag; set when any span of the trace errors)
-- ARGV[1] = bufferedSpanBytes
-- ARGV[2] = txid (zset member)
-- ARGV[3] = firstSeenMillis
-- ARGV[4] = bufferTtlSeconds
-- ARGV[5] = isError ("1" when this span carries an error, else "0")
-- ARGV[6] = errorTtlSeconds
-- returns: "keep" | "drop" | "buffered"
local d = redis.call('GET', KEYS[2])
if d then
    return d
end
redis.call('RPUSH', KEYS[1], ARGV[1])
redis.call('EXPIRE', KEYS[1], ARGV[4])
redis.call('ZADD', KEYS[3], 'NX', ARGV[3], ARGV[2])
if ARGV[5] == '1' then
    redis.call('SET', KEYS[4], '1', 'EX', tonumber(ARGV[6]))
end
return 'buffered'
