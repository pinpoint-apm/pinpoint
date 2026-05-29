-- KEYS[1] = buffer:{txid}   (list)
-- KEYS[2] = decision:{txid} (string)
-- KEYS[3] = pending         (zset, global)
-- ARGV[1] = "keep" | "drop"
-- ARGV[2] = txid (zset member)
-- ARGV[3] = decisionTtlSeconds
-- returns: false if another node already decided (skip).
--          otherwise {decision, span1, span2, ...} (spans included only when keep).
local ok = redis.call('SET', KEYS[2], ARGV[1], 'NX', 'EX', ARGV[3])
if not ok then
    return false
end
local spans = redis.call('LRANGE', KEYS[1], 0, -1)
redis.call('DEL', KEYS[1])
redis.call('ZREM', KEYS[3], ARGV[2])
local result = {ARGV[1]}
if ARGV[1] == 'keep' then
    for i = 1, #spans do
        result[#result + 1] = spans[i]
    end
end
return result
