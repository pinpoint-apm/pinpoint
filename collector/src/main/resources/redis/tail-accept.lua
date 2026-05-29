-- KEYS[1] = buffer:{txid}   (list)
-- KEYS[2] = decision:{txid} (string)
-- KEYS[3] = pending         (zset, global)
-- ARGV[1] = bufferedSpanBytes
-- ARGV[2] = txid (zset member)
-- ARGV[3] = firstSeenMillis
-- ARGV[4] = bufferTtlSeconds
-- returns: "keep" | "drop" | "buffered"
local d = redis.call('GET', KEYS[2])
if d then
    return d
end
redis.call('RPUSH', KEYS[1], ARGV[1])
redis.call('EXPIRE', KEYS[1], ARGV[4])
redis.call('ZADD', KEYS[3], 'NX', ARGV[3], ARGV[2])
return 'buffered'
